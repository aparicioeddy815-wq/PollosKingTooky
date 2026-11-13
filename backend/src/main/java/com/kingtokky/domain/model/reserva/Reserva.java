package com.kingtokky.domain.model.reserva;

import com.kingtokky.domain.model.seguridad.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 24)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Usuario cliente;

    @Column(name = "nombre_cliente", nullable = false, length = 255)
    private String nombreCliente;

    @Column(name = "telefono", nullable = false, length = 30)
    private String telefono;

    @Column(name = "correo", length = 255)
    private String correo;

    @Column(name = "fecha_encuentro", nullable = false)
    private LocalDate fechaEncuentro;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Column(name = "personas", nullable = false)
    private Integer personas;

    @Column(name = "notas", length = 500)
    private String notas;

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoReserva estado = EstadoReserva.PENDIENTE_PAGO;

    @Column(name = "limite_pago", nullable = false)
    private LocalDateTime limitePago;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "pagada_en")
    private LocalDateTime pagadaEn;

    @Column(name = "cancelada_en")
    private LocalDateTime canceladaEn;

    @Column(name = "motivo_cancelacion", length = 200)
    private String motivoCancelacion;

    @Column(name = "reprogramaciones", nullable = false)
    private Integer reprogramaciones = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_cobrada_por")
    private Usuario cobradaPor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public Usuario getCliente() { return cliente; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public LocalDate getFechaEncuentro() { return fechaEncuentro; }
    public void setFechaEncuentro(LocalDate fechaEncuentro) { this.fechaEncuentro = fechaEncuentro; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public Integer getPersonas() { return personas; }
    public void setPersonas(Integer personas) { this.personas = personas; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public EstadoReserva getEstado() { return estado; }
    public void setEstado(EstadoReserva estado) { this.estado = estado; }

    public LocalDateTime getLimitePago() { return limitePago; }
    public void setLimitePago(LocalDateTime limitePago) { this.limitePago = limitePago; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getPagadaEn() { return pagadaEn; }
    public void setPagadaEn(LocalDateTime pagadaEn) { this.pagadaEn = pagadaEn; }

    public LocalDateTime getCanceladaEn() { return canceladaEn; }
    public void setCanceladaEn(LocalDateTime canceladaEn) { this.canceladaEn = canceladaEn; }

    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

    public Integer getReprogramaciones() { return reprogramaciones; }
    public void setReprogramaciones(Integer reprogramaciones) { this.reprogramaciones = reprogramaciones; }

    public Usuario getCobradaPor() { return cobradaPor; }
    public void setCobradaPor(Usuario cobradaPor) { this.cobradaPor = cobradaPor; }
}
