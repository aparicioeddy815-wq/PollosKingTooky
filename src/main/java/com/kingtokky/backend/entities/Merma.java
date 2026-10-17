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
public class Merma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idMerma;
    private Integer idInsumo;
    private BigDecimal cantidadPerdida;
    private String motivo;
    private LocalDateTime fechaRegistro;
}