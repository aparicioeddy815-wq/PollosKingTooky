package com.kingtooky.backend.presentation.controller;

import com.kingtooky.backend.infrastructure.entity.UsuarioEntity;
import com.kingtooky.backend.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<UsuarioEntity> obtenerTodos() {
        return usuarioService.obtenerTodos();
    }

    @PostMapping
    public UsuarioEntity crearUsuario(@RequestBody UsuarioEntity usuario) {
        return usuarioService.crearUsuario(usuario);
    }
}