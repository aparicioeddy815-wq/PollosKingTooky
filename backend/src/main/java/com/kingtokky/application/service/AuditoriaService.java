package com.kingtokky.application.service;

import com.kingtokky.domain.model.auditoria.Bitacora;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.repository.BitacoraRepository;
import com.kingtokky.infrastructure.security.ContextoSeguridad;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Escribe en la bitácora de auditoría. Cada registro va en su propia transacción para que quede guardado
 * aunque la operación de negocio falle o se revierta.
 */
@Service
public class AuditoriaService {

    public static final String ATRIBUTO_REGISTRADA = "kingtokky.auditoria.registrada";

    private final BitacoraRepository bitacora;

    public AuditoriaService(BitacoraRepository bitacora) {
        this.bitacora = bitacora;
    }

    /** Registra un evento a nombre del usuario autenticado en la petición actual. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(TipoEvento tipo, String modulo, String accion, NivelBitacora nivel) {
        var usuario = ContextoSeguridad.actual();
        guardar(tipo, modulo, accion, nivel,
                usuario.map(u -> u.idUsuario()).orElse(null),
                usuario.map(u -> u.nombreUsuario()).orElse("Sistema"),
                usuario.map(u -> u.rolCodigo()).orElse(null));
        marcarComoRegistrada();
    }

    /** Registra un evento a nombre de un usuario concreto (por ejemplo, durante el inicio de sesión). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarComo(Long idUsuario, String nombreUsuario, String rolCodigo, TipoEvento tipo, String modulo,
                              String accion, NivelBitacora nivel) {
        guardar(tipo, modulo, accion, nivel, idUsuario, nombreUsuario, rolCodigo);
        marcarComoRegistrada();
    }

    /** Evento generado por el propio sistema (tareas programadas). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarSistema(TipoEvento tipo, String modulo, String accion, NivelBitacora nivel) {
        guardar(tipo, modulo, accion, nivel, null, "Sistema", null);
    }

    private void guardar(TipoEvento tipo, String modulo, String accion, NivelBitacora nivel, Long idUsuario, String nombre, String rol) {
        Bitacora b = new Bitacora();
        b.setTipo(tipo);
        b.setModulo(modulo);
        b.setAccion(accion.length() > 500 ? accion.substring(0, 500) : accion);
        b.setNivel(nivel);
        b.setIdUsuario(idUsuario);
        b.setUsuarioNombre(nombre);
        b.setRolCodigo(rol);
        b.setIp(ipActual());
        bitacora.save(b);
    }

    private void marcarComoRegistrada() {
        RequestAttributes atributos = RequestContextHolder.getRequestAttributes();
        if (atributos != null) {
            atributos.setAttribute(ATRIBUTO_REGISTRADA, Boolean.TRUE, RequestAttributes.SCOPE_REQUEST);
        }
    }

    public static String ipActual() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes servlet) {
            HttpServletRequest req = servlet.getRequest();
            String reenviada = req.getHeader("X-Forwarded-For");
            String ip = reenviada != null && !reenviada.isBlank() ? reenviada.split(",")[0].trim() : req.getRemoteAddr();
            return ip != null && ip.length() > 45 ? ip.substring(0, 45) : ip;
        }
        return null;
    }
}
