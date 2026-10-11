package com.kingtooky.backend.application.dto;

import jakarta.validation.constraints.*;

public class ProveedorRequest {

    @NotBlank(message = "El campo es obligatorio")
    private String nombre;

    private String telefono;

    @Email(message = "Correo invalido")
    private String correo;

    private String direccion;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}