package com.kingtokky.application.dto;

import com.kingtokky.domain.model.pedido.CanalPedido;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ReporteDtos {

    private ReporteDtos() {}

    public record CanalVenta(CanalPedido canal, BigDecimal monto, long pedidos, double porcentaje) {}

    public record SemanaVenta(String semana, LocalDate desde, LocalDate hasta, BigDecimal monto) {}

    public record DistribucionCategoria(String categoria, String nombre, BigDecimal ingresos, double porcentajeIngresos, double margenNetoEstimadoPct) {}

    public record ReporteMensualResponse(String mes, BigDecimal ingresos, BigDecimal costoAlimento, BigDecimal costoMermas, double margenOperativoPct,
                                         double desperdicioPct, long pedidos, BigDecimal ticketPromedio, List<CanalVenta> canales,
                                         List<SemanaVenta> evolucionSemanal, List<DistribucionCategoria> distribucion) {}

    public record ReporteGeneradoResponse(Long idReporte, String nombre, String tipo, String formato, String periodo, LocalDateTime generadoEn,
                                          long tamanoBytes, boolean programado, String generadoPor) {}

    public record ResumenArchivosResponse(long totalInformes, long programados) {}

    public record RespaldoResponse(Long idRespaldo, LocalDateTime fechaHora, String archivo, long tamanoBytes, String estado, long duracionMs,
                                   String mensaje, boolean disponible) {}
}
