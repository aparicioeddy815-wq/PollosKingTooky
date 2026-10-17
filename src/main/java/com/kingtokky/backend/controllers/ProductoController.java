package com.kingtokky.backend.controllers;

import com.kingtokky.backend.dto.ProductoDto;
import com.kingtokky.backend.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public List<ProductoDto> listar() {
        return productoService.listarProductos();
    }

    @PostMapping
    public ProductoDto guardar(@RequestBody ProductoDto productoDto) {
        return productoService.guardarProducto(productoDto);
    }
}