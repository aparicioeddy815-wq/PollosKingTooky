package com.kingtokky.domain.model.reporte;

import jakarta.persistence.*;

@Entity
@Table(name = "horario_atencion")
public class HorarioAtencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario")
    private Long id;

    @Column(name = "dias", nullable = false, length = 60)
    private String dias;

    @Column(name = "horario", nullable = false, length = 60)
    private String horario;

    @Column(name = "orden", nullable = false)
    private Integer orden;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDias() { return dias; }
    public void setDias(String dias) { this.dias = dias; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
