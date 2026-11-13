package com.kingtokky.domain.model.personal;

import jakarta.persistence.*;

@Entity
@Table(name = "estacion_operativa")
public class EstacionOperativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estacion")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(name = "puestos_totales", nullable = false)
    private Integer puestosTotales;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getPuestosTotales() { return puestosTotales; }
    public void setPuestosTotales(Integer puestosTotales) { this.puestosTotales = puestosTotales; }
}
