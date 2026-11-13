package com.kingtokky.infrastructure.security;

import java.util.List;

/** Datos del usuario que viaja dentro del token JWT y queda disponible en cada petición. */
public record UsuarioAutenticado(Long idUsuario, String nombreUsuario, String rolCodigo, List<String> permisos) {

    public boolean tienePermiso(String permiso) {
        return permisos.contains(permiso);
    }
}
