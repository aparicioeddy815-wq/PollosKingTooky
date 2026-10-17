package com.kingtokky.backend.controllers;

import com.kingtokky.backend.dto.InventarioDto;
import com.kingtokky.backend.services.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public List<InventarioDto> listar() {
        return inventarioService.listarInsumos();
    }

    @PostMapping
    public InventarioDto guardar(@RequestBody InventarioDto inventarioDto) {
        return inventarioService.guardarInsumo(inventarioDto);
    }
}