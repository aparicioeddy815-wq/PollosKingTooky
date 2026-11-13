package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.BitacoraDtos.BitacoraResponse;
import com.kingtokky.application.dto.BitacoraDtos.ResumenBitacoraResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.application.service.BitacoraService;
import com.kingtokky.application.service.ExportacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bitacora")
@Tag(name = "Bitácora de auditoría", description = "Registro permanente de lo que ocurre en el sistema")
public class BitacoraController {

    private final BitacoraService bitacora;
    private final ExportacionService exportacion;

    public BitacoraController(BitacoraService bitacora, ExportacionService exportacion) {
        this.bitacora = bitacora;
        this.exportacion = exportacion;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BITACORA_LEER')")
    @Operation(summary = "Eventos con filtros: categoria (TODOS, SEGURIDAD, ANULACIONES, PERMISOS), actor (TODOS, GERENTE, ADMIN, DESPACHO, CLIENTE), fechas y texto")
    public PaginaResponse<BitacoraResponse> listar(@RequestParam(defaultValue = "TODOS") String categoria, @RequestParam(defaultValue = "TODOS") String actor,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                                   @RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "25") int size) {
        return bitacora.listar(categoria, actor, desde, hasta, q, page, size);
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasAuthority('BITACORA_LEER')")
    @Operation(summary = "Tarjetas: eventos de hoy, accesos y sesiones, suspensiones y ajustes de stock")
    public ResumenBitacoraResponse resumen() {
        return bitacora.resumen();
    }

    @GetMapping("/exportar")
    @PreAuthorize("hasAuthority('BITACORA_EXPORTAR')")
    @Operation(summary = "Exporta los eventos filtrados a csv o pdf (hasta 5.000 registros)")
    public ResponseEntity<org.springframework.core.io.Resource> exportar(@RequestParam(defaultValue = "csv") String formato,
                                                                         @RequestParam(defaultValue = "TODOS") String categoria,
                                                                         @RequestParam(defaultValue = "TODOS") String actor,
                                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                                                         @RequestParam(required = false) String q) {
        List<List<Object>> filas = bitacora.listarParaExportar(categoria, actor, desde, hasta, q).stream().map(b -> List.<Object>of(
                b.fechaHora().toString().replace('T', ' ').substring(0, 19), b.tipo().name(), b.modulo(), b.usuario(),
                b.rolCodigo() == null ? "" : b.rolCodigo(), b.accion(), b.nivel().name(), b.ip() == null ? "" : b.ip())).toList();
        return Descargas.de(exportacion.exportar(formato, "bitacora", "Bitácora de auditoría", List.of("Fecha y hora", "Tipo", "Módulo", "Usuario",
                "Rol", "Acción", "Nivel", "IP"), filas));
    }
}
