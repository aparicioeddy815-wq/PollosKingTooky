package com.kingtooky.backend.application.dto;

import java.util.List;

public class RolResponse {

    private Integer idRol;
    private String nombre;
    private List<PermisoResponse> permisos;

    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<PermisoResponse> getPermisos() { return permisos; }
    public void setPermisos(List<PermisoResponse> permisos) { this.permisos = permisos; }
}