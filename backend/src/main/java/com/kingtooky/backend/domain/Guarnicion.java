package com.kingtooky.backend.domain;

public class Guarnicion {
    private Integer idGuarnicion;
    private String nombre;
    private Double stockDisponible;
    private Double stockMinimo;
    private String estado;

    public Integer getIdGuarnicion() {
        return idGuarnicion;
    }

    public void setIdGuarnicion(Integer idGuarnicion) {
        this.idGuarnicion = idGuarnicion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getStockDisponible() {
        return stockDisponible;
    }

    public void setStockDisponible(Double stockDisponible) {
        this.stockDisponible = stockDisponible;
    }

    public Double getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Double stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
