package com.kingtooky.backend.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AlertaResponse {

    private Integer idAlerta;
    private Integer idInsumo;
    private String nombreInsumo;
    private String unidadMedida;
    private Integer idGuarnicion;
    private BigDecimal stockAlGenerar;
    private BigDecimal stockMinimo;
    private LocalDateTime fechaHora;
    private String estado;

    public Integer getIdAlerta() { return idAlerta; }
    public void setIdAlerta(Integer idAlerta) { this.idAlerta = idAlerta; }

    public Integer getIdInsumo() { return idInsumo; }
    public void setIdInsumo(Integer idInsumo) { this.idInsumo = idInsumo; }

    public String getNombreInsumo() { return nombreInsumo; }
    public void setNombreInsumo(String nombreInsumo) { this.nombreInsumo = nombreInsumo; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

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
}