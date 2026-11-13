package com.kingtokky.application.service;

import com.kingtokky.application.dto.BitacoraDtos.BitacoraResponse;
import com.kingtokky.application.dto.BitacoraDtos.ResumenBitacoraResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.domain.model.auditoria.Bitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.personal.EstadoEmpleado;
import com.kingtokky.domain.repository.BitacoraRepository;
import com.kingtokky.domain.repository.EmpleadoRepository;
import com.kingtokky.domain.repository.RefreshTokenRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta de la bitácora de auditoría con filtros por categoría, colaborador, fechas y texto. */
@Service
@Transactional(readOnly = true)
public class BitacoraService {

    private static final int LIMITE_EXPORTACION = 5000;

    private final BitacoraRepository bitacora;
    private final EmpleadoRepository empleados;
    private final RefreshTokenRepository refreshTokens;

    public BitacoraService(BitacoraRepository bitacora, EmpleadoRepository empleados, RefreshTokenRepository refreshTokens) {
        this.bitacora = bitacora;
        this.empleados = empleados;
        this.refreshTokens = refreshTokens;
    }

    /**
     * @param categoria TODOS, SEGURIDAD, ANULACIONES o PERMISOS (los filtros de la pantalla)
     * @param actor     TODOS, GERENTE, ADMIN, DESPACHO o CLIENTE
     */
    public PaginaResponse<BitacoraResponse> listar(String categoria, String actor, LocalDate desde, LocalDate hasta, String q, int pagina, int tamano) {
        var tipos = tiposDe(categoria);
        String rol = rolDe(actor);
        var page = bitacora.filtrar(inicio(desde), fin(hasta), tipos.isEmpty(), tipos.isEmpty() ? List.of(TipoEvento.SISTEMA) : tipos,
                rol.isEmpty(), rol, patron(q), PageRequest.of(pagina, Math.min(tamano, 200)));
        return PaginaResponse.de(page, this::aRespuesta);
    }

    public List<BitacoraResponse> listarParaExportar(String categoria, String actor, LocalDate desde, LocalDate hasta, String q) {
        var tipos = tiposDe(categoria);
        String rol = rolDe(actor);
        return bitacora.filtrarTodas(inicio(desde), fin(hasta), tipos.isEmpty(), tipos.isEmpty() ? List.of(TipoEvento.SISTEMA) : tipos,
                rol.isEmpty(), rol, patron(q), PageRequest.of(0, LIMITE_EXPORTACION)).stream().map(this::aRespuesta).toList();
    }

    public ResumenBitacoraResponse resumen() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = inicio.plusDays(1);
        return new ResumenBitacoraResponse(
                bitacora.countByFechaHoraBetween(inicio, fin),
                bitacora.countByTipoAndFechaHoraBetween(TipoEvento.INICIO_SESION, inicio, fin),
                refreshTokens.contarSesionesVivas(LocalDateTime.now(), LocalDateTime.now().minusMinutes(30)),
                empleados.countByEstado(EstadoEmpleado.SUSPENDIDO),
                bitacora.countByTipoAndFechaHoraBetween(TipoEvento.AJUSTE_STOCK, inicio, fin),
                bitacora.countByTipoAndFechaHoraBetween(TipoEvento.SEGURIDAD, inicio, fin));
    }

    public List<BitacoraResponse> recientes() {
        return bitacora.findTop8ByOrderByFechaHoraDescIdDesc().stream().map(this::aRespuesta).toList();
    }

    public BitacoraResponse aRespuesta(Bitacora b) {
        return new BitacoraResponse(b.getId(), b.getFechaHora(), b.getTipo(), b.getModulo(), b.getUsuarioNombre(), b.getRolCodigo(), b.getAccion(), b.getNivel(), b.getIp());
    }

    private List<TipoEvento> tiposDe(String categoria) {
        if (categoria == null) return List.of();
        return switch (categoria.toUpperCase(Locale.ROOT)) {
            case "SEGURIDAD" -> List.of(TipoEvento.SEGURIDAD, TipoEvento.SUSPENSION);
            case "ANULACIONES" -> List.of(TipoEvento.ANULACION_COMANDA);
            case "PERMISOS" -> List.of(TipoEvento.PERMISOS);
            default -> Arrays.stream(TipoEvento.values()).filter(t -> categoria.equalsIgnoreCase(t.name())).toList();
        };
    }

    private String rolDe(String actor) {
        if (actor == null) return "";
        return switch (actor.toUpperCase(Locale.ROOT)) {
            case "GERENTE" -> "ROL-GERENTE";
            case "ADMIN", "ADMINISTRADOR" -> "ROL-ADMIN";
            case "DESPACHO" -> "ROL-DESPACHO";
            case "CLIENTE" -> "ROL-CLIENTE";
            default -> "";
        };
    }

    private String patron(String q) {
        return q == null || q.isBlank() ? "" : "%" + q.trim().toLowerCase() + "%";
    }

    private LocalDateTime inicio(LocalDate desde) {
        return (desde != null ? desde : LocalDate.now().minusDays(30)).atStartOfDay();
    }

    private LocalDateTime fin(LocalDate hasta) {
        return (hasta != null ? hasta : LocalDate.now()).plusDays(1).atStartOfDay();
    }
}
