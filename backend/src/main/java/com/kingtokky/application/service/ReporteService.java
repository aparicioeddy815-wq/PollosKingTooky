package com.kingtokky.application.service;

import com.kingtokky.application.dto.BitacoraDtos.BitacoraResponse;
import com.kingtokky.application.dto.DashboardDtos.DashboardAdminResponse;
import com.kingtokky.application.dto.InventarioDtos.InsumoResponse;
import com.kingtokky.application.dto.ReporteDtos.CanalVenta;
import com.kingtokky.application.dto.ReporteDtos.DistribucionCategoria;
import com.kingtokky.application.dto.ReporteDtos.ReporteGeneradoResponse;
import com.kingtokky.application.dto.ReporteDtos.ReporteMensualResponse;
import com.kingtokky.application.dto.ReporteDtos.ResumenArchivosResponse;
import com.kingtokky.application.dto.ReporteDtos.SemanaVenta;
import com.kingtokky.application.dto.comun.ArchivoDescarga;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.inventario.TipoInsumo;
import com.kingtokky.domain.model.pedido.CanalPedido;
import com.kingtokky.domain.model.pedido.EstadoPedido;
import com.kingtokky.domain.model.reporte.ReporteGenerado;
import com.kingtokky.domain.port.AlmacenArchivos;
import com.kingtokky.domain.repository.MermaRepository;
import com.kingtokky.domain.repository.PedidoRepository;
import com.kingtokky.domain.repository.ReporteGeneradoRepository;
import com.kingtokky.domain.repository.UsuarioRepository;
import com.kingtokky.infrastructure.export.ExportadorExcel;
import com.kingtokky.infrastructure.export.ExportadorPdf;
import com.kingtokky.presentation.exception.ApiException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Reportes financieros y operativos, su exportación (Excel/PDF) y el archivo de informes ya generados. */
@Service
@Transactional(readOnly = true)
public class ReporteService {

    public static final String TIPO_VENTAS = "VENTAS_MENSUAL";
    public static final String TIPO_INVENTARIO = "INVENTARIO";
    public static final String TIPO_AUDITORIA = "AUDITORIA";
    public static final String TIPO_RESUMEN_DIARIO = "RESUMEN_DIARIO";
    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private record Generado(byte[] contenido, String nombre, String tipoContenido) {}

    private final PedidoRepository pedidos;
    private final MermaRepository mermas;
    private final ReporteGeneradoRepository reportes;
    private final UsuarioRepository usuarios;
    private final ExportadorExcel excel;
    private final ExportadorPdf pdf;
    private final AlmacenArchivos almacen;
    private final InventarioService inventario;
    private final BitacoraService bitacora;
    private final DashboardService dashboard;
    private final AuditoriaService auditoria;

    public ReporteService(PedidoRepository pedidos, MermaRepository mermas, ReporteGeneradoRepository reportes, UsuarioRepository usuarios,
                          ExportadorExcel excel, ExportadorPdf pdf, AlmacenArchivos almacen, InventarioService inventario,
                          BitacoraService bitacora, DashboardService dashboard, AuditoriaService auditoria) {
        this.pedidos = pedidos;
        this.mermas = mermas;
        this.reportes = reportes;
        this.usuarios = usuarios;
        this.excel = excel;
        this.pdf = pdf;
        this.almacen = almacen;
        this.inventario = inventario;
        this.bitacora = bitacora;
        this.dashboard = dashboard;
        this.auditoria = auditoria;
    }

    // ------------------------------------------------------------------ reporte financiero

    public ReporteMensualResponse mensual(YearMonth mes) {
        LocalDate desde = mes.atDay(1);
        LocalDate hasta = mes.atEndOfMonth().isAfter(LocalDate.now()) ? LocalDate.now() : mes.atEndOfMonth();
        if (hasta.isBefore(desde)) hasta = desde;

        BigDecimal ingresos = pedidos.ventasEntre(desde, hasta, EstadoPedido.CANCELADO);
        BigDecimal costo = pedidos.costoDeVentas(desde, hasta, EstadoPedido.CANCELADO);
        BigDecimal costoMermas = mermas.costoEntre(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay());
        long cantidad = pedidos.contarEntre(desde, hasta, EstadoPedido.CANCELADO);

        double margen = porcentaje(ingresos.subtract(costo).subtract(costoMermas), ingresos);
        double desperdicio = porcentaje(costoMermas, costo);
        BigDecimal ticket = cantidad == 0 ? BigDecimal.ZERO : ingresos.divide(BigDecimal.valueOf(cantidad), 2, RoundingMode.HALF_UP);

        List<CanalVenta> canales = new ArrayList<>();
        for (Object[] f : pedidos.ventasPorCanal(desde, hasta, EstadoPedido.CANCELADO)) {
            BigDecimal monto = (BigDecimal) f[1];
            canales.add(new CanalVenta((CanalPedido) f[0], monto, ((Number) f[2]).longValue(), porcentaje(monto, ingresos)));
        }
        canales.sort((a, b) -> b.monto().compareTo(a.monto()));

        // Evolución semanal: días 1-7, 8-14, 15-21, 22-28 y el resto del mes
        BigDecimal[] porSemana = new BigDecimal[5];
        for (int i = 0; i < 5; i++) porSemana[i] = BigDecimal.ZERO;
        for (Object[] f : pedidos.ventasPorDia(desde, hasta, EstadoPedido.CANCELADO)) {
            int semana = Math.min(4, (((LocalDate) f[0]).getDayOfMonth() - 1) / 7);
            porSemana[semana] = porSemana[semana].add((BigDecimal) f[1]);
        }
        List<SemanaVenta> semanas = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            LocalDate ini = desde.plusDays(i * 7L);
            if (ini.isAfter(hasta)) break;
            LocalDate fin = i == 4 ? mes.atEndOfMonth() : ini.plusDays(6);
            semanas.add(new SemanaVenta("Semana " + (i + 1), ini, fin.isAfter(hasta) ? hasta : fin, porSemana[i]));
        }

        // Distribución de ganancia por categoría; las mermas se reparten en proporción a los ingresos
        List<Object[]> categorias = pedidos.ventasPorCategoria(desde, hasta, EstadoPedido.CANCELADO);
        BigDecimal totalCategorias = categorias.stream().map(f -> (BigDecimal) f[2]).reduce(BigDecimal.ZERO, BigDecimal::add);
        List<DistribucionCategoria> distribucion = new ArrayList<>();
        for (Object[] f : categorias) {
            BigDecimal ing = (BigDecimal) f[2];
            BigDecimal costoCat = (BigDecimal) f[3];
            BigDecimal mermasCat = totalCategorias.signum() == 0 ? BigDecimal.ZERO
                    : costoMermas.multiply(ing).divide(totalCategorias, 4, RoundingMode.HALF_UP);
            distribucion.add(new DistribucionCategoria((String) f[0], (String) f[1], ing, porcentaje(ing, totalCategorias),
                    porcentaje(ing.subtract(costoCat).subtract(mermasCat), ing)));
        }
        return new ReporteMensualResponse(mes.toString(), ingresos, costo, costoMermas, margen, desperdicio, cantidad, ticket, canales, semanas, distribucion);
    }

    // ------------------------------------------------------------------ archivo de informes

    public PaginaResponse<ReporteGeneradoResponse> listarArchivos(int pagina, int tamano) {
        var page = reportes.findAllByOrderByGeneradoEnDesc(PageRequest.of(pagina, Math.min(tamano, 100)));
        return PaginaResponse.de(page, this::aRespuesta);
    }

    public ResumenArchivosResponse resumenArchivos() {
        List<ReporteGenerado> todos = reportes.findAll();
        return new ResumenArchivosResponse(todos.size(), todos.stream().filter(ReporteGenerado::isProgramado).count());
    }

    /** Genera el informe, lo guarda en el almacén y lo registra en el archivo de informes. */
    @Transactional
    public ReporteGeneradoResponse generar(String tipo, String formato, String periodo, Long idUsuario, boolean programado) {
        String fmt = formato == null ? "PDF" : formato.toUpperCase();
        String per = periodo == null || periodo.isBlank() ? periodoPorDefecto(tipo) : periodo;
        Generado g = construir(tipo, fmt, per);
        ReporteGenerado r = new ReporteGenerado();
        r.setNombre(nombreLegible(tipo, per));
        r.setTipo(tipo);
        r.setFormato(fmt);
        r.setPeriodo(per);
        r.setIdUsuario(idUsuario);
        r.setProgramado(programado);
        r.setTamanoBytes((long) g.contenido().length);
        try {
            r.setRuta(almacen.guardar("reportes", g.nombre(), new ByteArrayInputStream(g.contenido())));
        } catch (IOException e) {
            throw ApiException.reglaDeNegocio("No se pudo guardar el informe generado");
        }
        reportes.save(r);
        if (programado) {
            auditoria.registrarSistema(TipoEvento.SISTEMA, "Reportes", "Se generó el informe programado \"" + r.getNombre() + "\" (" + fmt + ")", NivelBitacora.INFO);
        } else {
            auditoria.registrar(TipoEvento.SISTEMA, "Reportes", "Generó el informe \"" + r.getNombre() + "\" (" + fmt + ")", NivelBitacora.INFO);
        }
        return aRespuesta(r);
    }

    /** Descarga un informe: el archivo guardado o, si no está en disco (datos de ejemplo), se vuelve a generar. */
    public ArchivoDescarga descargar(Long id) {
        ReporteGenerado r = reportes.findById(id).orElseThrow(() -> ApiException.noEncontrado("Informe no encontrado"));
        if (almacen.existe(r.getRuta())) {
            return new ArchivoDescarga(nombreArchivo(r.getTipo(), r.getPeriodo(), r.getFormato()), tipoContenido(r.getFormato()), almacen.cargar(r.getRuta()));
        }
        Generado g = construir(r.getTipo(), r.getFormato(), r.getPeriodo());
        return new ArchivoDescarga(g.nombre(), g.tipoContenido(), new ByteArrayResource(g.contenido()));
    }

    public byte[] excelMensual(YearMonth mes) {
        return construir(TIPO_VENTAS, "XLSX", mes.toString()).contenido();
    }

    public byte[] pdfMensual(YearMonth mes) {
        return construir(TIPO_VENTAS, "PDF", mes.toString()).contenido();
    }

    // ------------------------------------------------------------------ construcción de archivos

    private Generado construir(String tipo, String formato, String periodo) {
        String nombre = nombreArchivo(tipo, periodo, formato);
        byte[] contenido = switch (tipo) {
            case TIPO_VENTAS -> "XLSX".equals(formato) ? ventasExcel(YearMonth.parse(periodo)) : ventasPdf(YearMonth.parse(periodo));
            case TIPO_INVENTARIO -> inventarioExcel();
            case TIPO_AUDITORIA -> auditoriaPdf();
            case TIPO_RESUMEN_DIARIO -> resumenDiarioPdf(LocalDate.parse(periodo));
            default -> throw ApiException.solicitudInvalida("Tipo de informe desconocido: " + tipo);
        };
        return new Generado(contenido, nombre, tipoContenido(formato));
    }

    private byte[] ventasExcel(YearMonth mes) {
        ReporteMensualResponse r = mensual(mes);
        List<List<Object>> resumen = List.of(
                List.of("Ingresos", r.ingresos()), List.of("Costo de alimentos", r.costoAlimento()), List.of("Costo de mermas", r.costoMermas()),
                List.of("Margen operativo (%)", r.margenOperativoPct()), List.of("Desperdicio (%)", r.desperdicioPct()),
                List.of("Pedidos", r.pedidos()), List.of("Ticket promedio", r.ticketPromedio()));
        return excel.generar(List.of(
                new ExportadorExcel.Hoja("Resumen " + mes, List.of("Indicador", "Valor"), resumen),
                new ExportadorExcel.Hoja("Canales de venta", List.of("Canal", "Monto (Bs)", "Pedidos", "% del total"),
                        r.canales().stream().map(c -> List.<Object>of(c.canal().name(), c.monto(), c.pedidos(), c.porcentaje())).toList()),
                new ExportadorExcel.Hoja("Evolución semanal", List.of("Semana", "Desde", "Hasta", "Ventas (Bs)"),
                        r.evolucionSemanal().stream().map(s -> List.<Object>of(s.semana(), s.desde().toString(), s.hasta().toString(), s.monto())).toList()),
                new ExportadorExcel.Hoja("Distribución por categoría", List.of("Categoría", "Ingresos (Bs)", "% de ingresos", "Margen neto estimado (%)"),
                        r.distribucion().stream().map(d -> List.<Object>of(d.nombre(), d.ingresos(), d.porcentajeIngresos(), d.margenNetoEstimadoPct())).toList())));
    }

    private byte[] ventasPdf(YearMonth mes) {
        ReporteMensualResponse r = mensual(mes);
        return pdf.documento("Reporte financiero " + mes, "Ingresos, márgenes y canales de venta", List.of(
                new ExportadorPdf.Seccion("Resumen del mes", List.of("Indicador", "Valor"), List.of(
                        List.of("Ingresos", "Bs " + r.ingresos()), List.of("Costo de alimentos", "Bs " + r.costoAlimento()),
                        List.of("Costo de mermas", "Bs " + r.costoMermas()), List.of("Margen operativo", r.margenOperativoPct() + " %"),
                        List.of("Desperdicio", r.desperdicioPct() + " %"), List.of("Pedidos", r.pedidos()), List.of("Ticket promedio", "Bs " + r.ticketPromedio()))),
                new ExportadorPdf.Seccion("Canales de venta", List.of("Canal", "Monto (Bs)", "Pedidos", "% del total"),
                        r.canales().stream().map(c -> List.<Object>of(c.canal().name(), c.monto(), c.pedidos(), c.porcentaje())).toList()),
                new ExportadorPdf.Seccion("Evolución semanal", List.of("Semana", "Desde", "Hasta", "Ventas (Bs)"),
                        r.evolucionSemanal().stream().map(s -> List.<Object>of(s.semana(), s.desde().toString(), s.hasta().toString(), s.monto())).toList()),
                new ExportadorPdf.Seccion("Distribución de ganancia", List.of("Categoría", "Ingresos (Bs)", "% ingresos", "Margen neto est. (%)"),
                        r.distribucion().stream().map(d -> List.<Object>of(d.nombre(), d.ingresos(), d.porcentajeIngresos(), d.margenNetoEstimadoPct())).toList())));
    }

    private byte[] inventarioExcel() {
        List<List<Object>> filas = new ArrayList<>();
        for (InsumoResponse i : inventario.listar(null, null, TipoInsumo.ALIMENTO, null)) {
            filas.add(List.of(i.codigo(), i.nombre(), i.categoria(), i.unidadMedida(), i.stockActual(), i.umbralMinimo(), i.estado(),
                    i.consumoPromedioDiario(), i.deficitProyectado(), i.costoUnitario(), i.valorStock()));
        }
        return excel.generar("Inventario valorizado", List.of("Código", "Insumo", "Categoría", "Unidad", "Stock", "Mínimo", "Estado",
                "Consumo prom./día", "Déficit proyectado", "Costo unitario (Bs)", "Valor (Bs)"), filas);
    }

    private byte[] auditoriaPdf() {
        List<BitacoraResponse> eventos = bitacora.listarParaExportar("TODOS", "TODOS", LocalDate.now().minusDays(7), LocalDate.now(), null);
        List<List<Object>> filas = eventos.stream().limit(400).map(b -> List.<Object>of(b.fechaHora().toString().replace('T', ' ').substring(0, 16),
                b.tipo().name(), b.modulo(), b.usuario(), b.accion(), b.nivel().name())).toList();
        return pdf.documento("Auditoría semanal", "Eventos de los últimos 7 días (máximo 400 más recientes)",
                List.of(new ExportadorPdf.Seccion("Bitácora", List.of("Fecha", "Tipo", "Módulo", "Usuario", "Acción", "Nivel"), filas)));
    }

    public byte[] resumenDiarioPdf(LocalDate fecha) {
        DashboardAdminResponse d = dashboard.admin(fecha);
        return pdf.documento("Resumen diario " + fecha, "Ventas y operación del día", List.of(
                new ExportadorPdf.Seccion("Indicadores", List.of("Indicador", "Valor"), List.of(
                        List.of("Ventas totales", "Bs " + d.ventasTotales()), List.of("Comandas atendidas", d.comandasAtendidas()),
                        List.of("Margen operativo (mes)", d.margenOperativoPct() + " %"), List.of("Tiempo promedio de despacho", d.operaciones().tiempoPromedioDespachoMin() + " min"),
                        List.of("Variación vs. promedio semanal", d.variacionVsPromedioPct() + " %"))),
                new ExportadorPdf.Seccion("Productos más vendidos", List.of("Producto", "Unidades", "Monto (Bs)"),
                        d.topMenu().stream().map(t -> List.<Object>of(t.nombre(), t.unidades(), t.monto())).toList())));
    }

    // ------------------------------------------------------------------ internos

    private double porcentaje(BigDecimal parte, BigDecimal total) {
        if (total == null || total.signum() == 0) return 0;
        return parte.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP).doubleValue();
    }

    private String periodoPorDefecto(String tipo) {
        return switch (tipo) {
            case TIPO_VENTAS -> YearMonth.now().toString();
            case TIPO_RESUMEN_DIARIO -> LocalDate.now().toString();
            default -> LocalDate.now().toString();
        };
    }

    private String nombreLegible(String tipo, String periodo) {
        return switch (tipo) {
            case TIPO_VENTAS -> "Ventas del mes " + periodo;
            case TIPO_INVENTARIO -> "Inventario valorizado " + periodo;
            case TIPO_AUDITORIA -> "Auditoría semanal " + periodo;
            case TIPO_RESUMEN_DIARIO -> "Resumen diario de ventas " + periodo;
            default -> tipo + " " + periodo;
        };
    }

    private String nombreArchivo(String tipo, String periodo, String formato) {
        String base = tipo.toLowerCase().replace('_', '-') + "-" + periodo.replaceAll("[^A-Za-z0-9-]", "");
        return "king-tokky-" + base + ("XLSX".equals(formato) ? ".xlsx" : ".pdf");
    }

    private String tipoContenido(String formato) {
        return "XLSX".equals(formato) ? XLSX : "application/pdf";
    }

    private ReporteGeneradoResponse aRespuesta(ReporteGenerado r) {
        String autor = r.getIdUsuario() == null ? "Sistema" : usuarios.findById(r.getIdUsuario()).map(u -> u.getNombreCompleto()).orElse("—");
        return new ReporteGeneradoResponse(r.getId(), r.getNombre(), r.getTipo(), r.getFormato(), r.getPeriodo(), r.getGeneradoEn(), r.getTamanoBytes(), r.isProgramado(), autor);
    }
}
