package com.kingtokky.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class QuejaGestionDtos {

    private QuejaGestionDtos() {}

    public record ResponderQuejaRequest(@NotBlank @Size(max = 1000) String respuesta) {}

    public record ResumenQuejasResponse(long recibidasMes, long reclamos, long sugerencias, long felicitaciones, long pendientes,
                                        double tiempoPromedioDespachoMin, int objetivoDespachoMin, double satisfaccionPorcentaje,
                                        double horasRespuestaPromedio) {}
}
