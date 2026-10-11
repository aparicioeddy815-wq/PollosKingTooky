package com.kingtooky.backend.presentation.controller;

import com.kingtooky.backend.application.dto.AlertaResponse;
import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.services.AlertaStockService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/alertas")
public class AlertaStockController {

    private final AlertaStockService alertaService;

    public AlertaStockController(AlertaStockService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ALERTA_LEER')")
    public PageResponse<AlertaResponse> listar(@RequestParam(defaultValue = "ACTIVA") String estado,
                                               @PageableDefault(size = 20) Pageable pageable) {
        return alertaService.listar(estado, pageable);
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasAuthority('ALERTA_LEER')")
    public Map<String, Long> resumen() {
        return Map.of("activas", alertaService.contarActivas());
    }

    @PatchMapping("/{id}/atender")
    @PreAuthorize("hasAuthority('INVENTARIO_ESCRIBIR')")
    public AlertaResponse atender(@PathVariable Integer id) {
        return alertaService.atender(id);
    }

    @PostMapping("/evaluar")
    @PreAuthorize("hasAuthority('INVENTARIO_ESCRIBIR')")
    public Map<String, String> evaluar() {
        alertaService.barridoProgramado();
        return Map.of("mensaje", "Barrido de alertas ejecutado");
    }
}