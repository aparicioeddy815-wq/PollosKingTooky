package com.kingtokky.domain.model.menu;

import com.kingtokky.domain.model.inventario.Insumo;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "receta_insumo")
public class RecetaInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_receta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_insumo", nullable = false)
    private Insumo insumo;

    @Column(name = "cantidad", nullable = false, precision = 14, scale = 3)
    private BigDecimal cantidad;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) { this.insumo = insumo; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }
}
