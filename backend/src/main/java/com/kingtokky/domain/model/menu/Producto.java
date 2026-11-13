package com.kingtokky.domain.model.menu;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private CategoriaProducto categoria;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "costo", nullable = false, precision = 12, scale = 2)
    private BigDecimal costo;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "disponible", nullable = false)
    private boolean disponible = true;

    @Column(name = "publicado_pedidosya", nullable = false)
    private boolean publicadoPedidosYa = true;

    @Column(name = "en_salon", nullable = false)
    private boolean enSalon = true;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CategoriaProducto getCategoria() { return categoria; }
    public void setCategoria(CategoriaProducto categoria) { this.categoria = categoria; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public boolean isPublicadoPedidosYa() { return publicadoPedidosYa; }
    public void setPublicadoPedidosYa(boolean publicadoPedidosYa) { this.publicadoPedidosYa = publicadoPedidosYa; }

    public boolean isEnSalon() { return enSalon; }
    public void setEnSalon(boolean enSalon) { this.enSalon = enSalon; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
