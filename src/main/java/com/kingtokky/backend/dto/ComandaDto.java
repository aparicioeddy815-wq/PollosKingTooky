package com.kingtokky.backend.dto;

import com.kingtokky.backend.entities.Comanda;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComandaDto {

    private Integer idComanda;
    private Integer numeroComanda;
    private String idUsuario;
    private String estado;
    private LocalDateTime fechaHora;
    private String observaciones;

    public ComandaDto(Comanda comanda) {
        this.idComanda = comanda.getIdComanda();
        this.numeroComanda = comanda.getNumeroComanda();
        this.idUsuario = comanda.getIdUsuario();
        this.estado = comanda.getEstado();
        this.fechaHora = comanda.getFechaHora();
        this.observaciones = comanda.getObservaciones();
    }
}