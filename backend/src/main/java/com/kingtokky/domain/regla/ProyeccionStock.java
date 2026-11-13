package com.kingtokky.domain.regla;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Cálculo del estado de stock y de la proyección de consumo de un insumo. */
public final class ProyeccionStock {

    public static final String CRITICO = "CRITICO";
    public static final String BAJO = "BAJO";
    public static final String NORMAL = "NORMAL";

    private ProyeccionStock() {}

    public record Resultado(String estado, BigDecimal consumoProyectado, BigDecimal deficitProyectado, BigDecimal superavitSeguro) {}

    public static Resultado calcular(BigDecimal stock, BigDecimal umbral, BigDecimal consumoDiario, int dias, BigDecimal factorBajo) {
        BigDecimal consumo = consumoDiario.multiply(BigDecimal.valueOf(dias)).setScale(3, RoundingMode.HALF_UP);
        BigDecimal deficit = consumo.subtract(stock).max(BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP);
        BigDecimal superavit = stock.subtract(consumo).subtract(umbral).max(BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP);
        return new Resultado(estado(stock, umbral, factorBajo), consumo, deficit, superavit);
    }

    public static String estado(BigDecimal stock, BigDecimal umbral, BigDecimal factorBajo) {
        if (stock.compareTo(umbral) <= 0) return CRITICO;
        if (stock.compareTo(umbral.multiply(factorBajo)) <= 0) return BAJO;
        return NORMAL;
    }
}
