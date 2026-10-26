package com.kingtooky.backend.application.dto;

import jakarta.validation.constraints.*;

public class ConsumoItemRequest {

    @NotNull(message = "El campo es obligatorio")
    private Integer idProducto;

    @NotNull(message = "El campo es obligatorio")
    @Min(value = 1, message = "La cantidad minima es 1")
    private Integer cantidad;

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
}