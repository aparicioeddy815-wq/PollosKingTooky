package com.kingtokky.domain.model.cliente;

import com.kingtokky.domain.model.seguridad.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "queja")
public class Queja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_queja")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Usuario cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoQueja tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 30)
    private CategoriaQueja categoria;

    @Column(name = "mensaje", nullable = false, length = 1000)
    private String mensaje;

    @Column(name = "numero_pedido")
    private Integer numeroPedido;

    @Column(name = "valoracion")
    private Integer valoracion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoQueja estado = EstadoQueja.RECIBIDA;

    @Column(name = "respuesta", length = 1000)
    private String respuesta;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_respondido_por")
    private Usuario respondidoPor;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "respondido_en")
    private LocalDateTime respondidoEn;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getCliente() { return cliente; }
    public void setCliente(Usuario cliente) { this.cliente = cliente; }

    public TipoQueja getTipo() { return tipo; }
    public void setTipo(TipoQueja tipo) { this.tipo = tipo; }

    public CategoriaQueja getCategoria() { return categoria; }
    public void setCategoria(CategoriaQueja categoria) { this.categoria = categoria; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public Integer getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(Integer numeroPedido) { this.numeroPedido = numeroPedido; }

    public Integer getValoracion() { return valoracion; }
    public void setValoracion(Integer valoracion) { this.valoracion = valoracion; }

    public EstadoQueja getEstado() { return estado; }
    public void setEstado(EstadoQueja estado) { this.estado = estado; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public Usuario getRespondidoPor() { return respondidoPor; }
    public void setRespondidoPor(Usuario respondidoPor) { this.respondidoPor = respondidoPor; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getRespondidoEn() { return respondidoEn; }
    public void setRespondidoEn(LocalDateTime respondidoEn) { this.respondidoEn = respondidoEn; }
}
