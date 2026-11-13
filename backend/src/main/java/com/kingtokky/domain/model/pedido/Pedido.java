package com.kingtokky.domain.model.pedido;

import com.kingtokky.domain.model.reserva.Reserva;
import com.kingtokky.domain.model.seguridad.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long id;

    @Column(name = "numero_turno", nullable = false)
    private Integer numeroTurno;

    @Column(name = "fecha_turno", nullable = false)
    private LocalDate fechaTurno;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_cliente")
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_registrado_por")
    private Usuario registradoPor;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 30)
    private CanalPedido canal;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPedido estado = EstadoPedido.EN_PREPARACION;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "listo_en")
    private LocalDateTime listoEn;

    @Column(name = "entregado_en")
    private LocalDateTime entregadoEn;

    @Column(name = "cancelado_en")
    private LocalDateTime canceladoEn;

    @Column(name = "motivo_cancelacion", length = 200)
    private String motivoCancelacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_reserva")
    private Reserva reserva;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getNumeroTurno() { return numeroTurno; }
    public void setNumeroTurno(Integer numeroTurno) { this.numeroTurno = numeroTurno; }

    public LocalDate getFechaTurno() { return fechaTurno; }
    public void setFechaTurno(LocalDate fechaTurno) { this.fechaTurno = fechaTurno; }

    public Usuario getCliente() { return cliente; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }

    public Usuario getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(Usuario registradoPor) { this.registradoPor = registradoPor; }

    public CanalPedido getCanal() { return canal; }
    public void setCanal(CanalPedido canal) { this.canal = canal; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getListoEn() { return listoEn; }
    public void setListoEn(LocalDateTime listoEn) { this.listoEn = listoEn; }

    public LocalDateTime getEntregadoEn() { return entregadoEn; }
    public void setEntregadoEn(LocalDateTime entregadoEn) { this.entregadoEn = entregadoEn; }

    public LocalDateTime getCanceladoEn() { return canceladoEn; }
    public void setCanceladoEn(LocalDateTime canceladoEn) { this.canceladoEn = canceladoEn; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public Reserva getReserva() { return reserva; }
    public void setReserva(Reserva reserva) { this.reserva = reserva; }
}
