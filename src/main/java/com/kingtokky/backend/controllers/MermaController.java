package com.kingtokky.backend.controllers;

import com.kingtokky.backend.dto.MermaDto;
import com.kingtokky.backend.services.MermaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mermas")
@RequiredArgsConstructor
public class MermaController {

    private final MermaService mermaService;

    @GetMapping
    public List<MermaDto> listar() {
        return mermaService.listarMermas();
    }

    @PostMapping
    public MermaDto guardar(@RequestBody MermaDto mermaDto) {
        return mermaService.registrarMerma(mermaDto);
    }
}