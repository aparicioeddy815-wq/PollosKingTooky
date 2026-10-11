package com.kingtooky.backend.application.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ControlResponse {

    private Integer idControl;
    private Integer idUsuario;
    private String tipo;
    private LocalDate fecha;
    private String estado;
    private String observaciones;
    private List<DetalleControlResponse> detalles = new ArrayList<>();

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

    public List<DetalleControlResponse> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleControlResponse> detalles) { this.detalles = detalles; }
}