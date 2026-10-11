package com.kingtooky.backend.application.dto;

import java.util.List;

public class ConsumoResponse {

    private Integer idPedido;
    private List<MovimientoResponse> movimientos;

    public Integer getIdPedido() { return idPedido; }
    public void setIdPedido(Integer idPedido) { this.idPedido = idPedido; }

    public List<MovimientoResponse> getMovimientos() { return movimientos; }
    public void setMovimientos(List<MovimientoResponse> movimientos) { this.movimientos = movimientos; }
}