package com.kingtokky.domain.model.seguridad;

import jakarta.persistence.*;

@Entity
@Table(name = "permiso")
public class Permiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_permiso")
    private Long idPermiso;

    @Column(name = "codigo", nullable = false, unique = true, length = 60)
    private String codigo;

    @Column(name = "descripcion", nullable = false, length = 200)
    private String descripcion;

    public Long getIdPermiso() { return idPermiso; }
    public void setIdPermiso(Long idPermiso) { this.idPermiso = idPermiso; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
