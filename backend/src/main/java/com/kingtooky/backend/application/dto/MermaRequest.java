package com.kingtooky.backend.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class MermaRequest {

    @NotNull(message = "El campo es obligatorio")
    private Integer idInsumo;

    @NotNull(message = "El campo es obligatorio")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    public Integer getIdInsumo() { return idInsumo; }
    public void setIdInsumo(Integer idInsumo) { this.idInsumo = idInsumo; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}