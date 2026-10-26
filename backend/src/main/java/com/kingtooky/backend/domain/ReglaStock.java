package com.kingtooky.backend.domain;

import java.math.BigDecimal;

public final class ReglaStock {

    private ReglaStock() {
    }

    public static boolean requiereAlerta(BigDecimal stockActual, BigDecimal stockMinimo) {
        if (stockActual == null || stockMinimo == null) {
            return false;
        }
        return stockActual.compareTo(stockMinimo) <= 0;
    }

    public static boolean stockSuficiente(BigDecimal stockActual, BigDecimal cantidadRequerida) {
        return stockActual.compareTo(cantidadRequerida) >= 0;
    }
}