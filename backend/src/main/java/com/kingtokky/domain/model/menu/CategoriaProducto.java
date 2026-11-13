package com.kingtokky.domain.model.menu;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria_producto")
public class CategoriaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    @Column(name = "url_pedidosya", length = 500)
    private String urlPedidosYa;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }

    public String getUrlPedidosYa() { return urlPedidosYa; }
    public void setUrlPedidosYa(String urlPedidosYa) { this.urlPedidosYa = urlPedidosYa; }
}
