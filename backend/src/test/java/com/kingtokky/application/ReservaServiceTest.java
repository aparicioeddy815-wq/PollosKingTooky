package com.kingtokky.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kingtokky.application.service.AuditoriaService;
import com.kingtokky.application.service.ReservaService;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.reserva.EstadoReserva;
import com.kingtokky.domain.model.reserva.Reserva;
import com.kingtokky.domain.repository.ReservaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservas;

    @Mock
    private AuditoriaService auditoria;

    @InjectMocks
    private ReservaService servicio;

    @Test
    void cancelaAutomaticamenteLasReservasCuyoPlazoDePagoVencio() {
        Reserva vencida = new Reserva();
        vencida.setCodigo("RSV-20261107-0001");
        vencida.setFechaEncuentro(LocalDate.now().minusDays(1));
        vencida.setHora(LocalTime.of(20, 0));
        vencida.setEstado(EstadoReserva.PENDIENTE_PAGO);
        when(reservas.findByEstadoAndLimitePagoBefore(eq(EstadoReserva.PENDIENTE_PAGO), any(LocalDateTime.class))).thenReturn(List.of(vencida));

        int canceladas = servicio.cancelarVencidas();

        assertThat(canceladas).isEqualTo(1);
        assertThat(vencida.getEstado()).isEqualTo(EstadoReserva.CANCELADA_AUTO);
        assertThat(vencida.getMotivoCancelacion()).contains("pago");
        verify(auditoria).registrarSistema(eq(TipoEvento.RESERVA), any(String.class), any(String.class), any());
    }
}
