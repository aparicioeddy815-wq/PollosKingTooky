package com.kingtokky.domain.model.inventario;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "insumo")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_insumo")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria_insumo", nullable = false)
    private CategoriaInsumo categoria;

    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    @Column(name = "stock_actual", nullable = false, precision = 14, scale = 3)
    private BigDecimal stockActual;

    @Column(name = "umbral_minimo", nullable = false, precision = 14, scale = 3)
    private BigDecimal umbralMinimo;

    @Column(name = "consumo_promedio_diario", nullable = false, precision = 14, scale = 3)
    private BigDecimal consumoPromedioDiario;

    @Column(name = "costo_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public CategoriaInsumo getCategoria() { return categoria; }
    public void setCategoria(CategoriaInsumo categoria) { this.categoria = categoria; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public BigDecimal getStockActual() { return stockActual; }
    public void setStockActual(BigDecimal stockActual) { this.stockActual = stockActual; }

    public BigDecimal getUmbralMinimo() { return umbralMinimo; }
    public void setUmbralMinimo(BigDecimal umbralMinimo) { this.umbralMinimo = umbralMinimo; }

    public BigDecimal getConsumoPromedioDiario() { return consumoPromedioDiario; }
    public void setConsumoPromedioDiario(BigDecimal consumoPromedioDiario) { this.consumoPromedioDiario = consumoPromedioDiario; }

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}
