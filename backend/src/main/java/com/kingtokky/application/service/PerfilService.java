package com.kingtokky.application.service;

import com.kingtokky.application.dto.AuthDtos.CambiarContrasenaRequest;
import com.kingtokky.application.dto.AuthDtos.PerfilRequest;
import com.kingtokky.application.dto.AuthDtos.PerfilResponse;
import com.kingtokky.application.dto.comun.MensajeResponse;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.seguridad.Usuario;
import com.kingtokky.domain.port.AlmacenArchivos;
import com.kingtokky.domain.repository.AccesoUsuarioRepository;
import com.kingtokky.domain.repository.UsuarioRepository;
import com.kingtokky.infrastructure.config.KingTokkyProperties;
import com.kingtokky.presentation.exception.ApiException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional(readOnly = true)
public class PerfilService {

    private static final Set<String> TIPOS_IMAGEN = Set.of("image/png", "image/jpeg", "image/webp");

    private final UsuarioRepository usuarios;
    private final AccesoUsuarioRepository accesos;
    private final PasswordEncoder encoder;
    private final AlmacenArchivos almacen;
    private final AuditoriaService auditoria;
    private final KingTokkyProperties propiedades;

    public PerfilService(UsuarioRepository usuarios, AccesoUsuarioRepository accesos, PasswordEncoder encoder,
                         AlmacenArchivos almacen, AuditoriaService auditoria, KingTokkyProperties propiedades) {
        this.usuarios = usuarios;
        this.accesos = accesos;
        this.encoder = encoder;
        this.almacen = almacen;
        this.auditoria = auditoria;
        this.propiedades = propiedades;
    }

    public PerfilResponse obtener(Long idUsuario) {
        return aRespuesta(buscar(idUsuario));
    }

    @Transactional
    public PerfilResponse actualizar(Long idUsuario, PerfilRequest req) {
        Usuario u = buscar(idUsuario);
        String correo = req.correo().trim().toLowerCase();
        if (!correo.equalsIgnoreCase(u.getCorreo()) && usuarios.existsByCorreoIgnoreCase(correo)) {
            throw ApiException.conflicto("Ya existe una cuenta con ese correo");
        }
        u.setNombreCompleto(req.nombreCompleto().trim());
        u.setCorreo(correo);
        if (req.fotoUrl() != null) u.setFotoUrl(req.fotoUrl().isBlank() ? null : req.fotoUrl().trim());
        return aRespuesta(u);
    }

    @Transactional
    public MensajeResponse cambiarContrasena(Long idUsuario, CambiarContrasenaRequest req) {
        Usuario u = buscar(idUsuario);
        if (u.getContrasenaHash() == null || !encoder.matches(req.contrasenaActual(), u.getContrasenaHash())) {
            throw ApiException.solicitudInvalida("La contraseña actual no es correcta");
        }
        u.setContrasenaHash(encoder.encode(req.nuevaContrasena()));
        auditoria.registrar(TipoEvento.SEGURIDAD, "Perfil", "Cambió su contraseña", NivelBitacora.INFO);
        return new MensajeResponse("Tu contraseña fue actualizada");
    }

    @Transactional
    public PerfilResponse subirFoto(Long idUsuario, MultipartFile archivo) {
        if (archivo.isEmpty() || archivo.getContentType() == null || !TIPOS_IMAGEN.contains(archivo.getContentType())) {
            throw ApiException.solicitudInvalida("La foto debe ser una imagen PNG, JPG o WEBP");
        }
        Usuario u = buscar(idUsuario);
        try {
            String ruta = almacen.guardar("fotos", archivo.getOriginalFilename(), archivo.getInputStream());
            String nombre = ruta.substring(ruta.lastIndexOf('/') + 1);
            u.setFotoUrl(propiedades.almacenamiento().urlPublicaBase() + "/api/archivos/fotos/" + nombre);
        } catch (IOException e) {
            throw ApiException.reglaDeNegocio("No se pudo guardar la foto. Intenta de nuevo");
        }
        return aRespuesta(u);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado"));
    }

    private PerfilResponse aRespuesta(Usuario u) {
        long ultimas4Semanas = accesos.countByUsuario_IdUsuarioAndFechaHoraAfter(u.getIdUsuario(), LocalDateTime.now().minusDays(28));
        double porSemana = Math.round(ultimas4Semanas / 4.0 * 10) / 10.0;
        return new PerfilResponse(u.getIdUsuario(), u.getNombreUsuario(), u.getNombreCompleto(), u.getCorreo(), u.getFotoUrl(),
                u.getRol().getNombre(), u.getFechaIngreso(), u.getUltimoAcceso(), u.getTotalAccesos(), porSemana);
    }
}
