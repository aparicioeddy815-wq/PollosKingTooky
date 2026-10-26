package com.kingtooky.backend.services;

import com.kingtooky.backend.application.dto.AuthRequest;
import com.kingtooky.backend.application.dto.AuthResponse;
import com.kingtooky.backend.infrastructure.entity.PermisoEntity;
import com.kingtooky.backend.infrastructure.entity.UsuarioEntity;
import com.kingtooky.backend.presentation.exception.RecursoNoEncontradoException;
import com.kingtooky.backend.repository.UsuarioJpaRepository;
import com.kingtooky.backend.security.application.JwtUtil;
import com.kingtooky.backend.security.application.SecurityUtil;
import com.kingtooky.backend.security.application.TokenBlacklistService;
import com.kingtooky.backend.security.application.UsuarioPrincipal;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService blacklist;
    private final AuditoriaService auditoriaService;

    public AuthService(UsuarioJpaRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil,
                       TokenBlacklistService blacklist, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.blacklist = blacklist;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        UsuarioEntity usuario = usuarioRepository.findByNombreUsuario(request.getNombreUsuario().trim())
                .orElseThrow(() -> new BadCredentialsException("Usuario o contrasena incorrectos"));

        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            throw new BadCredentialsException("Usuario o contrasena incorrectos");
        }
        if (!Integer.valueOf(1).equals(usuario.getEstado())) {
            throw new DisabledException("El usuario esta desactivado");
        }

        auditoriaService.registrar(usuario.getIdUsuario(), "LOGIN", "usuario", usuario.getIdUsuario(),
                "Inicio de sesion de " + usuario.getNombreUsuario());
        return construirRespuesta(usuario, true);
    }

    @Transactional(readOnly = true)
    public AuthResponse refrescar(String refreshToken) {
        if (!jwtUtil.isRefreshToken(refreshToken) || blacklist.estaInvalidado(refreshToken)) {
            throw new BadCredentialsException("Refresh token invalido o expirado");
        }
        UsuarioEntity usuario = usuarioRepository.findByNombreUsuario(jwtUtil.extractUsername(refreshToken))
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
        if (!Integer.valueOf(1).equals(usuario.getEstado())) {
            throw new DisabledException("El usuario esta desactivado");
        }
        return construirRespuesta(usuario, true);
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        if (accessToken != null && jwtUtil.validateToken(accessToken)) {
            blacklist.invalidar(accessToken, jwtUtil.extractExpiration(accessToken));
        }
        if (refreshToken != null && jwtUtil.validateToken(refreshToken)) {
            blacklist.invalidar(refreshToken, jwtUtil.extractExpiration(refreshToken));
        }
        UsuarioPrincipal p = SecurityUtil.usuarioActual();
        if (p != null) {
            auditoriaService.registrar(p.getIdUsuario(), "LOGOUT", "usuario", p.getIdUsuario(),
                    "Cierre de sesion de " + p.getNombreUsuario());
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse perfilActual() {
        Integer id = SecurityUtil.idUsuarioActual();
        UsuarioEntity usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return construirRespuesta(usuario, false);
    }

    private AuthResponse construirRespuesta(UsuarioEntity usuario, boolean conTokens) {
        AuthResponse r = new AuthResponse();
        if (conTokens) {
            r.setAccessToken(jwtUtil.generateToken(usuario.getNombreUsuario()));
            r.setRefreshToken(jwtUtil.generateRefreshToken(usuario.getNombreUsuario()));
        }
        r.setIdUsuario(usuario.getIdUsuario());
        r.setNombreUsuario(usuario.getNombreUsuario());
        r.setNombreCompleto(usuario.getNombreCompleto());
        r.setRol(usuario.getRol().getNombre());
        List<String> permisos = usuario.getRol().getPermisos().stream().map(PermisoEntity::getCodigo).sorted().toList();
        r.setPermisos(permisos);
        return r;
    }
}