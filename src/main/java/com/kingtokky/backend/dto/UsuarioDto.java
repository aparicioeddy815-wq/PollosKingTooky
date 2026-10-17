package com.kingtokky.backend.dto;

import com.kingtokky.backend.entities.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDto {

    private String idUsuario;
    private String nombreCompleto;
    private String nombreUsuario;
    private String correoElectronico;
    private String contrasenaHash; // <--- Agrégalo aquí
    private String fotoPerfil;
    private String idRol;
    private LocalDateTime fechaIngreso;
    private String estado;

    public UsuarioDto(Usuario usuario) {
        this.idUsuario = usuario.getIdUsuario();
        this.nombreCompleto = usuario.getNombreCompleto();
        this.nombreUsuario = usuario.getNombreUsuario();
        this.correoElectronico = usuario.getCorreoElectronico();
        this.contrasenaHash = usuario.getContrasenaHash(); // <--- Y aquí
        this.fotoPerfil = usuario.getFotoPerfil();
        this.idRol = usuario.getIdRol();
        this.fechaIngreso = usuario.getFechaIngreso();
        this.estado = usuario.getEstado();
    }
}