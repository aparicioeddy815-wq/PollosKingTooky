package com.kingtokky.application.service;

import com.kingtokky.application.dto.DashboardDtos.DashboardAdminResponse;
import com.kingtokky.application.dto.DashboardDtos.DashboardGerenteResponse;
import com.kingtokky.application.dto.DashboardDtos.InsumoCritico;
import com.kingtokky.application.dto.DashboardDtos.MasVendido;
import com.kingtokky.application.dto.DashboardDtos.Operaciones;
import com.kingtokky.application.dto.DashboardDtos.RitmoVenta;
import com.kingtokky.application.dto.DashboardDtos.TopMenu;
import com.kingtokky.application.dto.DashboardDtos.VentaCategoria;
import com.kingtokky.domain.model.entrevista.EstadoEntrevista;
import com.kingtokky.domain.model.entrevista.EstadoPostulante;
import com.kingtokky.domain.model.inventario.EstadoAlerta;
import com.kingtokky.domain.model.inventario.Insumo;
import com.kingtokky.domain.model.pedido.EstadoPedido;
import com.kingtokky.domain.model.pedido.Pedido;
import com.kingtokky.domain.model.reserva.EstadoReserva;
import com.kingtokky.domain.repository.AlertaStockRepository;
import com.kingtokky.domain.repository.EntrevistaRepository;
import com.kingtokky.domain.repository.EvaluacionDiariaRepository;
import com.kingtokky.domain.repository.InsumoRepository;
import com.kingtokky.domain.repository.MermaRepository;
import com.kingtokky.domain.repository.PedidoRepository;
import com.kingtokky.domain.repository.PostulanteRepository;
import com.kingtokky.domain.repository.QuejaRepository;
import com.kingtokky.domain.repository.RefreshTokenRepository;
import com.kingtokky.domain.repository.ReservaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cifras de los dashboards del gerente (operación del día) y del administrador (dueños). */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final Set<EstadoPedido> EN_CURSO = EnumSet.of(EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO);

    private final PedidoRepository pedidos;
    private final InsumoRepository insumos;
    private final QuejaRepository quejas;
    private final EvaluacionDiariaRepository evaluaciones;
    private final EntrevistaRepository entrevistas;
    private final PostulanteRepository postulantes;
    private final MermaRepository mermas;
    private final AlertaStockRepository alertas;
    private final ReservaRepository reservas;
    private final RefreshTokenRepository refreshTokens;
    private final PedidoService pedidoService;
    private final BitacoraService bitacoraService;
    private final ConfiguracionService config;

    public DashboardService(PedidoRepository pedidos, InsumoRepository insumos, QuejaRepository quejas, EvaluacionDiariaRepository evaluaciones,
                            EntrevistaRepository entrevistas, PostulanteRepository postulantes, MermaRepository mermas, AlertaStockRepository alertas,
                            ReservaRepository reservas, RefreshTokenRepository refreshTokens, PedidoService pedidoService,
                            BitacoraService bitacoraService, ConfiguracionService config) {
        this.pedidos = pedidos;
        this.insumos = insumos;
        this.quejas = quejas;
        this.evaluaciones = evaluaciones;
        this.entrevistas = entrevistas;
        this.postulantes = postulantes;
        this.mermas = mermas;
        this.alertas = alertas;
        this.reservas = reservas;
        this.refreshTokens = refreshTokens;
        this.pedidoService = pedidoService;
        this.bitacoraService = bitacoraService;
        this.config = config;
    }

    public DashboardGerenteResponse gerente(LocalDate fechaPedida) {
        LocalDate fecha = fechaPedida != null ? fechaPedida : LocalDate.now();
        LocalDate inicioMes = fecha.withDayOfMonth(1);
        LocalDateTime desdeMes = inicioMes.atStartOfDay();
        LocalDateTime hastaMes = fecha.plusDays(1).atStartOfDay();

        List<Insumo> bajoUmbral = insumos.listarBajoUmbral().stream().filter(i -> i.getCategoria().getTipo().name().equals("ALIMENTO")).toList();
        List<InsumoCritico> criticos = bajoUmbral.stream().limit(6)
                .map(i -> new InsumoCritico(i.getId(), i.getNombre(), i.getUnidadMedida(), i.getStockActual(), i.getUmbralMinimo())).toList();
        List<Object[]> ranking = pedidos.productosMasVendidos(fecha, fecha, EstadoPedido.CANCELADO, PageRequest.of(0, 1));
        MasVendido masVendido = ranking.isEmpty() ? null : new MasVendido((String) ranking.get(0)[1], ((Number) ranking.get(0)[2]).longValue());
        List<VentaCategoria> porCategoria = pedidos.ventasPorCategoria(fecha, fecha, EstadoPedido.CANCELADO).stream()
                .map(f -> new VentaCategoria((String) f[0], (String) f[1], (BigDecimal) f[2])).toList();
        Double satisfaccion = evaluaciones.promedioEntre(fecha.minusDays(30), fecha);
        LocalDateTime inicioDia = fecha.atStartOfDay();

        return new DashboardGerenteResponse(fecha, pedidos.ventasEntre(fecha, fecha, EstadoPedido.CANCELADO),
                pedidos.countByFechaTurnoAndEstado(fecha, EstadoPedido.ENTREGADO), pedidos.countByFechaTurnoAndEstadoIn(fecha, EN_CURSO),
                bajoUmbral.size(), criticos, masVendido, porCategoria,
                quejas.countByCreadoEnBetween(desdeMes, hastaMes), quejas.countByEstadoNot(com.kingtokky.domain.model.cliente.EstadoQueja.RESPONDIDA),
                entrevistas.countByEstadoAndFechaHoraBetween(EstadoEntrevista.PROGRAMADA, inicioDia, inicioDia.plusDays(1)),
                postulantes.countByEstadoIn(List.of(EstadoPostulante.POSTULADO, EstadoPostulante.ENTREVISTA_AGENDADA, EstadoPostulante.EN_EVALUACION,
                        EstadoPostulante.APROBADO_GERENTE, EstadoPostulante.EN_REVISION_ANTECEDENTES)),
                satisfaccion == null ? 0 : Math.round(satisfaccion / 5.0 * 1000) / 10.0,
                pedidoService.tiempoPromedioDespachoMin(inicioMes, fecha), config.entero("objetivo_despacho_min", 8),
                mermas.costoEntre(desdeMes, hastaMes), reservasPendientesDelDia(fecha));
    }

    public DashboardAdminResponse admin(LocalDate fechaPedida) {
        LocalDate fecha = fechaPedida != null ? fechaPedida : LocalDate.now();
        LocalDate inicioMes = fecha.withDayOfMonth(1);
        BigDecimal ventas = pedidos.ventasEntre(fecha, fecha, EstadoPedido.CANCELADO);

        // Margen operativo del mes en curso: (ingresos - costo de lo vendido - mermas) / ingresos
        BigDecimal ingresosMes = pedidos.ventasEntre(inicioMes, fecha, EstadoPedido.CANCELADO);
        BigDecimal costoMes = pedidos.costoDeVentas(inicioMes, fecha, EstadoPedido.CANCELADO);
        BigDecimal mermasMes = mermas.costoEntre(inicioMes.atStartOfDay(), fecha.plusDays(1).atStartOfDay());
        double margen = ingresosMes.signum() == 0 ? 0 : ingresosMes.subtract(costoMes).subtract(mermasMes)
                .multiply(BigDecimal.valueOf(100)).divide(ingresosMes, 1, RoundingMode.HALF_UP).doubleValue();

        List<RitmoVenta> ritmo = ritmoPorHora(fecha);
        List<TopMenu> top = pedidos.productosMasVendidos(fecha, fecha, EstadoPedido.CANCELADO, PageRequest.of(0, 5)).stream()
                .map(f -> new TopMenu(((Number) f[0]).longValue(), (String) f[1], ((Number) f[2]).longValue(), (BigDecimal) f[3])).toList();

        Insumo pollo = insumos.findByCodigo("INS-POLLO").orElse(null);
        BigDecimal stockPollo = pollo == null ? BigDecimal.ZERO : pollo.getStockActual();
        BigDecimal umbralPollo = pollo == null ? BigDecimal.ZERO : pollo.getUmbralMinimo();
        double cobertura = pollo == null || pollo.getConsumoPromedioDiario().signum() == 0 ? 0
                : stockPollo.divide(pollo.getConsumoPromedioDiario(), 1, RoundingMode.HALF_UP).doubleValue();
        Operaciones operaciones = new Operaciones(pedidoService.tiempoPromedioDespachoMin(fecha, fecha), config.entero("objetivo_despacho_min", 8),
                quejas.countByCreadoEnBetween(inicioMes.atStartOfDay(), fecha.plusDays(1).atStartOfDay()), stockPollo, umbralPollo, cobertura,
                alertas.countByEstado(EstadoAlerta.ABIERTA));

        return new DashboardAdminResponse(fecha, ventas, variacionContraPromedio(fecha, ventas), pedidos.countByFechaTurnoAndEstado(fecha, EstadoPedido.ENTREGADO),
                margen, refreshTokens.contarSesionesVivas(LocalDateTime.now(), LocalDateTime.now().minusMinutes(30)), ritmo, top, operaciones,
                bitacoraService.recientes());
    }

    private long reservasPendientesDelDia(LocalDate fecha) {
        return reservas.gestion(false, EstadoReserva.PENDIENTE_PAGO, false, fecha, PageRequest.of(0, 1)).getTotalElements();
    }

    /** Ventas por hora del día (de 10:00 a 23:00) para el gráfico de "ritmo de ventas". */
    private List<RitmoVenta> ritmoPorHora(LocalDate fecha) {
        BigDecimal[] montos = new BigDecimal[24];
        long[] cantidades = new long[24];
        for (int h = 0; h < 24; h++) montos[h] = BigDecimal.ZERO;
        for (Pedido p : pedidos.vigentesDelDia(fecha, EstadoPedido.CANCELADO)) {
            int h = p.getCreadoEn().getHour();
            montos[h] = montos[h].add(p.getTotal());
            cantidades[h]++;
        }
        List<RitmoVenta> ritmo = new ArrayList<>();
        for (int h = 10; h <= 23; h++) ritmo.add(new RitmoVenta(h, montos[h], cantidades[h]));
        return ritmo;
    }

    /** Compara con el promedio de los 7 días anteriores a la misma hora del día, para que "hoy" sea comparable. */
    private double variacionContraPromedio(LocalDate fecha, BigDecimal ventasDelDia) {
        LocalTime hasta = fecha.equals(LocalDate.now()) ? LocalTime.now() : LocalTime.MAX;
        BigDecimal suma = BigDecimal.ZERO;
        for (int i = 1; i <= 7; i++) {
            for (Pedido p : pedidos.vigentesDelDia(fecha.minusDays(i), EstadoPedido.CANCELADO)) {
                if (!p.getCreadoEn().toLocalTime().isAfter(hasta)) suma = suma.add(p.getTotal());
            }
        }
        BigDecimal promedio = suma.divide(BigDecimal.valueOf(7), 2, RoundingMode.HALF_UP);
        if (promedio.signum() == 0) return 0;
        return ventasDelDia.subtract(promedio).multiply(BigDecimal.valueOf(100)).divide(promedio, 1, RoundingMode.HALF_UP).doubleValue();
    }
}
