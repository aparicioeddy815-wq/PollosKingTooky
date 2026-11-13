package com.kingtokky.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.kingtokky.domain.model.pedido.EstadoPedido;
import com.kingtokky.domain.regla.ReglaEstadoPedido;
import org.junit.jupiter.api.Test;

class ReglaEstadoPedidoTest {

    @Test
    void unaComandaEnPreparacionPuedePasarAListoOCancelarse() {
        assertThat(ReglaEstadoPedido.puedePasar(EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO)).isTrue();
        assertThat(ReglaEstadoPedido.puedePasar(EstadoPedido.EN_PREPARACION, EstadoPedido.CANCELADO)).isTrue();
    }

    @Test
    void unaComandaListaSoloPuedeEntregarse() {
        assertThat(ReglaEstadoPedido.puedePasar(EstadoPedido.LISTO, EstadoPedido.ENTREGADO)).isTrue();
        assertThat(ReglaEstadoPedido.puedePasar(EstadoPedido.LISTO, EstadoPedido.CANCELADO)).isFalse();
        assertThat(ReglaEstadoPedido.puedePasar(EstadoPedido.LISTO, EstadoPedido.EN_PREPARACION)).isFalse();
    }

    @Test
    void losEstadosFinalesNoCambian() {
        for (EstadoPedido destino : EstadoPedido.values()) {
            assertThat(ReglaEstadoPedido.puedePasar(EstadoPedido.ENTREGADO, destino)).isFalse();
            assertThat(ReglaEstadoPedido.puedePasar(EstadoPedido.CANCELADO, destino)).isFalse();
        }
    }
}
