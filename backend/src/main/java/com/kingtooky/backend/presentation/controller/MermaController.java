package com.kingtooky.backend.presentation.controller;

import com.kingtooky.backend.application.dto.MermaRequest;
import com.kingtooky.backend.application.dto.MermaResponse;
import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.services.MermaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/mermas")
public class MermaController {

    private final MermaService mermaService;

    public MermaController(MermaService mermaService) {
        this.mermaService = mermaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('MERMA_REGISTRAR')")
    public MermaResponse registrar(@Valid @RequestBody MermaRequest request) {
        return mermaService.registrar(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    public PageResponse<MermaResponse> listar(
            @RequestParam(required = false) Integer idInsumo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @PageableDefault(size = 20) Pageable pageable) {
        return mermaService.listar(idInsumo, desde, hasta, pageable);
    }
}