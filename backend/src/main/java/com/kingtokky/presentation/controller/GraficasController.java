package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.GraficasDtos.GraficasInventarioResponse;
import com.kingtokky.application.dto.GraficasDtos.VentaDia;
import com.kingtokky.application.service.GraficasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/graficas")
@Tag(name = "Gráficas", description = "Series listas para dibujar gráficas")
public class GraficasController {

    private final GraficasService graficas;

    public GraficasController(GraficasService graficas) {
        this.graficas = graficas;
    }

    @GetMapping("/inventario")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    @Operation(summary = "Valor por categoría, estado del stock, mermas por motivo, movimientos diarios y menor cobertura")
    public GraficasInventarioResponse inventario(@RequestParam(defaultValue = "14") int dias) {
        return graficas.inventario(dias);
    }

    @GetMapping("/ventas")
    @PreAuthorize("hasAnyAuthority('PANEL_GERENTE','PANEL_ADMIN')")
    @Operation(summary = "Ventas por día de los últimos N días (3 a 60)")
    public List<VentaDia> ventas(@RequestParam(defaultValue = "7") int dias) {
        return graficas.ventas(dias);
    }
}