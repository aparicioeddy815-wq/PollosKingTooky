package com.kingtooky.backend.application.dto;

import jakarta.validation.constraints.*;

public class ControlRequest {

    @NotNull(message = "El tipo es obligatorio")
    @Pattern(regexp = "SEMANAL|QUINCENAL", message = "El tipo debe ser SEMANAL o QUINCENAL")
    private String tipo;

    private String observaciones;

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
