package com.kingtooky.backend.presentation.controller;

import com.kingtooky.backend.application.dto.ConsumoRequest;
import com.kingtooky.backend.application.dto.ConsumoResponse;
import com.kingtooky.backend.application.dto.MovimientoRequest;
import com.kingtooky.backend.application.dto.MovimientoResponse;
import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.services.ConsumoService;
import com.kingtooky.backend.services.MovimientoInventarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final MovimientoInventarioService movimientoService;
    private final ConsumoService consumoService;

    public InventarioController(MovimientoInventarioService movimientoService, ConsumoService consumoService) {
        this.movimientoService = movimientoService;
        this.consumoService = consumoService;
    }

    @PostMapping("/entradas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('INVENTARIO_ESCRIBIR')")
    public MovimientoResponse entrada(@Valid @RequestBody MovimientoRequest request) {
        return movimientoService.registrarEntrada(request);
    }

    @PostMapping("/salidas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('INVENTARIO_ESCRIBIR')")
    public MovimientoResponse salida(@Valid @RequestBody MovimientoRequest request) {
        return movimientoService.registrarSalida(request);
    }

    @PostMapping("/consumo")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CONSUMO_REGISTRAR')")
    public ConsumoResponse consumo(@Valid @RequestBody ConsumoRequest request) {
        return consumoService.registrar(request);
    }

    @GetMapping("/movimientos")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    public PageResponse<MovimientoResponse> movimientos(
            @RequestParam(required = false) Integer idInsumo,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @PageableDefault(size = 20) Pageable pageable) {
        return movimientoService.listar(idInsumo, tipo, desde, hasta, pageable);
    }

    @GetMapping("/kardex/{idInsumo}")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    public PageResponse<MovimientoResponse> kardex(
            @PathVariable Integer idInsumo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @PageableDefault(size = 20) Pageable pageable) {
        return movimientoService.kardex(idInsumo, desde, hasta, pageable);
    }
}