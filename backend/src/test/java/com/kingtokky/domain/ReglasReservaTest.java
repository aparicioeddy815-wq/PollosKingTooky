package com.kingtokky.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.kingtokky.domain.model.reserva.EstadoReserva;
import com.kingtokky.domain.regla.ReglasReserva;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ReglasReservaTest {

    private final LocalDateTime reserva = LocalDateTime.of(2026, 11, 12, 20, 0);

    @Test
    void laReservaPendienteSeVenceCuandoLlegaSuHoraSinPago() {
        assertThat(ReglasReserva.estaVencida(EstadoReserva.PENDIENTE_PAGO, reserva, reserva.minusMinutes(5))).isFalse();
        assertThat(ReglasReserva.estaVencida(EstadoReserva.PENDIENTE_PAGO, reserva, reserva.plusMinutes(1))).isTrue();
    }

    @Test
    void unaReservaPagadaNuncaSeVenceSola() {
        assertThat(ReglasReserva.estaVencida(EstadoReserva.PAGADA, reserva, reserva.plusHours(3))).isFalse();
    }

    @Test
    void soloSeReprogramaConAlMenosUnaHoraDeAnticipacionYSinPago() {
        assertThat(ReglasReserva.puedeReprogramar(EstadoReserva.PENDIENTE_PAGO, reserva, reserva.minusMinutes(61), 60)).isTrue();
        assertThat(ReglasReserva.puedeReprogramar(EstadoReserva.PENDIENTE_PAGO, reserva, reserva.minusMinutes(30), 60)).isFalse();
        assertThat(ReglasReserva.puedeReprogramar(EstadoReserva.PAGADA, reserva, reserva.minusDays(1), 60)).isFalse();
    }

    @Test
    void soloSeCancelaAntesDeLaHoraYSiLaReservaSigueActiva() {
        assertThat(ReglasReserva.puedeCancelar(EstadoReserva.PENDIENTE_PAGO, reserva, reserva.minusMinutes(1))).isTrue();
        assertThat(ReglasReserva.puedeCancelar(EstadoReserva.PENDIENTE_PAGO, reserva, reserva.plusMinutes(1))).isFalse();
        assertThat(ReglasReserva.puedeCancelar(EstadoReserva.COMPLETADA, reserva, reserva.minusDays(1))).isFalse();
    }

    @Test
    void elTurnoDependeDeLaHora() {
        assertThat(ReglasReserva.turno(LocalTime.of(12, 30))).isEqualTo("MANANA");
        assertThat(ReglasReserva.turno(LocalTime.of(15, 0))).isEqualTo("TARDE");
        assertThat(ReglasReserva.turno(LocalTime.of(20, 0))).isEqualTo("TARDE");
    }

    @Test
    void elCodigoLlevaLaFechaYLaSecuenciaDelDia() {
        assertThat(ReglasReserva.codigo(LocalDate.of(2026, 11, 7), 12)).isEqualTo("RSV-20261107-0012");
        assertThat(ReglasReserva.prefijoCodigo(LocalDate.of(2026, 11, 7))).isEqualTo("RSV-20261107-");
    }

    @Test
    void laAdvertenciaIndicaLaHoraYElPagoEnCaja() {
        assertThat(ReglasReserva.advertenciaPago(LocalTime.of(20, 0))).contains("20:00").contains("5 y 10 minutos");
    }
}
