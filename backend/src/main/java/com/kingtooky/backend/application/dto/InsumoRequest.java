package com.kingtooky.backend.application.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class InsumoRequest {

    @NotBlank(message = "El campo es obligatorio")
    private String nombre;

    @NotBlank(message = "El campo es obligatorio")
    private String unidadMedida;

    @PositiveOrZero(message = "El stock no puede ser negativo")
    private BigDecimal stockActual;

    @NotNull(message = "El campo es obligatorio")
    @PositiveOrZero(message = "El stock minimo no puede ser negativo")
    private BigDecimal stockMinimo;

    @Pattern(regexp = "INSUMO|MENAJE|UTENSILIO", message = "El tipo debe ser INSUMO, MENAJE o UTENSILIO")
    private String tipo;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public BigDecimal getStockActual() { return stockActual; }
    public void setStockActual(BigDecimal stockActual) { this.stockActual = stockActual; }

    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}