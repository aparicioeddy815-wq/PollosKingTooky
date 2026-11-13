package com.kingtokky.domain.model.inventario;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "control_inventario")
public class ControlInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_control")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoControl tipo;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoControl estado = EstadoControl.ABIERTO;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "observacion", length = 300)
    private String observacion;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "cerrado_en")
    private LocalDateTime cerradoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public TipoControl getTipo() { return tipo; }
    public void setTipo(TipoControl tipo) { this.tipo = tipo; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public EstadoControl getEstado() { return estado; }
    public void setEstado(EstadoControl estado) { this.estado = estado; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getCerradoEn() { return cerradoEn; }
    public void setCerradoEn(LocalDateTime cerradoEn) { this.cerradoEn = cerradoEn; }
}
