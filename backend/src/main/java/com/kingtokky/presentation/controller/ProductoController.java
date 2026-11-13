package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.ProductoDtos.CategoriaProductoResponse;
import com.kingtokky.application.dto.ProductoDtos.ProductoRequest;
import com.kingtokky.application.dto.ProductoDtos.ProductoResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.application.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Menú y productos", description = "Gestión de productos, precios y disponibilidad")
public class ProductoController {

    private final ProductoService productos;

    public ProductoController(ProductoService productos) {
        this.productos = productos;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCTO_LEER')")
    @Operation(summary = "Lista paginada de productos con filtros por nombre, categoría y disponibilidad")
    public PaginaResponse<ProductoResponse> listar(@RequestParam(required = false) String q, @RequestParam(required = false) String categoria,
                                                   @RequestParam(required = false) Boolean disponible,
                                                   @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return productos.listar(q, categoria, disponible, page, size);
    }

    @GetMapping("/categorias")
    @PreAuthorize("hasAuthority('PRODUCTO_LEER')")
    public List<CategoriaProductoResponse> categorias() {
        return productos.listarCategorias();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTO_LEER')")
    public ProductoResponse obtener(@PathVariable Long id) {
        return productos.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCTO_ESCRIBIR')")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productos.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTO_ESCRIBIR')")
    @Operation(summary = "Edita un producto; si cambia el precio queda como «ajuste de precio» en la bitácora")
    public ProductoResponse actualizar(@PathVariable Long id, @Valid @RequestBody ProductoRequest req) {
        return productos.actualizar(id, req);
    }

    @PatchMapping("/{id}/disponibilidad")
    @PreAuthorize("hasAuthority('PRODUCTO_ESCRIBIR')")
    @Operation(summary = "Marca un producto como disponible o agotado")
    public ProductoResponse disponibilidad(@PathVariable Long id, @RequestParam boolean disponible) {
        return productos.cambiarDisponibilidad(id, disponible);
    }

    @PatchMapping("/{id}/publicacion")
    @PreAuthorize("hasAuthority('PRODUCTO_ESCRIBIR')")
    @Operation(summary = "Publica o retira un producto del menú de PedidosYA")
    public ProductoResponse publicacion(@PathVariable Long id, @RequestParam boolean publicado) {
        return productos.cambiarPublicacion(id, publicado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCTO_ESCRIBIR')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productos.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
