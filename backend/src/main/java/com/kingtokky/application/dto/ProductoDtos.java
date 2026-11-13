package com.kingtokky.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public final class ProductoDtos {

    private ProductoDtos() {}

    public record ProductoRequest(
            @NotBlank @Size(max = 120) String nombre,
            @Size(max = 500) String descripcion,
            @NotNull @DecimalMin("0.0") BigDecimal precio,
            @NotBlank String categoria,
            @Size(max = 500) String imagenUrl,
            Boolean disponible,
            Boolean publicadoPedidosYa,
            Boolean enSalon,
            @DecimalMin("0.0") BigDecimal costo) {}

    public record ProductoResponse(Long idProducto, String nombre, String descripcion, BigDecimal precio, BigDecimal costo,
                                   String categoria, String categoriaNombre, String imagenUrl, boolean disponible,
                                   boolean publicadoPedidosYa, boolean enSalon) {}

    public record CategoriaProductoResponse(Long idCategoria, String codigo, String nombre) {}
}
