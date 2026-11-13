package com.kingtokky.infrastructure.scheduler;

import com.kingtokky.application.service.ControlInventarioService;
import com.kingtokky.application.service.InventarioService;
import com.kingtokky.application.service.ReporteService;
import com.kingtokky.application.service.ReservaService;
import com.kingtokky.application.service.RespaldoService;
import com.kingtokky.domain.repository.RefreshTokenRepository;
import com.kingtokky.infrastructure.config.KingTokkyProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Tareas automáticas del sistema: reservas vencidas, alertas de stock, controles, informes y respaldos. */
@Component
public class TareasProgramadas {

    private static final Logger log = LoggerFactory.getLogger(TareasProgramadas.class);

    private final ReservaService reservas;
    private final InventarioService inventario;
    private final ControlInventarioService controles;
    private final ReporteService reportes;
    private final RespaldoService respaldos;
    private final RefreshTokenRepository refreshTokens;
    private final KingTokkyProperties propiedades;

    public TareasProgramadas(ReservaService reservas, InventarioService inventario, ControlInventarioService controles,
                             ReporteService reportes, RespaldoService respaldos, RefreshTokenRepository refreshTokens,
                             KingTokkyProperties propiedades) {
        this.reservas = reservas;
        this.inventario = inventario;
        this.controles = controles;
        this.reportes = reportes;
        this.respaldos = respaldos;
        this.refreshTokens = refreshTokens;
        this.propiedades = propiedades;
    }

    /** Cada minuto: cancela las reservas cuya hora llegó sin pago en caja. */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void cancelarReservasVencidas() {
        int canceladas = reservas.cancelarVencidas();
        if (canceladas > 0) log.info("{} reserva(s) cancelada(s) automáticamente por falta de pago", canceladas);
    }

    /** Cada 15 minutos: revisa el stock de todos los insumos y abre o cierra alertas. */
    @Scheduled(cron = "0 */15 * * * *")
    public void revisarAlertasDeStock() {
        inventario.evaluarTodasLasAlertas();
    }

    /** Todos los días a las 07:00: abre el control de inventario que toque (sábados y días 15 y 30). */
    @Scheduled(cron = "0 0 7 * * *")
    public void abrirControlesProgramados() {
        controles.crearProgramados();
    }

    /** Cada 12 horas (03:00 y 15:00): respaldo de la base de datos. */
    @Scheduled(cron = "0 0 3,15 * * *")
    public void respaldarBaseDeDatos() {
        if (propiedades.respaldo().habilitado()) respaldos.ejecutar(true);
    }

    /** Domingos 23:30: informe de auditoría semanal. */
    @Scheduled(cron = "0 30 23 * * SUN")
    public void auditoriaSemanal() {
        reportes.generar(ReporteService.TIPO_AUDITORIA, "PDF", LocalDate.now().toString(), null, true);
    }

    /** Día 1 de cada mes a las 06:00: reporte financiero del mes anterior en PDF y Excel. */
    @Scheduled(cron = "0 0 6 1 * *")
    public void reporteMensual() {
        String mesAnterior = YearMonth.now().minusMonths(1).toString();
        reportes.generar(ReporteService.TIPO_VENTAS, "PDF", mesAnterior, null, true);
        reportes.generar(ReporteService.TIPO_VENTAS, "XLSX", mesAnterior, null, true);
    }

    /** Diario 04:00: elimina sesiones vencidas. */
    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void limpiarSesionesVencidas() {
        refreshTokens.borrarVencidos(LocalDateTime.now().minusDays(1));
    }
}
