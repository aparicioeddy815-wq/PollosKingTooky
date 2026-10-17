package com.kingtokky.backend.dto;

import com.kingtokky.backend.entities.Merma;
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
public class MermaDto {

    private Integer idMerma;
    private Integer idInsumo;
    private BigDecimal cantidadPerdida;
    private String motivo;
    private LocalDateTime fechaRegistro;

    public MermaDto(Merma merma) {
        this.idMerma = merma.getIdMerma();
        this.idInsumo = merma.getIdInsumo();
        this.cantidadPerdida = merma.getCantidadPerdida();
        this.motivo = merma.getMotivo();
        this.fechaRegistro = merma.getFechaRegistro();
    }
}