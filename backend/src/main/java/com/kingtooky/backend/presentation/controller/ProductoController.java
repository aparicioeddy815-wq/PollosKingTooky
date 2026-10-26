package com.kingtooky.backend.presentation.controller;

import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.application.dto.ProductoRequest;
import com.kingtooky.backend.application.dto.ProductoResponse;
import com.kingtooky.backend.application.dto.RecetaItemResponse;
import com.kingtooky.backend.application.dto.RecetaRequest;
import com.kingtooky.backend.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCTO_LEER')")
    public PageResponse<ProductoResponse> listar(@RequestParam(required = false) String q,
                                                 @RequestParam(required = false) Boolean disponible,
                                                 @PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
        return productoService.listar(q, disponible, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTO_LEER')")
    public ProductoResponse obtener(@PathVariable Integer id) {
        return productoService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PRODUCTO_ESCRIBIR')")
    public ProductoResponse crear(@Valid @RequestBody ProductoRequest request) {
        return productoService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTO_ESCRIBIR')")
    public ProductoResponse actualizar(@PathVariable Integer id, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(id, request);
    }

    @PatchMapping("/{id}/disponibilidad")
    @PreAuthorize("hasAuthority('PRODUCTO_ESCRIBIR')")
    public ProductoResponse disponibilidad(@PathVariable Integer id, @RequestParam boolean disponible) {
        return productoService.cambiarDisponibilidad(id, disponible);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PRODUCTO_ESCRIBIR')")
    public void eliminar(@PathVariable Integer id) {
        productoService.eliminar(id);
    }

    @GetMapping("/{id}/insumos")
    @PreAuthorize("hasAuthority('PRODUCTO_LEER') and hasAuthority('INVENTARIO_LEER')")
    public List<RecetaItemResponse> receta(@PathVariable Integer id) {
        return productoService.obtenerReceta(id);
    }

    @PutMapping("/{id}/insumos")
    @PreAuthorize("hasAuthority('PRODUCTO_ESCRIBIR')")
    public List<RecetaItemResponse> definirReceta(@PathVariable Integer id, @Valid @RequestBody RecetaRequest request) {
        return productoService.definirReceta(id, request.getItems());
    }
}