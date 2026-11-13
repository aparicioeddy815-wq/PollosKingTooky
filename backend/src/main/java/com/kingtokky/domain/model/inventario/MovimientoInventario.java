package com.kingtokky.domain.model.inventario;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_inventario")
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_insumo", nullable = false)
    private Insumo insumo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoMovimiento tipo;

    @Column(name = "variacion", nullable = false, precision = 14, scale = 3)
    private BigDecimal variacion;

    @Column(name = "stock_resultante", nullable = false, precision = 14, scale = 3)
    private BigDecimal stockResultante;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "id_pedido")
    private Long idPedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_proveedor")
    private Proveedor proveedor;

    @Column(name = "costo_unitario", precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "documento", length = 60)
    private String documento;

    @Column(name = "motivo", length = 200)
    private String motivo;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) { this.insumo = insumo; }

    public TipoMovimiento getTipo() { return tipo; }
    public void setTipo(TipoMovimiento tipo) { this.tipo = tipo; }

    public BigDecimal getVariacion() { return variacion; }
    public void setVariacion(BigDecimal variacion) { this.variacion = variacion; }

    public BigDecimal getStockResultante() { return stockResultante; }
    public void setStockResultante(BigDecimal stockResultante) { this.stockResultante = stockResultante; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public Long getIdPedido() { return idPedido; }
    public void setIdPedido(Long idPedido) { this.idPedido = idPedido; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public BigDecimal getCostoUnitario() { return costoUnitario; }
    public void setCostoUnitario(BigDecimal costoUnitario) { this.costoUnitario = costoUnitario; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
