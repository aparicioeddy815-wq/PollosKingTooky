package com.kingtokky.domain.model.seguridad;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Column(name = "codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 30)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 300)
    private String descripcion;

    @Column(name = "nivel_seguridad", nullable = false, length = 20)
    private String nivelSeguridad;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "rol_permiso",
        joinColumns = @JoinColumn(name = "id_rol"),
        inverseJoinColumns = @JoinColumn(name = "id_permiso"))
    private Set<Permiso> permisos = new HashSet<>();

    public Long getIdRol() { return idRol; }
    public void setIdRol(Long idRol) { this.idRol = idRol; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getNivelSeguridad() { return nivelSeguridad; }
    public void setNivelSeguridad(String nivelSeguridad) { this.nivelSeguridad = nivelSeguridad; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public Set<Permiso> getPermisos() { return permisos; }
    public void setPermisos(Set<Permiso> permisos) { this.permisos = permisos; }
}
