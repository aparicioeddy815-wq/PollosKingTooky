package com.kingtokky.infrastructure.audit;

import com.kingtokky.application.service.AuditoriaService;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.infrastructure.security.ContextoSeguridad;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor de auditoría: toda operación que modifica datos y termina bien queda en la bitácora con
 * fecha, tipo, módulo, usuario y acción. Si un servicio ya registró un evento más descriptivo, no se duplica.
 */
@Component
public class AuditoriaInterceptor implements HandlerInterceptor {

    private static final Set<String> METODOS_QUE_MODIFICAN = Set.of("POST", "PUT", "PATCH", "DELETE");

    private static final Map<String, String> MODULOS = Map.ofEntries(
            Map.entry("pedidos", "Despacho"), Map.entry("reservas", "Reservas"), Map.entry("productos", "Menú"),
            Map.entry("inventario", "Inventario"), Map.entry("quejas", "Quejas"), Map.entry("personal", "Roles y personal"),
            Map.entry("roles", "Roles y personal"), Map.entry("entrevistas", "Entrevistas"), Map.entry("postulantes", "Entrevistas"),
            Map.entry("entrevistados", "Entrevistas"), Map.entry("usuarios", "Usuarios"), Map.entry("perfil", "Perfil"),
            Map.entry("cliente", "Cliente"), Map.entry("caja", "Caja"), Map.entry("reportes", "Reportes"), Map.entry("respaldos", "Respaldo"));

    private final AuditoriaService auditoria;

    public AuditoriaInterceptor(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        String metodo = request.getMethod();
        String ruta = request.getRequestURI();
        boolean exitosa = response.getStatus() >= 200 && response.getStatus() < 300;
        if (!METODOS_QUE_MODIFICAN.contains(metodo) || !exitosa || ex != null) return;
        if (ruta.startsWith("/api/auth/") || ruta.endsWith("/timbre")) return;
        if (request.getAttribute(AuditoriaService.ATRIBUTO_REGISTRADA) != null) return;
        if (ContextoSeguridad.actual().isEmpty()) return;

        String[] partes = ruta.split("/");
        String clave = partes.length > 2 ? partes[2] : "";
        String modulo = MODULOS.getOrDefault(clave, "Sistema");
        TipoEvento tipo = switch (metodo) {
            case "POST" -> TipoEvento.CREACION;
            case "DELETE" -> TipoEvento.ELIMINACION;
            default -> TipoEvento.MODIFICACION;
        };
        String verbo = switch (tipo) {
            case CREACION -> "Registró información";
            case ELIMINACION -> "Eliminó información";
            default -> "Modificó información";
        };
        try {
            auditoria.registrar(tipo, modulo, verbo + " en " + modulo + " (" + metodo + " " + ruta + ")", NivelBitacora.INFO);
        } catch (RuntimeException e) {
            // La auditoría nunca debe romper la respuesta al usuario
        }
    }
}
