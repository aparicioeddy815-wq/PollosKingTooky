package com.kingtooky.backend.application.dto;

import java.math.BigDecimal;

public class DetalleControlResponse {

    private Integer idDetalleControl;
    private Integer idInsumo;
    private String nombreInsumo;
    private BigDecimal stockSistema;
    private BigDecimal stockFisico;
    private BigDecimal diferencia;

    public Integer getIdDetalleControl() { return idDetalleControl; }
    public void setIdDetalleControl(Integer idDetalleControl) { this.idDetalleControl = idDetalleControl; }

    public Integer getIdInsumo() { return idInsumo; }
    public void setIdInsumo(Integer idInsumo) { this.idInsumo = idInsumo; }

    public String getNombreInsumo() { return nombreInsumo; }
    public void setNombreInsumo(String nombreInsumo) { this.nombreInsumo = nombreInsumo; }

    public BigDecimal getStockSistema() { return stockSistema; }
    public void setStockSistema(BigDecimal stockSistema) { this.stockSistema = stockSistema; }

    public BigDecimal getStockFisico() { return stockFisico; }
    public void setStockFisico(BigDecimal stockFisico) { this.stockFisico = stockFisico; }

    public BigDecimal getDiferencia() { return diferencia; }
    public void setDiferencia(BigDecimal diferencia) { this.diferencia = diferencia; }
}