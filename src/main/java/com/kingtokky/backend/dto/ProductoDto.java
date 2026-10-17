package com.kingtokky.backend.dto;

import com.kingtokky.backend.entities.Producto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {

    private Integer idProducto;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer idCategoria;
    private Boolean disponibleLocal;
    private Boolean disponiblePedidosya;
    private String imagenUrl;

    public ProductoDto(Producto producto) {
        this.idProducto = producto.getIdProducto();
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.precio = producto.getPrecio();
        this.idCategoria = producto.getIdCategoria();
        this.disponibleLocal = producto.getDisponibleLocal();
        this.disponiblePedidosya = producto.getDisponiblePedidosya();
        this.imagenUrl = producto.getImagenUrl();
    }
}