package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.DashboardDtos.DashboardAdminResponse;
import com.kingtokky.application.dto.DashboardDtos.DashboardGerenteResponse;
import com.kingtokky.application.service.DashboardService;
import com.kingtokky.application.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboards", description = "Cifras del gerente y del administrador")
public class DashboardController {

    private final DashboardService dashboard;
    private final ReporteService reportes;

    public DashboardController(DashboardService dashboard, ReporteService reportes) {
        this.dashboard = dashboard;
        this.reportes = reportes;
    }

    @GetMapping("/gerente")
    @PreAuthorize("hasAuthority('PANEL_GERENTE')")
    @Operation(summary = "Dashboard del gerente: ventas, pedidos, stock crítico, ventas por categoría, quejas y entrevistas")
    public DashboardGerenteResponse gerente(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return dashboard.gerente(fecha);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('PANEL_ADMIN')")
    @Operation(summary = "Dashboard del administrador: ventas totales, margen, ritmo por hora, top del menú, operaciones y bitácora reciente")
    public DashboardAdminResponse admin(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return dashboard.admin(fecha);
    }

    @GetMapping("/admin/exportar")
    @PreAuthorize("hasAuthority('PANEL_ADMIN')")
    @Operation(summary = "Descarga el resumen diario en PDF")
    public ResponseEntity<org.springframework.core.io.Resource> exportar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        return Descargas.de("resumen-diario-" + dia + ".pdf", "application/pdf", reportes.resumenDiarioPdf(dia));
    }
}
