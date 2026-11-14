package com.kingtokky.application.service;

import com.kingtokky.application.dto.GraficasDtos.Cobertura;
import com.kingtokky.application.dto.GraficasDtos.GraficasInventarioResponse;
import com.kingtokky.application.dto.GraficasDtos.MovimientoDia;
import com.kingtokky.application.dto.GraficasDtos.Punto;
import com.kingtokky.application.dto.GraficasDtos.VentaDia;
import com.kingtokky.domain.model.inventario.Insumo;
import com.kingtokky.domain.model.inventario.Merma;
import com.kingtokky.domain.model.inventario.MotivoMerma;
import com.kingtokky.domain.model.inventario.MovimientoInventario;
import com.kingtokky.domain.model.inventario.TipoInsumo;
import com.kingtokky.domain.model.pedido.EstadoPedido;
import com.kingtokky.domain.regla.ProyeccionStock;
import com.kingtokky.domain.repository.InsumoRepository;
import com.kingtokky.domain.repository.MermaRepository;
import com.kingtokky.domain.repository.MovimientoInventarioRepository;
import com.kingtokky.domain.repository.PedidoRepository;
import com.kingtokky.infrastructure.config.KingTokkyProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Series ya agrupadas para dibujar gráficas en el frontend (inventario y ventas). */
@Service
@Transactional(readOnly = true)
public class GraficasService {

    private static final BigDecimal UMBRAL_MINIMO_DIVISOR = new BigDecimal("0.001");

    private final InsumoRepository insumos;
    private final MovimientoInventarioRepository movimientos;
    private final MermaRepository mermas;
    private final PedidoRepository pedidos;
    private final KingTokkyProperties propiedades;

    public GraficasService(InsumoRepository insumos, MovimientoInventarioRepository movimientos, MermaRepository mermas,
                           PedidoRepository pedidos, KingTokkyProperties propiedades) {
        this.insumos = insumos;
        this.movimientos = movimientos;
        this.mermas = mermas;
        this.pedidos = pedidos;
        this.propiedades = propiedades;
    }

    public GraficasInventarioResponse inventario(int dias) {
        int ventana = limitar(dias);
        LocalDate hoy = LocalDate.now();
        BigDecimal factor = propiedades.inventario().factorStockBajo();
        List<Insumo> lista = insumos.listarPorTipo(TipoInsumo.ALIMENTO);

        // 1) Valor del stock por categoría y cantidad de insumos por estado
        Map<String, BigDecimal> valorPorCategoria = new HashMap<>();
        Map<String, Long> conteoEstados = new LinkedHashMap<>();
        conteoEstados.put(ProyeccionStock.CRITICO, 0L);
        conteoEstados.put(ProyeccionStock.BAJO, 0L);
        conteoEstados.put(ProyeccionStock.NORMAL, 0L);
        for (Insumo i : lista) {
            valorPorCategoria.merge(i.getCategoria().getNombre(), i.getStockActual().multiply(i.getCostoUnitario()), BigDecimal::add);
            conteoEstados.merge(ProyeccionStock.estado(i.getStockActual(), i.getUmbralMinimo(), factor), 1L, Long::sum);
        }
        List<Punto> categorias = valorPorCategoria.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .map(e -> new Punto(e.getKey(), dinero(e.getValue())))
                .toList();
        List<Punto> estados = conteoEstados.entrySet().stream()
                .map(e -> new Punto(e.getKey(), BigDecimal.valueOf(e.getValue())))
                .toList();

        // 2) Mermas de los últimos 30 días por motivo (valoradas en Bs)
        Map<MotivoMerma, BigDecimal> porMotivo = new EnumMap<>(MotivoMerma.class);
        for (MotivoMerma motivo : MotivoMerma.values()) {
            porMotivo.put(motivo, BigDecimal.ZERO);
        }
        for (Merma m : mermas.findByFechaHoraBetweenOrderByFechaHoraDesc(hoy.minusDays(29).atStartOfDay(), hoy.plusDays(1).atStartOfDay())) {
            porMotivo.merge(m.getMotivo(), m.getCostoTotal(), BigDecimal::add);
        }
        List<Punto> mermasMotivo = porMotivo.entrySet().stream()
                .map(e -> new Punto(e.getKey().name(), dinero(e.getValue())))
                .toList();

        // 3) Entradas, consumo y mermas por día (valoradas en Bs, porque los insumos tienen unidades distintas)
        LocalDate desde = hoy.minusDays(ventana - 1L);
        Map<LocalDate, BigDecimal[]> porDia = new TreeMap<>();
        for (int k = 0; k < ventana; k++) {
            porDia.put(desde.plusDays(k), new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO});
        }
        for (MovimientoInventario m : movimientos.desdeConInsumo(desde.atStartOfDay())) {
            BigDecimal[] acumulado = porDia.get(m.getFechaHora().toLocalDate());
            if (acumulado == null) continue;
            BigDecimal valor = m.getVariacion().abs().multiply(m.getInsumo().getCostoUnitario());
            switch (m.getTipo()) {
                case ENTRADA -> acumulado[0] = acumulado[0].add(valor);
                case CONSUMO -> acumulado[1] = acumulado[1].add(valor);
                case MERMA -> acumulado[2] = acumulado[2].add(valor);
                default -> { }
            }
        }
        List<MovimientoDia> diarios = porDia.entrySet().stream()
                .map(e -> new MovimientoDia(e.getKey(), dinero(e.getValue()[0]), dinero(e.getValue()[1]), dinero(e.getValue()[2])))
                .toList();

        // 4) Los 8 insumos con menos stock respecto a su mínimo
        List<Cobertura> cobertura = lista.stream()
                .sorted(Comparator.comparing(this::razonStock))
                .limit(8)
                .map(i -> new Cobertura(i.getId(), i.getNombre(), i.getUnidadMedida(), i.getStockActual(), i.getUmbralMinimo(),
                        i.getConsumoPromedioDiario(), diasDeCobertura(i),
                        ProyeccionStock.estado(i.getStockActual(), i.getUmbralMinimo(), factor)))
                .toList();

        return new GraficasInventarioResponse(ventana, categorias, estados, mermasMotivo, diarios, cobertura);
    }

    public List<VentaDia> ventas(int dias) {
        int ventana = limitar(dias);
        LocalDate hoy = LocalDate.now();
        LocalDate desde = hoy.minusDays(ventana - 1L);
        Map<LocalDate, BigDecimal> porDia = new TreeMap<>();
        for (int k = 0; k < ventana; k++) {
            porDia.put(desde.plusDays(k), BigDecimal.ZERO);
        }
        for (Object[] fila : pedidos.ventasPorDia(desde, hoy, EstadoPedido.CANCELADO)) {
            LocalDate fecha = fila[0] instanceof LocalDate d ? d : LocalDate.parse(fila[0].toString());
            porDia.put(fecha, new BigDecimal(fila[1].toString()));
        }
        return porDia.entrySet().stream().map(e -> new VentaDia(e.getKey(), dinero(e.getValue()))).toList();
    }

    // ------------------------------------------------------------------ internos

    private int limitar(int dias) {
        return Math.max(3, Math.min(dias, 60));
    }

    private BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal razonStock(Insumo i) {
        return i.getStockActual().divide(i.getUmbralMinimo().max(UMBRAL_MINIMO_DIVISOR), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal diasDeCobertura(Insumo i) {
        BigDecimal consumo = i.getConsumoPromedioDiario();
        if (consumo == null || consumo.signum() <= 0) return null;
        return i.getStockActual().divide(consumo, 1, RoundingMode.HALF_UP);
    }
}