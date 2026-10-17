package com.kingtokky.backend.controllers;

import com.kingtokky.backend.dto.CategoriaDto;
import com.kingtokky.backend.services.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public List<CategoriaDto> listar() {
        return categoriaService.listarCategorias();
    }

    @PostMapping
    public CategoriaDto guardar(@RequestBody CategoriaDto categoriaDto) {
        return categoriaService.guardarCategoria(categoriaDto);
    }
}