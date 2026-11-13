package com.kingtokky.domain.model.inventario;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerta_stock")
public class AlertaStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_insumo", nullable = false)
    private Insumo insumo;

    @Column(name = "stock_actual", nullable = false, precision = 14, scale = 3)
    private BigDecimal stockActual;

    @Column(name = "umbral_minimo", nullable = false, precision = 14, scale = 3)
    private BigDecimal umbralMinimo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoAlerta estado = EstadoAlerta.ABIERTA;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn = LocalDateTime.now();

    @Column(name = "atendida_en")
    private LocalDateTime atendidaEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) { this.insumo = insumo; }

    public BigDecimal getStockActual() { return stockActual; }
    public void setStockActual(BigDecimal stockActual) { this.stockActual = stockActual; }

    public BigDecimal getUmbralMinimo() { return umbralMinimo; }
    public void setUmbralMinimo(BigDecimal umbralMinimo) { this.umbralMinimo = umbralMinimo; }

    public EstadoAlerta getEstado() { return estado; }
    public void setEstado(EstadoAlerta estado) { this.estado = estado; }

    public LocalDateTime getCreadaEn() { return creadaEn; }
    public void setCreadaEn(LocalDateTime creadaEn) { this.creadaEn = creadaEn; }

    public LocalDateTime getAtendidaEn() { return atendidaEn; }
    public void setAtendidaEn(LocalDateTime atendidaEn) { this.atendidaEn = atendidaEn; }
}
