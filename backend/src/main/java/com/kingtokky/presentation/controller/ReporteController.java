package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.ReporteDtos.ReporteGeneradoResponse;
import com.kingtokky.application.dto.ReporteDtos.ReporteMensualResponse;
import com.kingtokky.application.dto.ReporteDtos.RespaldoResponse;
import com.kingtokky.application.dto.ReporteDtos.ResumenArchivosResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.application.service.ReporteService;
import com.kingtokky.application.service.RespaldoService;
import com.kingtokky.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Reportes y respaldos", description = "Reportes financieros, archivo de informes y respaldos de la base de datos")
public class ReporteController {

    private final ReporteService reportes;
    private final RespaldoService respaldos;

    public ReporteController(ReporteService reportes, RespaldoService respaldos) {
        this.reportes = reportes;
        this.respaldos = respaldos;
    }

    @GetMapping("/reportes/resumen")
    @PreAuthorize("hasAuthority('REPORTE_LEER')")
    @Operation(summary = "Reporte financiero del mes (YYYY-MM): ingresos, margen, desperdicio, canales, evolución semanal y distribución")
    public ReporteMensualResponse resumen(@RequestParam(required = false) String mes) {
        return reportes.mensual(mes == null || mes.isBlank() ? YearMonth.now() : YearMonth.parse(mes));
    }

    @GetMapping("/reportes/exportar/excel")
    @PreAuthorize("hasAuthority('REPORTE_EXPORTAR')")
    @Operation(summary = "Descarga el reporte del mes en Excel")
    public ResponseEntity<org.springframework.core.io.Resource> excel(@RequestParam(required = false) String mes) {
        YearMonth periodo = mes == null || mes.isBlank() ? YearMonth.now() : YearMonth.parse(mes);
        return Descargas.de("reporte-financiero-" + periodo + ".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", reportes.excelMensual(periodo));
    }

    @GetMapping("/reportes/imprimible")
    @PreAuthorize("hasAuthority('REPORTE_EXPORTAR')")
    @Operation(summary = "Descarga el reporte del mes en PDF listo para imprimir")
    public ResponseEntity<org.springframework.core.io.Resource> imprimible(@RequestParam(required = false) String mes) {
        YearMonth periodo = mes == null || mes.isBlank() ? YearMonth.now() : YearMonth.parse(mes);
        return Descargas.de("reporte-financiero-" + periodo + ".pdf", "application/pdf", reportes.pdfMensual(periodo));
    }

    @GetMapping("/reportes/archivos")
    @PreAuthorize("hasAuthority('REPORTE_LEER')")
    @Operation(summary = "Archivo de informes oficiales (incluye los programados)")
    public PaginaResponse<ReporteGeneradoResponse> archivos(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return reportes.listarArchivos(page, size);
    }

    @GetMapping("/reportes/archivos/resumen")
    @PreAuthorize("hasAuthority('REPORTE_LEER')")
    public ResumenArchivosResponse resumenArchivos() {
        return reportes.resumenArchivos();
    }

    @PostMapping("/reportes/archivos")
    @PreAuthorize("hasAuthority('REPORTE_EXPORTAR')")
    @Operation(summary = "Genera un informe y lo archiva. tipo: VENTAS_MENSUAL, INVENTARIO, AUDITORIA o RESUMEN_DIARIO; formato: PDF o XLSX")
    public ResponseEntity<ReporteGeneradoResponse> generar(@RequestParam String tipo, @RequestParam(defaultValue = "PDF") String formato,
                                                           @RequestParam(required = false) String periodo, @AuthenticationPrincipal UsuarioAutenticado u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportes.generar(tipo.toUpperCase(), formato, periodo, u.idUsuario(), false));
    }

    @GetMapping("/reportes/archivos/{id}/descargar")
    @PreAuthorize("hasAuthority('REPORTE_LEER')")
    public ResponseEntity<org.springframework.core.io.Resource> descargar(@PathVariable Long id) {
        return Descargas.de(reportes.descargar(id));
    }

    // ------------------------------------------------------------------ respaldos
    @GetMapping("/respaldos")
    @PreAuthorize("hasAuthority('RESPALDO_GESTIONAR')")
    @Operation(summary = "Historial de respaldos (automáticos cada 12 horas y manuales)")
    public PaginaResponse<RespaldoResponse> respaldos(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return respaldos.listar(page, size);
    }

    @PostMapping("/respaldos")
    @PreAuthorize("hasAuthority('RESPALDO_GESTIONAR')")
    @Operation(summary = "Ejecuta un respaldo ahora mismo")
    public ResponseEntity<RespaldoResponse> respaldar() {
        return ResponseEntity.status(HttpStatus.CREATED).body(respaldos.ejecutar(false));
    }

    @GetMapping("/respaldos/{id}/descargar")
    @PreAuthorize("hasAuthority('RESPALDO_GESTIONAR')")
    public ResponseEntity<org.springframework.core.io.Resource> descargarRespaldo(@PathVariable Long id) {
        return Descargas.de(respaldos.descargar(id));
    }
}
