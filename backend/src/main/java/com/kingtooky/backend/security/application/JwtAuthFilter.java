package com.kingtooky.backend.security.application;

import com.kingtooky.backend.infrastructure.entity.PermisoEntity;
import com.kingtooky.backend.infrastructure.entity.UsuarioEntity;
import com.kingtooky.backend.repository.UsuarioJpaRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioJpaRepository usuarioRepository;
    private final TokenBlacklistService blacklist;

    public JwtAuthFilter(JwtUtil jwtUtil, UsuarioJpaRepository usuarioRepository, TokenBlacklistService blacklist) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepository = usuarioRepository;
        this.blacklist = blacklist;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.isAccessToken(token) && !blacklist.estaInvalidado(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                Optional<UsuarioEntity> opt = usuarioRepository.findByNombreUsuario(jwtUtil.extractUsername(token));
                if (opt.isPresent() && Integer.valueOf(1).equals(opt.get().getEstado())) {
                    UsuarioEntity usuario = opt.get();
                    String rol = usuario.getRol().getNombre();
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + rol));
                    for (PermisoEntity p : usuario.getRol().getPermisos()) {
                        authorities.add(new SimpleGrantedAuthority(p.getCodigo()));
                    }
                    UsuarioPrincipal principal = new UsuarioPrincipal(usuario.getIdUsuario(), usuario.getNombreUsuario(), rol);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(request, response);
    }
}