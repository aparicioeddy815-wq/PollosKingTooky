package com.kingtooky.backend.infrastructure.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerta_stock")
public class AlertaStockEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Integer idAlerta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_insumo")
    private InsumoEntity insumo;

    @Column(name = "id_guarnicion")
    private Integer idGuarnicion;

    @Column(name = "stock_al_generar", precision = 10, scale = 2)
    private BigDecimal stockAlGenerar;

    @Column(name = "stock_minimo", precision = 10, scale = 2)
    private BigDecimal stockMinimo;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora;

    @Column(name = "estado")
    private String estado;

    public Integer getIdAlerta() { return idAlerta; }
    public void setIdAlerta(Integer idAlerta) { this.idAlerta = idAlerta; }

    public InsumoEntity getInsumo() { return insumo; }
    public void setInsumo(InsumoEntity insumo) { this.insumo = insumo; }

    public Integer getIdGuarnicion() { return idGuarnicion; }
    public void setIdGuarnicion(Integer idGuarnicion) { this.idGuarnicion = idGuarnicion; }

    public BigDecimal getStockAlGenerar() { return stockAlGenerar; }
    public void setStockAlGenerar(BigDecimal stockAlGenerar) { this.stockAlGenerar = stockAlGenerar; }

    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) {
            fechaHora = LocalDateTime.now();
        }
    }
}