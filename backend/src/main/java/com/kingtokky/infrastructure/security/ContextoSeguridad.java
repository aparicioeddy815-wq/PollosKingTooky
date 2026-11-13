package com.kingtokky.infrastructure.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Acceso cómodo al usuario autenticado desde cualquier servicio. */
public final class ContextoSeguridad {

    private ContextoSeguridad() {}

    public static Optional<UsuarioAutenticado> actual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return Optional.of(usuario);
        }
        return Optional.empty();
    }
}
