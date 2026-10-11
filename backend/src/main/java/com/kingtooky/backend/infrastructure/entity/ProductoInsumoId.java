package com.kingtooky.backend.infrastructure.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProductoInsumoId implements Serializable {

    @Column(name = "id_producto")
    private Integer idProducto;

    @Column(name = "id_insumo")
    private Integer idInsumo;

    public ProductoInsumoId() {
    }

    public ProductoInsumoId(Integer idProducto, Integer idInsumo) {
        this.idProducto = idProducto;
        this.idInsumo = idInsumo;
    }

    public Integer getIdProducto() { return idProducto; }
    public void setIdProducto(Integer idProducto) { this.idProducto = idProducto; }

    public Integer getIdInsumo() { return idInsumo; }
    public void setIdInsumo(Integer idInsumo) { this.idInsumo = idInsumo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductoInsumoId)) return false;
        ProductoInsumoId that = (ProductoInsumoId) o;
        return Objects.equals(idProducto, that.idProducto) && Objects.equals(idInsumo, that.idInsumo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProducto, idInsumo);
    }
}