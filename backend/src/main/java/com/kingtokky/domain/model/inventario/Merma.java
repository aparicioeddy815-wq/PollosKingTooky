package com.kingtokky.domain.model.inventario;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "merma")
public class Merma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_merma")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_insumo", nullable = false)
    private Insumo insumo;

    @Column(name = "cantidad", nullable = false, precision = 14, scale = 3)
    private BigDecimal cantidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false, length = 30)
    private MotivoMerma motivo;

    @Column(name = "observacion", length = 300)
    private String observacion;

    @Column(name = "costo_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoTotal;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "id_usuario")
    private Long idUsuario;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) { this.insumo = insumo; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public MotivoMerma getMotivo() { return motivo; }
    public void setMotivo(MotivoMerma motivo) { this.motivo = motivo; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public BigDecimal getCostoTotal() { return costoTotal; }
    public void setCostoTotal(BigDecimal costoTotal) { this.costoTotal = costoTotal; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
}
