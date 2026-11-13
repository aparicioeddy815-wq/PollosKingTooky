package com.kingtokky.domain.model.reporte;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cierre_caja")
public class CierreCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cierre")
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoCierre tipo;

    @Column(name = "total_ventas", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalVentas;

    @Column(name = "total_pedidos", nullable = false)
    private Integer totalPedidos;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "observacion", length = 300)
    private String observacion;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public TipoCierre getTipo() { return tipo; }
    public void setTipo(TipoCierre tipo) { this.tipo = tipo; }

    public BigDecimal getTotalVentas() { return totalVentas; }
    public void setTotalVentas(BigDecimal totalVentas) { this.totalVentas = totalVentas; }

    public Integer getTotalPedidos() { return totalPedidos; }
    public void setTotalPedidos(Integer totalPedidos) { this.totalPedidos = totalPedidos; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
