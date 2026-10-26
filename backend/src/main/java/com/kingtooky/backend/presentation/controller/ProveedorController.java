package com.kingtooky.backend.presentation.controller;

import com.kingtooky.backend.application.dto.EstadoRequest;
import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.application.dto.ProveedorRequest;
import com.kingtooky.backend.application.dto.ProveedorResponse;
import com.kingtooky.backend.services.ProveedorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    public PageResponse<ProveedorResponse> listar(@RequestParam(required = false) String q,
                                                  @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return proveedorService.listar(q, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    public ProveedorResponse obtener(@PathVariable Integer id) {
        return proveedorService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PROVEEDOR_GESTIONAR')")
    public ProveedorResponse crear(@Valid @RequestBody ProveedorRequest request) {
        return proveedorService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROVEEDOR_GESTIONAR')")
    public ProveedorResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ProveedorRequest request) {
        return proveedorService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('PROVEEDOR_GESTIONAR')")
    public ProveedorResponse estado(@PathVariable Integer id, @Valid @RequestBody EstadoRequest request) {
        return proveedorService.cambiarEstado(id, request.getEstado());
    }
}