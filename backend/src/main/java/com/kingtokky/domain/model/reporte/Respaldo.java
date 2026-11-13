package com.kingtokky.domain.model.reporte;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "respaldo")
public class Respaldo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respaldo")
    private Long id;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "archivo", nullable = false, length = 200)
    private String archivo;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoRespaldo estado;

    @Column(name = "duracion_ms", nullable = false)
    private Long duracionMs = 0L;

    @Column(name = "mensaje", length = 300)
    private String mensaje;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getArchivo() { return archivo; }
    public void setArchivo(String archivo) { this.archivo = archivo; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public EstadoRespaldo getEstado() { return estado; }
    public void setEstado(EstadoRespaldo estado) { this.estado = estado; }

    public Long getDuracionMs() { return duracionMs; }
    public void setDuracionMs(Long duracionMs) { this.duracionMs = duracionMs; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
}
