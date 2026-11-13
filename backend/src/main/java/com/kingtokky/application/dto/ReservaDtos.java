package com.kingtokky.application.dto;

import com.kingtokky.domain.model.reserva.EstadoReserva;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class ReservaDtos {

    private ReservaDtos() {}

    public record ItemReservaRequest(@NotNull Long idProducto, @Min(1) @Max(50) int cantidad) {}

    public record ReservaRequest(
            @NotBlank @Size(max = 255) String nombreCliente,
            @NotBlank @Pattern(regexp = "^[0-9+() -]{7,30}$", message = "teléfono no válido") String telefono,
            @Email @Size(max = 255) String correo,
            @NotNull LocalDate fecha,
            @NotNull LocalTime hora,
            @Min(1) @Max(50) int personas,
            @Size(max = 500) String notas,
            @NotEmpty @Valid List<ItemReservaRequest> items) {}

    public record ReprogramarRequest(@NotNull LocalDate fecha, @NotNull LocalTime hora) {}

    public record ItemReservaResponse(Long idProducto, String producto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}

    public record ReservaResponse(Long idReserva, String codigo, String nombreCliente, String telefono, String correo, LocalDate fecha,
                                  LocalTime hora, String turno, int personas, String notas, BigDecimal total, EstadoReserva estado,
                                  LocalDateTime fechaCreacion, LocalDateTime fechaPago, boolean puedeCancelar, boolean puedeReprogramar,
                                  String advertenciaPago, List<ItemReservaResponse> items) {}

    /** Paso 1 del asistente: fechas de la semana. */
    public record DiaReserva(LocalDate fecha, String diaSemana, String etiqueta, boolean disponible) {}

    /** Paso 2 del asistente: horas disponibles y cupo restante. */
    public record HoraReserva(LocalTime hora, String turno, int cupoRestante, boolean disponible) {}

    public record ProximaVisita(Long idReserva, String codigo, LocalDate fecha, LocalTime hora, int personas, EstadoReserva estado) {}

    public record ResumenReservasResponse(long totalReservas, long pendientesDePago, ProximaVisita proximaVisita, String avisoDeHoy) {}
}
