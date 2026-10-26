package com.kingtooky.backend.application.dto;

import java.time.LocalDateTime;

public class AuditoriaResponse {

    private Long idAuditoria;
    private Integer idUsuario;
    private String accion;
    private String entidad;
    private String idEntidad;
    private String detalle;
    private LocalDateTime fechaHora;

    public Long getIdAuditoria() { return idAuditoria; }
    public void setIdAuditoria(Long idAuditoria) { this.idAuditoria = idAuditoria; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getEntidad() { return entidad; }
    public void setEntidad(String entidad) { this.entidad = entidad; }

    public String getIdEntidad() { return idEntidad; }
    public void setIdEntidad(String idEntidad) { this.idEntidad = idEntidad; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}