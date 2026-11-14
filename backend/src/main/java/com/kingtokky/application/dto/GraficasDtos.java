package com.kingtokky.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class GraficasDtos {

    private GraficasDtos() {}

    public record Punto(String etiqueta, BigDecimal valor) {}

    public record MovimientoDia(LocalDate fecha, BigDecimal entradas, BigDecimal consumos, BigDecimal mermas) {}

    public record Cobertura(Long idInsumo, String nombre, String unidadMedida, BigDecimal stockActual, BigDecimal umbralMinimo,
                            BigDecimal consumoPromedioDiario, BigDecimal diasCobertura, String estado) {}

    public record GraficasInventarioResponse(int dias, List<Punto> valorPorCategoria, List<Punto> estadoDelStock,
                                             List<Punto> mermasPorMotivo, List<MovimientoDia> movimientosDiarios,
                                             List<Cobertura> menorCobertura) {}

    public record VentaDia(LocalDate fecha, BigDecimal monto) {}
}