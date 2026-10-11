package com.kingtooky.backend.security.application;

public class UsuarioPrincipal {

    private Integer idUsuario;
    private String nombreUsuario;
    private String rol;

    public UsuarioPrincipal() {
    }

    public UsuarioPrincipal(Integer idUsuario, String nombreUsuario, String rol) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.rol = rol;
    }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    @Override
    public String toString() {
        return nombreUsuario;
    }
}