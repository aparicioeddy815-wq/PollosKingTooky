package com.kingtokky.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.kingtokky.domain.regla.ProyeccionStock;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProyeccionStockTest {

    private static final BigDecimal FACTOR = new BigDecimal("1.2");

    private BigDecimal n(String valor) {
        return new BigDecimal(valor);
    }

    @Test
    void elStockEnElUmbralOPorDebajoEsCritico() {
        assertThat(ProyeccionStock.estado(n("40"), n("40"), FACTOR)).isEqualTo(ProyeccionStock.CRITICO);
        assertThat(ProyeccionStock.estado(n("10"), n("40"), FACTOR)).isEqualTo(ProyeccionStock.CRITICO);
    }

    @Test
    void elStockCercanoAlUmbralEsBajo() {
        assertThat(ProyeccionStock.estado(n("47"), n("40"), FACTOR)).isEqualTo(ProyeccionStock.BAJO);
    }

    @Test
    void elStockHolgadoEsNormal() {
        assertThat(ProyeccionStock.estado(n("100"), n("40"), FACTOR)).isEqualTo(ProyeccionStock.NORMAL);
    }

    @Test
    void calculaDeficitCuandoElConsumoProyectadoSuperaLasExistencias() {
        ProyeccionStock.Resultado r = ProyeccionStock.calcular(n("30"), n("20"), n("10"), 7, FACTOR);
        assertThat(r.consumoProyectado()).isEqualByComparingTo("70");
        assertThat(r.deficitProyectado()).isEqualByComparingTo("40");
        assertThat(r.superavitSeguro()).isEqualByComparingTo("0");
    }

    @Test
    void calculaSuperavitSeguroDescontandoConsumoYUmbral() {
        ProyeccionStock.Resultado r = ProyeccionStock.calcular(n("200"), n("20"), n("10"), 7, FACTOR);
        assertThat(r.deficitProyectado()).isEqualByComparingTo("0");
        assertThat(r.superavitSeguro()).isEqualByComparingTo("110");
    }
}
