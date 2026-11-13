package com.kingtokky.domain.model.inventario;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria_insumo")
public class CategoriaInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria_insumo")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 60)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoInsumo tipo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public TipoInsumo getTipo() { return tipo; }
    public void setTipo(TipoInsumo tipo) { this.tipo = tipo; }
}
