package com.kingtooky.backend.presentation.controller;

import com.kingtooky.backend.application.dto.EstadoRequest;
import com.kingtooky.backend.application.dto.InsumoRequest;
import com.kingtooky.backend.application.dto.InsumoResponse;
import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.services.InsumoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insumos")
public class InsumoController {

    private final InsumoService insumoService;

    public InsumoController(InsumoService insumoService) {
        this.insumoService = insumoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    public PageResponse<InsumoResponse> listar(@RequestParam(required = false) String q,
                                               @RequestParam(required = false) String tipo,
                                               @RequestParam(required = false) String estado,
                                               @RequestParam(defaultValue = "false") boolean bajoStock,
                                               @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return insumoService.listar(q, tipo, estado, bajoStock, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    public InsumoResponse obtener(@PathVariable Integer id) {
        return insumoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('INVENTARIO_ESCRIBIR')")
    public InsumoResponse crear(@Valid @RequestBody InsumoRequest request) {
        return insumoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTARIO_ESCRIBIR')")
    public InsumoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody InsumoRequest request) {
        return insumoService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('INVENTARIO_ESCRIBIR')")
    public InsumoResponse cambiarEstado(@PathVariable Integer id, @Valid @RequestBody EstadoRequest request) {
        return insumoService.cambiarEstado(id, request.getEstado());
    }
}