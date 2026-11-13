package com.kingtokky.domain.regla;

import com.kingtokky.domain.model.reserva.EstadoReserva;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Reglas de las reservas. El pago se hace en caja, no en línea: el cliente debe pasar entre 5 y 10 minutos
 * antes de su hora; si la hora llega y no se pagó, la reserva se cancela automáticamente.
 */
public final class ReglasReserva {

    public static final int MINUTOS_PAGO_ANTICIPADO = 10;
    private static final DateTimeFormatter FORMATO_CODIGO = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final LocalTime INICIO_TARDE = LocalTime.of(15, 0);

    private ReglasReserva() {}

    public static LocalDateTime fechaHora(LocalDate fecha, LocalTime hora) {
        return LocalDateTime.of(fecha, hora);
    }

    /** Hora límite para pagar en caja: la hora misma de la reserva. */
    public static LocalDateTime limitePago(LocalDate fecha, LocalTime hora) {
        return fechaHora(fecha, hora);
    }

    public static boolean estaVencida(EstadoReserva estado, LocalDateTime limitePago, LocalDateTime ahora) {
        return estado == EstadoReserva.PENDIENTE_PAGO && ahora.isAfter(limitePago);
    }

    public static boolean puedeCancelar(EstadoReserva estado, LocalDateTime fechaHora, LocalDateTime ahora) {
        boolean activa = estado == EstadoReserva.PENDIENTE_PAGO || estado == EstadoReserva.PAGADA;
        return activa && ahora.isBefore(fechaHora);
    }

    public static boolean puedeReprogramar(EstadoReserva estado, LocalDateTime fechaHora, LocalDateTime ahora, int limiteMinutos) {
        return estado == EstadoReserva.PENDIENTE_PAGO && !ahora.isAfter(fechaHora.minusMinutes(limiteMinutos));
    }

    /** Turno del personal que atenderá la reserva. */
    public static String turno(LocalTime hora) {
        return hora.isBefore(INICIO_TARDE) ? "MANANA" : "TARDE";
    }

    public static String codigo(LocalDate fechaCreacion, long secuencia) {
        return "RSV-" + fechaCreacion.format(FORMATO_CODIGO) + "-" + String.format("%04d", secuencia);
    }

    public static String prefijoCodigo(LocalDate fechaCreacion) {
        return "RSV-" + fechaCreacion.format(FORMATO_CODIGO) + "-";
    }

    public static String advertenciaPago(LocalTime hora) {
        return "Recuerda pasar por caja entre 5 y " + MINUTOS_PAGO_ANTICIPADO + " minutos antes de las "
                + hora.format(DateTimeFormatter.ofPattern("HH:mm"))
                + " para pagar y confirmar tu reserva. Si no pagas a tiempo, se cancelará automáticamente.";
    }

    public static long minutosHasta(LocalDateTime ahora, LocalDateTime fechaHora) {
        return Duration.between(ahora, fechaHora).toMinutes();
    }
}
