package com.kingtooky.backend.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ConsumoRequest {

    private Integer idPedido;

    @NotEmpty(message = "Debe enviar al menos un producto")
    @Valid
    private List<ConsumoItemRequest> items;

    public Integer getIdPedido() { return idPedido; }
    public void setIdPedido(Integer idPedido) { this.idPedido = idPedido; }

    public List<ConsumoItemRequest> getItems() { return items; }
    public void setItems(List<ConsumoItemRequest> items) { this.items = items; }
}