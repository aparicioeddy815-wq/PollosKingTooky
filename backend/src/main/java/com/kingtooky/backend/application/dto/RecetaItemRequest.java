package com.kingtooky.backend.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class RecetaItemRequest {

    @NotNull(message = "El campo es obligatorio")
    private Integer idInsumo;

    @NotNull(message = "El campo es obligatorio")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidadRequerida;

    public Integer getIdInsumo() { return idInsumo; }
    public void setIdInsumo(Integer idInsumo) { this.idInsumo = idInsumo; }

    public BigDecimal getCantidadRequerida() { return cantidadRequerida; }
    public void setCantidadRequerida(BigDecimal cantidadRequerida) { this.cantidadRequerida = cantidadRequerida; }
}