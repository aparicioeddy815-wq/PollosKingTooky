package com.kingtooky.backend.security.application;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {
    }

    public static UsuarioPrincipal usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal) {
            return (UsuarioPrincipal) auth.getPrincipal();
        }
        return null;
    }

    public static Integer idUsuarioActual() {
        UsuarioPrincipal p = usuarioActual();
        return p != null ? p.getIdUsuario() : null;
    }
}
