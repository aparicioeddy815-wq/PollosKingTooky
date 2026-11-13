package com.kingtokky.domain.model.entrevista;

import com.kingtokky.domain.model.seguridad.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "entrevista")
public class Entrevista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entrevista")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_postulante", nullable = false)
    private Postulante postulante;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", nullable = false, length = 30)
    private ModalidadEntrevista modalidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoEntrevista estado = EstadoEntrevista.PROGRAMADA;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_gerente")
    private Usuario gerente;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Postulante getPostulante() { return postulante; }
    public void setPostulante(Postulante postulante) { this.postulante = postulante; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public ModalidadEntrevista getModalidad() { return modalidad; }
    public void setModalidad(ModalidadEntrevista modalidad) { this.modalidad = modalidad; }

    public EstadoEntrevista getEstado() { return estado; }
    public void setEstado(EstadoEntrevista estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public Usuario getGerente() { return gerente; }
    public void setGerente(Usuario gerente) { this.gerente = gerente; }

    public LocalDateTime getCreadaEn() { return creadaEn; }
    public void setCreadaEn(LocalDateTime creadaEn) { this.creadaEn = creadaEn; }
}
