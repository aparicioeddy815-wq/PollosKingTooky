package com.kingtokky.domain.model.reporte;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reporte_generado")
public class ReporteGenerado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reporte")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 160)
    private String nombre;

    @Column(name = "tipo", nullable = false, length = 40)
    private String tipo;

    @Column(name = "formato", nullable = false, length = 10)
    private String formato;

    @Column(name = "periodo", length = 30)
    private String periodo;

    @Column(name = "generado_en", nullable = false)
    private LocalDateTime generadoEn = LocalDateTime.now();

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "ruta", length = 500)
    private String ruta;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes = 0L;

    @Column(name = "programado", nullable = false)
    private boolean programado = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getFormato() { return formato; }
    public void setFormato(String formato) { this.formato = formato; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public LocalDateTime getGeneradoEn() { return generadoEn; }
    public void setGeneradoEn(LocalDateTime generadoEn) { this.generadoEn = generadoEn; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }

    public Long getTamanoBytes() { return tamanoBytes; }
    public void setTamanoBytes(Long tamanoBytes) { this.tamanoBytes = tamanoBytes; }

    public boolean isProgramado() { return programado; }
    public void setProgramado(boolean programado) { this.programado = programado; }
}
