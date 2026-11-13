package com.kingtokky.application.dto;

import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import java.time.LocalDateTime;

public final class BitacoraDtos {

    private BitacoraDtos() {}

    public record BitacoraResponse(Long idBitacora, LocalDateTime fechaHora, TipoEvento tipo, String modulo, String usuario, String rolCodigo,
                                   String accion, NivelBitacora nivel, String ip) {}

    public record ResumenBitacoraResponse(long eventosHoy, long iniciosDeSesionHoy, long sesionesActivas, long suspensionesActivas,
                                          long ajustesDeStockHoy, long alertasDeSeguridadHoy) {}
}
