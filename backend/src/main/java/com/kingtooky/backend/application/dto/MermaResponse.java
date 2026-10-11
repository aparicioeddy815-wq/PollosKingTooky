package com.kingtooky.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MermaResponse {

    private Integer idMerma;
    private Integer idInsumo;
    private String nombreInsumo;
    private Integer idUsuario;
    private BigDecimal cantidad;
    private String motivo;
    private LocalDateTime fechaHora;

    public Integer getIdMerma() { return idMerma; }
    public void setIdMerma(Integer idMerma) { this.idMerma = idMerma; }

    public Integer getIdInsumo() { return idInsumo; }
    public void setIdInsumo(Integer idInsumo) { this.idInsumo = idInsumo; }

    public String getNombreInsumo() { return nombreInsumo; }
    public void setNombreInsumo(String nombreInsumo) { this.nombreInsumo = nombreInsumo; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}