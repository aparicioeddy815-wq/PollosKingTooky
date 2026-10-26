package com.kingtooky.backend.application.dto;

import jakarta.validation.constraints.NotBlank;

public class EstadoRequest {

    @NotBlank(message = "El estado es obligatorio")
    private String estado;

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}