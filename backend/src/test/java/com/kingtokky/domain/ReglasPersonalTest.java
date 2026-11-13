package com.kingtokky.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.kingtokky.domain.regla.ReglasPersonal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ReglasPersonalTest {

    @Test
    void elControlSemanalEsLosSabados() {
        assertThat(ReglasPersonal.esDiaDeControlSemanal(LocalDate.of(2026, 11, 7))).isTrue();   // sábado
        assertThat(ReglasPersonal.esDiaDeControlSemanal(LocalDate.of(2026, 11, 6))).isFalse();  // viernes
    }

    @Test
    void elControlQuincenalEsElDia15YElDia30() {
        assertThat(ReglasPersonal.esDiaDeControlQuincenal(LocalDate.of(2026, 11, 15))).isTrue();
        assertThat(ReglasPersonal.esDiaDeControlQuincenal(LocalDate.of(2026, 11, 30))).isTrue();
        assertThat(ReglasPersonal.esDiaDeControlQuincenal(LocalDate.of(2026, 11, 20))).isFalse();
    }

    @Test
    void enFebreroElControlQuincenalCaeElUltimoDiaDelMes() {
        assertThat(ReglasPersonal.esDiaDeControlQuincenal(LocalDate.of(2027, 2, 28))).isTrue();
    }

    @Test
    void calculaLasProximasFechasDeControl() {
        assertThat(ReglasPersonal.proximoSabado(LocalDate.of(2026, 11, 9))).isEqualTo(LocalDate.of(2026, 11, 14));
        assertThat(ReglasPersonal.proximaQuincena(LocalDate.of(2026, 11, 9))).isEqualTo(LocalDate.of(2026, 11, 15));
    }

    @Test
    void elTurnoMananaTerminaALas15() {
        assertThat(ReglasPersonal.esTurnoManana(LocalTime.of(14, 59))).isTrue();
        assertThat(ReglasPersonal.esTurnoManana(LocalTime.of(15, 0))).isFalse();
    }
}
