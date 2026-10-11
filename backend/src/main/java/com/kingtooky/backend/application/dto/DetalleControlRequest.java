package com.kingtooky.backend.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class DetalleControlRequest {

    @NotNull(message = "El campo es obligatorio")
    private Integer idInsumo;

    @NotNull(message = "El campo es obligatorio")
    @PositiveOrZero(message = "El stock fisico no puede ser negativo")
    private BigDecimal stockFisico;

    public Integer getIdInsumo() { return idInsumo; }
    public void setIdInsumo(Integer idInsumo) { this.idInsumo = idInsumo; }

    public BigDecimal getStockFisico() { return stockFisico; }
    public void setStockFisico(BigDecimal stockFisico) { this.stockFisico = stockFisico; }
}