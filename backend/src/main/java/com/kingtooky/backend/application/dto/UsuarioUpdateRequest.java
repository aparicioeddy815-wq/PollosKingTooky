package com.kingtooky.backend.application.dto;

import jakarta.validation.constraints.*;

public class UsuarioUpdateRequest {

    @NotBlank(message = "El campo es obligatorio")
    private String nombreCompleto;

    @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
    private String contrasena;

    @NotNull(message = "El rol es obligatorio")
    private Integer idRol;

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
}