package com.kingtooky.backend.services;

import com.kingtooky.backend.infrastructure.entity.UsuarioEntity;
import com.kingtooky.backend.repository.UsuarioJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioJpaRepository usuarioRepository;

    public List<UsuarioEntity> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public UsuarioEntity crearUsuario(UsuarioEntity usuario) {
        return usuarioRepository.save(usuario);
    }
}