package com.kingtokky.domain.model.pedido;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "contador_turno")
public class ContadorTurno {

    @Id
    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "ultimo", nullable = false)
    private Integer ultimo = 0;

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Integer getUltimo() { return ultimo; }
    public void setUltimo(Integer ultimo) { this.ultimo = ultimo; }
}
