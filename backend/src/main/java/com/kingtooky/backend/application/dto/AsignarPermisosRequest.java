package com.kingtooky.backend.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class AsignarPermisosRequest {

    @NotNull(message = "Debe enviar la lista de permisos")
    private List<Integer> idPermisos;

    public List<Integer> getIdPermisos() { return idPermisos; }
    public void setIdPermisos(List<Integer> idPermisos) { this.idPermisos = idPermisos; }
}