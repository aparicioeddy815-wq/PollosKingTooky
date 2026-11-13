package com.kingtokky.application.dto;

import com.kingtokky.application.dto.BitacoraDtos.BitacoraResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {}

    public record InsumoCritico(Long idInsumo, String nombre, String unidadMedida, BigDecimal stockActual, BigDecimal stockMinimo) {}

    public record MasVendido(String nombre, long unidades) {}

    public record VentaCategoria(String categoria, String nombre, BigDecimal monto) {}

    /** Los primeros campos coinciden con la interfaz DashboardGerente del frontend; los demás son adicionales. */
    public record DashboardGerenteResponse(LocalDate fecha, BigDecimal ventasDelDia, long pedidosAtendidos, long pedidosEnCurso, long stockCritico,
                                           List<InsumoCritico> insumosCriticos, MasVendido masVendido, List<VentaCategoria> ventasPorCategoria,
                                           long quejasDelMes, long quejasPendientes, long entrevistasHoy, long postulantesActivos,
                                           double satisfaccionPorcentaje, double tiempoPromedioDespachoMin, int objetivoDespachoMin,
                                           BigDecimal mermasDelMes, long reservasPendientesHoy) {}

    public record RitmoVenta(int hora, BigDecimal monto, long pedidos) {}

    public record TopMenu(Long idProducto, String nombre, long unidades, BigDecimal monto) {}

    public record Operaciones(double tiempoPromedioDespachoMin, int objetivoDespachoMin, long quejasDelMes, BigDecimal stockPolloKg,
                              BigDecimal umbralPolloKg, double diasCoberturaPollo, long alertasDeStock) {}

    public record DashboardAdminResponse(LocalDate fecha, BigDecimal ventasTotales, double variacionVsPromedioPct, long comandasAtendidas,
                                         double margenOperativoPct, long sesionesEnVivo, List<RitmoVenta> ritmoDeVentas, List<TopMenu> topMenu,
                                         Operaciones operaciones, List<BitacoraResponse> bitacoraReciente) {}
}
