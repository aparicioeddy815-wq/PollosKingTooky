package com.kingtokky.domain.model.inventario;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "control_inventario_detalle")
public class ControlInventarioDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_control")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_control", nullable = false)
    private ControlInventario control;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_insumo", nullable = false)
    private Insumo insumo;

    @Column(name = "stock_sistema", nullable = false, precision = 14, scale = 3)
    private BigDecimal stockSistema;

    @Column(name = "stock_contado", precision = 14, scale = 3)
    private BigDecimal stockContado;

    @Column(name = "diferencia", precision = 14, scale = 3)
    private BigDecimal diferencia;

    @Column(name = "ajustado", nullable = false)
    private boolean ajustado = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ControlInventario getControl() { return control; }
    public void setControl(ControlInventario control) { this.control = control; }

    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) { this.insumo = insumo; }

    public BigDecimal getStockSistema() { return stockSistema; }
    public void setStockSistema(BigDecimal stockSistema) { this.stockSistema = stockSistema; }

    public BigDecimal getStockContado() { return stockContado; }
    public void setStockContado(BigDecimal stockContado) { this.stockContado = stockContado; }

    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }

    public boolean isAjustado() { return ajustado; }
    public void setAjustado(boolean ajustado) { this.ajustado = ajustado; }
}
