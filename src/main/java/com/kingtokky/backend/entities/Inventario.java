package com.kingtokky.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idInsumo;
    private String nombreInsumo;
    private String categoriaInsumo;
    private BigDecimal stockActual;
    private BigDecimal umbralMinimo;
    private BigDecimal consumoDia;
    private String unidadMedida;
    private LocalDateTime ultimaActualizacion;
}