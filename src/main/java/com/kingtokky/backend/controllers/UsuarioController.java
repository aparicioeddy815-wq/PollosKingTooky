package com.kingtokky.backend.controllers;

import com.kingtokky.backend.dto.UsuarioDto;
import com.kingtokky.backend.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioDto> listar() {
        return usuarioService.listarUsuarios();
    }

    @PostMapping
    public UsuarioDto guardar(@RequestBody UsuarioDto usuarioDto) {
        return usuarioService.guardarUsuario(usuarioDto);
    }
}