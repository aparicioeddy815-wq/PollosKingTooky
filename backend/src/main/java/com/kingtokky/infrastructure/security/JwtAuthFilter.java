package com.kingtokky.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Lee el token "Authorization: Bearer ..." y deja al usuario autenticado en el contexto de seguridad. */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = extraerToken(request);
        if (token != null) {
            jwtService.validar(token).ifPresent(usuario -> {
                List<GrantedAuthority> autoridades = new ArrayList<>();
                autoridades.add(new SimpleGrantedAuthority(usuario.rolCodigo()));
                usuario.permisos().forEach(p -> autoridades.add(new SimpleGrantedAuthority(p)));
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(usuario, null, autoridades));
            });
        }
        chain.doFilter(request, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String cabecera = request.getHeader("Authorization");
        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            return cabecera.substring(7);
        }
        // EventSource (SSE) del navegador no permite cabeceras: en ese canal el token viaja en la URL
        if (request.getRequestURI().startsWith("/api/stream/")) {
            return request.getParameter("access_token");
        }
        return null;
    }
}
