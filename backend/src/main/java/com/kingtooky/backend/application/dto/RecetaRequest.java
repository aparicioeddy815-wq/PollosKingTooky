package com.kingtooky.backend.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class RecetaRequest {

    @NotNull(message = "Debe enviar los items de la receta")
    @Valid
    private List<RecetaItemRequest> items;

    public List<RecetaItemRequest> getItems() { return items; }
    public void setItems(List<RecetaItemRequest> items) { this.items = items; }
}