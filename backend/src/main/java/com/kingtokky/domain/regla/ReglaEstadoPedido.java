package com.kingtokky.domain.regla;

import com.kingtokky.domain.model.pedido.EstadoPedido;

/** Transiciones válidas del ciclo de vida de una comanda. */
public final class ReglaEstadoPedido {

    private ReglaEstadoPedido() {}

    public static boolean puedePasar(EstadoPedido actual, EstadoPedido nuevo) {
        return switch (actual) {
            case EN_PREPARACION -> nuevo == EstadoPedido.LISTO || nuevo == EstadoPedido.CANCELADO;
            case LISTO -> nuevo == EstadoPedido.ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }
}
