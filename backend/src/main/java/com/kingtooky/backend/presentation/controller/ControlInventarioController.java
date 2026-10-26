package com.kingtooky.backend.presentation.controller;

import com.kingtooky.backend.application.dto.ControlProgramacionResponse;
import com.kingtooky.backend.application.dto.ControlRequest;
import com.kingtooky.backend.application.dto.ControlResponse;
import com.kingtooky.backend.application.dto.DetalleControlRequest;
import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.services.ControlInventarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/inventario/controles")
public class ControlInventarioController {

    private final ControlInventarioService controlService;

    public ControlInventarioController(ControlInventarioService controlService) {
        this.controlService = controlService;
    }

    @GetMapping("/programacion")
    @PreAuthorize("hasAuthority('CONTROL_INVENTARIO')")
    public ControlProgramacionResponse programacion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return controlService.programacion(fecha);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONTROL_INVENTARIO')")
    public PageResponse<ControlResponse> listar(@RequestParam(required = false) String tipo,
                                                @RequestParam(required = false) String estado,
                                                @PageableDefault(size = 20) Pageable pageable) {
        return controlService.listar(tipo, estado, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONTROL_INVENTARIO')")
    public ControlResponse obtener(@PathVariable Integer id) {
        return controlService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CONTROL_INVENTARIO')")
    public ControlResponse iniciar(@Valid @RequestBody ControlRequest request) {
        return controlService.iniciar(request);
    }

    @PostMapping("/{id}/conteos")
    @PreAuthorize("hasAuthority('CONTROL_INVENTARIO')")
    public ControlResponse conteo(@PathVariable Integer id, @Valid @RequestBody DetalleControlRequest request) {
        return controlService.registrarConteo(id, request);
    }

    @PostMapping("/{id}/cerrar")
    @PreAuthorize("hasAuthority('CONTROL_INVENTARIO')")
    public ControlResponse cerrar(@PathVariable Integer id, @RequestParam(defaultValue = "true") boolean ajustarStock) {
        return controlService.cerrar(id, ajustarStock);
    }
}