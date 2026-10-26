package com.kingtokky.backend.services;

import com.kingtokky.backend.dto.LoginRequest;
import com.kingtokky.backend.dto.LoginResponse;
import com.kingtokky.backend.dto.UsuarioDto;
import com.kingtokky.backend.entities.Usuario;
import com.kingtokky.backend.repositories.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // Listar todos los usuarios y transformarlos a DTO
    public List<UsuarioDto> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioDto::new)
                .collect(Collectors.toList());
    }

    // Guardar un usuario nuevo
    public UsuarioDto guardarUsuario(UsuarioDto usuarioDto) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(usuarioDto.getIdUsuario());
        usuario.setNombreCompleto(usuarioDto.getNombreCompleto());
        usuario.setNombreUsuario(usuarioDto.getNombreUsuario());
        usuario.setCorreoElectronico(usuarioDto.getCorreoElectronico());
        usuario.setContrasenaHash(usuarioDto.getContrasenaHash());
        usuario.setFotoPerfil(usuarioDto.getFotoPerfil());
        usuario.setIdRol(usuarioDto.getIdRol());
        usuario.setFechaIngreso(usuarioDto.getFechaIngreso());
        usuario.setEstado(usuarioDto.getEstado());

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return new UsuarioDto(usuarioGuardado);
    }

    // Metodo para autenticar y validar contra la Base de Datos
    public LoginResponse autenticar(LoginRequest request) {
        System.out.println(">>> Intentando autenticar usuario: [" + request.getUsername() + "]");

        var usuarioOpt = usuarioRepository.findByNombreUsuario(request.getUsername());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            System.out.println(">>> Usuario encontrado en BD: " + usuario.getNombreUsuario());
            System.out.println(">>> Password en BD: [" + usuario.getContrasenaHash() + "] vs Ingresado: [" + request.getPassword() + "]");

            if (usuario.getContrasenaHash().equals(request.getPassword())) {
                String rolStr = String.valueOf(usuario.getIdRol());
                return new LoginResponse(usuario.getNombreUsuario(), rolStr);
            } else {
                System.out.println(">>> LA CONTRASEÑA NO COINCIDE");
            }
        } else {
            System.out.println(">>> NO SE ENCONTRÓ EL USUARIO EN LA BD");
        }

        return null;
    }
}