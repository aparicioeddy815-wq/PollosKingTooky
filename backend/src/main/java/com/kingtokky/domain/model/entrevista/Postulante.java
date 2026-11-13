package com.kingtokky.domain.model.entrevista;

import com.kingtokky.domain.model.seguridad.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "postulante")
public class Postulante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_postulante")
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 255)
    private String nombreCompleto;

    @Column(name = "telefono", nullable = false, length = 30)
    private String telefono;

    @Column(name = "correo", length = 255)
    private String correo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_vacante", nullable = false)
    private Vacante vacante;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoPostulante estado = EstadoPostulante.POSTULADO;

    @Column(name = "evaluacion_gerente", length = 1000)
    private String evaluacionGerente;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "decidido_en")
    private LocalDateTime decididoEn;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_decidido_por")
    private Usuario decididoPor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public Vacante getVacante() { return vacante; }
    public void setVacante(Vacante vacante) { this.vacante = vacante; }

    public EstadoPostulante getEstado() { return estado; }
    public void setEstado(EstadoPostulante estado) { this.estado = estado; }

    public String getEvaluacionGerente() { return evaluacionGerente; }
    public void setEvaluacionGerente(String evaluacionGerente) { this.evaluacionGerente = evaluacionGerente; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }

    public LocalDateTime getDecididoEn() { return decididoEn; }
    public void setDecididoEn(LocalDateTime decididoEn) { this.decididoEn = decididoEn; }

    public Usuario getDecididoPor() { return decididoPor; }
    public void setDecididoPor(Usuario decididoPor) { this.decididoPor = decididoPor; }
}
