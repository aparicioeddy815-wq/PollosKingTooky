package com.kingtooky.backend.infrastructure.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "producto_insumo")
public class ProductoInsumoEntity {

    @EmbeddedId
    private ProductoInsumoId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_insumo", insertable = false, updatable = false)
    private InsumoEntity insumo;

    @Column(name = "cantidad_requerida", precision = 10, scale = 3)
    private BigDecimal cantidadRequerida;

    public ProductoInsumoId getId() { return id; }
    public void setId(ProductoInsumoId id) { this.id = id; }

    public InsumoEntity getInsumo() { return insumo; }
    public void setInsumo(InsumoEntity insumo) { this.insumo = insumo; }

    public BigDecimal getCantidadRequerida() { return cantidadRequerida; }
    public void setCantidadRequerida(BigDecimal cantidadRequerida) { this.cantidadRequerida = cantidadRequerida; }
}