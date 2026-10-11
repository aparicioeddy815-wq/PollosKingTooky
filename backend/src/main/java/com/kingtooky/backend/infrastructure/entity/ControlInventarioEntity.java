package com.kingtooky.backend.infrastructure.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "control_inventario")
public class ControlInventarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_control")
    private Integer idControl;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "tipo")
    private String tipo;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "estado")
    private String estado;

    @Column(name = "observaciones")
    private String observaciones;

    public Integer getIdControl() { return idControl; }
    public void setIdControl(Integer idControl) { this.idControl = idControl; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}