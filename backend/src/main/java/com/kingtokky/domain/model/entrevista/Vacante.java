package com.kingtokky.domain.model.entrevista;

import com.kingtokky.domain.model.personal.EstacionOperativa;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vacante")
public class Vacante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vacante")
    private Long id;

    @Column(name = "cargo", nullable = false, length = 80)
    private String cargo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_estacion")
    private EstacionOperativa estacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgencia", nullable = false, length = 30)
    private UrgenciaVacante urgencia = UrgenciaVacante.NORMAL;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoVacante estado = EstadoVacante.ABIERTA;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public EstacionOperativa getEstacion() { return estacion; }
    public void setEstacion(EstacionOperativa estacion) { this.estacion = estacion; }

    public UrgenciaVacante getUrgencia() { return urgencia; }
    public void setUrgencia(UrgenciaVacante urgencia) { this.urgencia = urgencia; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public EstadoVacante getEstado() { return estado; }
    public void setEstado(EstadoVacante estado) { this.estado = estado; }

    public LocalDateTime getCreadaEn() { return creadaEn; }
    public void setCreadaEn(LocalDateTime creadaEn) { this.creadaEn = creadaEn; }
}
