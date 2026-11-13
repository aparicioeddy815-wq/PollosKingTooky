package com.kingtokky.application.dto;

import com.kingtokky.domain.model.entrevista.EstadoEntrevista;
import com.kingtokky.domain.model.entrevista.EstadoPostulante;
import com.kingtokky.domain.model.entrevista.ModalidadEntrevista;
import com.kingtokky.domain.model.entrevista.TipoDocumento;
import com.kingtokky.domain.model.entrevista.UrgenciaVacante;
import com.kingtokky.domain.model.personal.Turno;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class EntrevistaDtos {

    private EntrevistaDtos() {}

    public record VacanteResponse(Long idVacante, String cargo, String estacion, UrgenciaVacante urgencia, int cantidad, String estado) {}

    public record DocumentoResponse(Long idDocumento, TipoDocumento tipo, String nombreArchivo, long tamanoBytes, LocalDateTime subidoEn) {}

    public record PostulanteResponse(Long idPostulante, String nombreCompleto, String telefono, String correo, Long idVacante, String cargo,
                                     UrgenciaVacante urgencia, EstadoPostulante estado, String evaluacionGerente, LocalDateTime creadoEn,
                                     boolean documentosCompletos, List<TipoDocumento> documentosFaltantes, List<DocumentoResponse> documentos,
                                     LocalDateTime fechaEntrevista) {}

    public record EntrevistaResponse(Long idEntrevista, Long idPostulante, String postulante, String telefono, String cargo,
                                     UrgenciaVacante urgencia, LocalDateTime fechaHora, ModalidadEntrevista modalidad, EstadoEntrevista estado,
                                     String observaciones, Long minutosParaInicio, boolean documentosCompletos, boolean cvDisponible,
                                     EstadoPostulante estadoPostulante) {}

    public record ResumenEntrevistasResponse(long entrevistasHoy, long enEvaluacion, long postulantesActivos, long vacantesAbiertas,
                                             List<String> puestosCriticos) {}

    public record AgendaEntrevistasResponse(ResumenEntrevistasResponse resumen, List<EntrevistaResponse> hoy, List<EntrevistaResponse> programadas) {}

    public record ResumenEntrevistadosResponse(long postulantesDelCiclo, long vistoBuenoGerencial, long urgenciaOperativa, long contratadosDelMes) {}

    public record PostulanteRequest(@NotBlank @Size(max = 255) String nombreCompleto, @NotBlank @Size(max = 30) String telefono,
                                    @Email @Size(max = 255) String correo, @NotNull Long idVacante) {}

    public record AgendarEntrevistaRequest(@NotNull Long idPostulante, @NotNull LocalDateTime fechaHora, @NotNull ModalidadEntrevista modalidad) {}

    /** Observaciones de la entrevista; "realizada" y "aprobar" hacen avanzar el proceso del postulante. */
    public record ObservacionesRequest(@Size(max = 1000) String observaciones, @Size(max = 1000) String evaluacionGerente,
                                       Boolean realizada, Boolean aprobar) {}

    public record DescartarRequest(@Size(max = 300) String motivo) {}

    public record ContratarRequest(@NotNull Turno turno, @NotNull @DecimalMin("0.0") BigDecimal salario, Long idEstacion) {}
}
