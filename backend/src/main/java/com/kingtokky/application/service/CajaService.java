package com.kingtokky.application.service;

import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.pedido.EstadoPedido;
import com.kingtokky.domain.model.reporte.CierreCaja;
import com.kingtokky.domain.model.reporte.TipoCierre;
import com.kingtokky.domain.repository.CierreCajaRepository;
import com.kingtokky.domain.repository.PedidoRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Cierres de caja intermedios (cambio de turno) y finales del día. */
@Service
@Transactional(readOnly = true)
public class CajaService {

    private final CierreCajaRepository cierres;
    private final PedidoRepository pedidos;
    private final AuditoriaService auditoria;

    public CajaService(CierreCajaRepository cierres, PedidoRepository pedidos, AuditoriaService auditoria) {
        this.cierres = cierres;
        this.pedidos = pedidos;
        this.auditoria = auditoria;
    }

    public List<CierreCaja> delDia(LocalDate fecha) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        return cierres.findByFechaHoraBetweenOrderByFechaHoraDesc(dia.atStartOfDay(), dia.plusDays(1).atStartOfDay());
    }

    @Transactional
    public CierreCaja registrar(TipoCierre tipo, String observacion, Long idUsuario) {
        LocalDate hoy = LocalDate.now();
        CierreCaja c = new CierreCaja();
        c.setTipo(tipo);
        c.setTotalVentas(pedidos.ventasEntre(hoy, hoy, EstadoPedido.CANCELADO));
        c.setTotalPedidos((int) pedidos.contarEntre(hoy, hoy, EstadoPedido.CANCELADO));
        c.setIdUsuario(idUsuario);
        c.setObservacion(observacion == null || observacion.isBlank() ? null : observacion.trim());
        cierres.save(c);
        BigDecimal total = c.getTotalVentas();
        auditoria.registrar(TipoEvento.CIERRE_CAJA, "Caja", "Registró cierre " + tipo.name().toLowerCase() + " de caja por Bs " + total
                + " (" + c.getTotalPedidos() + " pedidos)", NivelBitacora.INFO);
        return c;
    }

}
