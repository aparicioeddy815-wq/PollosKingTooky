package com.kingtooky.backend.infrastructure.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_control_inventario")
public class DetalleControlInventarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_control")
    private Integer idDetalleControl;

    @Column(name = "id_control")
    private Integer idControl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_insumo")
    private InsumoEntity insumo;

    @Column(name = "stock_sistema", precision = 10, scale = 2)
    private BigDecimal stockSistema;

    @Column(name = "stock_fisico", precision = 10, scale = 2)
    private BigDecimal stockFisico;

    @Column(name = "diferencia", precision = 10, scale = 2)
    private BigDecimal diferencia;

    public Integer getIdDetalleControl() { return idDetalleControl; }
    public void setIdDetalleControl(Integer idDetalleControl) { this.idDetalleControl = idDetalleControl; }

    public Integer getIdControl() { return idControl; }
    public void setIdControl(Integer idControl) { this.idControl = idControl; }

    public InsumoEntity getInsumo() { return insumo; }
    public void setInsumo(InsumoEntity insumo) { this.insumo = insumo; }

    public BigDecimal getStockSistema() { return stockSistema; }
    public void setStockSistema(BigDecimal stockSistema) { this.stockSistema = stockSistema; }

    public BigDecimal getStockFisico() { return stockFisico; }
    public void setStockFisico(BigDecimal stockFisico) { this.stockFisico = stockFisico; }

    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }
}