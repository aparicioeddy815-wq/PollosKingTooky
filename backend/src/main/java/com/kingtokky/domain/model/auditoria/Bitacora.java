package com.kingtokky.domain.model.auditoria;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bitacora")
public class Bitacora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bitacora")
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoEvento tipo;

    @Column(name = "modulo", nullable = false, length = 40)
    private String modulo;

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "usuario_nombre", nullable = false, length = 100)
    private String usuarioNombre;

    @Column(name = "rol_codigo", length = 30)
    private String rolCodigo;

    @Column(name = "accion", nullable = false, length = 500)
    private String accion;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false, length = 30)
    private NivelBitacora nivel = NivelBitacora.INFO;

    @Column(name = "ip", length = 45)
    private String ip;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public TipoEvento getTipo() { return tipo; }
    public void setTipo(TipoEvento tipo) { this.tipo = tipo; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getUsuarioNombre() { return usuarioNombre; }
    public void setUsuarioNombre(String usuarioNombre) { this.usuarioNombre = usuarioNombre; }

    public String getRolCodigo() { return rolCodigo; }
    public void setRolCodigo(String rolCodigo) { this.rolCodigo = rolCodigo; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public NivelBitacora getNivel() { return nivel; }
    public void setNivel(NivelBitacora nivel) { this.nivel = nivel; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
}
