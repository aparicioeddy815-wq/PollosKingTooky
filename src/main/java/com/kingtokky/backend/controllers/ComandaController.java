package com.kingtokky.backend.controllers;

import com.kingtokky.backend.dto.ComandaDto;
import com.kingtokky.backend.services.ComandaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comandas")
@RequiredArgsConstructor
public class ComandaController {

    private final ComandaService comandaService;

    @GetMapping
    public List<ComandaDto> listar() {
        return comandaService.listarComandas();
    }

    @PostMapping
    public ComandaDto crear(@RequestBody ComandaDto comandaDto) {
        return comandaService.crearComanda(comandaDto);
    }
}