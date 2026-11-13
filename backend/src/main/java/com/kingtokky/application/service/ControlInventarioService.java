package com.kingtokky.application.service;

import com.kingtokky.application.dto.InventarioDtos.ConteoRequest;
import com.kingtokky.application.dto.InventarioDtos.ControlDetalleResponse;
import com.kingtokky.application.dto.InventarioDtos.ControlResponse;
import com.kingtokky.application.dto.InventarioDtos.LineaConteo;
import com.kingtokky.application.dto.InventarioDtos.LineaControlResponse;
import com.kingtokky.application.dto.InventarioDtos.ProgramacionControlesResponse;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.inventario.ControlInventario;
import com.kingtokky.domain.model.inventario.ControlInventarioDetalle;
import com.kingtokky.domain.model.inventario.EstadoControl;
import com.kingtokky.domain.model.inventario.Insumo;
import com.kingtokky.domain.model.inventario.TipoControl;
import com.kingtokky.domain.model.inventario.TipoInsumo;
import com.kingtokky.domain.regla.ReglasPersonal;
import com.kingtokky.domain.repository.ControlInventarioDetalleRepository;
import com.kingtokky.domain.repository.ControlInventarioRepository;
import com.kingtokky.domain.repository.InsumoRepository;
import com.kingtokky.presentation.exception.ApiException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Controles físicos de inventario: semanal (alimentos, sábados) y quincenal (utensilios, mesas y vasos, días 15 y 30). */
@Service
@Transactional(readOnly = true)
public class ControlInventarioService {

    private final ControlInventarioRepository controles;
    private final ControlInventarioDetalleRepository detalles;
    private final InsumoRepository insumos;
    private final InventarioService inventario;
    private final AuditoriaService auditoria;

    public ControlInventarioService(ControlInventarioRepository controles, ControlInventarioDetalleRepository detalles,
                                    InsumoRepository insumos, InventarioService inventario, AuditoriaService auditoria) {
        this.controles = controles;
        this.detalles = detalles;
        this.insumos = insumos;
        this.inventario = inventario;
        this.auditoria = auditoria;
    }

    public List<ControlResponse> listar(int limite) {
        return controles.findAllByOrderByFechaDescIdDesc(PageRequest.of(0, Math.min(Math.max(limite, 1), 50))).stream().map(this::aControl).toList();
    }

    public ControlDetalleResponse obtener(Long id) {
        return aDetalle(buscar(id));
    }

    public ProgramacionControlesResponse programacion() {
        LocalDate hoy = LocalDate.now();
        return new ProgramacionControlesResponse(ReglasPersonal.proximoSabado(hoy), ReglasPersonal.proximaQuincena(hoy),
                controles.findFirstByTipoAndEstado(TipoControl.SEMANAL, EstadoControl.ABIERTO).isPresent(),
                controles.findFirstByTipoAndEstado(TipoControl.QUINCENAL, EstadoControl.ABIERTO).isPresent());
    }

    @Transactional
    public ControlDetalleResponse crear(TipoControl tipo, Long idUsuario) {
        if (controles.findFirstByTipoAndEstado(tipo, EstadoControl.ABIERTO).isPresent()) {
            throw ApiException.conflicto("Ya hay un control " + tipo.name().toLowerCase() + " abierto. Ciérralo antes de crear otro");
        }
        ControlInventario c = new ControlInventario();
        c.setTipo(tipo);
        c.setIdUsuario(idUsuario);
        controles.save(c);
        TipoInsumo alcance = tipo == TipoControl.SEMANAL ? TipoInsumo.ALIMENTO : TipoInsumo.UTENSILIO;
        for (Insumo i : insumos.listarPorTipo(alcance)) {
            ControlInventarioDetalle d = new ControlInventarioDetalle();
            d.setControl(c);
            d.setInsumo(i);
            d.setStockSistema(i.getStockActual());
            detalles.save(d);
        }
        auditoria.registrar(TipoEvento.AJUSTE_STOCK, "Inventario", "Abrió el control de inventario " + tipo.name().toLowerCase() + " #" + c.getId(), NivelBitacora.INFO);
        return aDetalle(c);
    }

    /** Guarda lo contado por el personal; la diferencia se calcula contra el stock del sistema en ese momento. */
    @Transactional
    public ControlDetalleResponse registrarConteo(Long id, ConteoRequest req) {
        ControlInventario c = buscar(id);
        exigirAbierto(c);
        Map<Long, ControlInventarioDetalle> porInsumo = detalles.findByControl_IdOrderByInsumo_NombreAsc(id).stream()
                .collect(Collectors.toMap(d -> d.getInsumo().getId(), Function.identity()));
        for (LineaConteo linea : req.lineas()) {
            ControlInventarioDetalle d = porInsumo.get(linea.idInsumo());
            if (d == null) throw ApiException.solicitudInvalida("El insumo " + linea.idInsumo() + " no pertenece a este control");
            d.setStockSistema(d.getInsumo().getStockActual());
            d.setStockContado(linea.stockContado());
            d.setDiferencia(linea.stockContado().subtract(d.getStockSistema()));
        }
        return aDetalle(c);
    }

    /** Cierra el control: cada diferencia se aplica como ajuste en el kardex. */
    @Transactional
    public ControlDetalleResponse cerrar(Long id, Long idUsuario) {
        ControlInventario c = buscar(id);
        exigirAbierto(c);
        List<ControlInventarioDetalle> lineas = detalles.findByControl_IdOrderByInsumo_NombreAsc(id);
        long sinContar = lineas.stream().filter(d -> d.getStockContado() == null).count();
        if (sinContar > 0) {
            throw ApiException.reglaDeNegocio("Faltan " + sinContar + " insumos por contar antes de cerrar el control");
        }
        int ajustes = 0;
        for (ControlInventarioDetalle d : lineas) {
            if (d.getDiferencia() != null && d.getDiferencia().signum() != 0) {
                inventario.ajustarPorControl(d.getInsumo().getId(), d.getDiferencia(), idUsuario,
                        "Ajuste por control " + c.getTipo().name().toLowerCase() + " #" + c.getId());
                d.setAjustado(true);
                ajustes++;
            }
        }
        c.setEstado(EstadoControl.CERRADO);
        c.setCerradoEn(LocalDateTime.now());
        auditoria.registrar(TipoEvento.AJUSTE_STOCK, "Inventario", "Cerró el control de inventario " + c.getTipo().name().toLowerCase() + " #" + c.getId()
                + " con " + ajustes + " ajustes de conteo físico", NivelBitacora.ALERTA);
        return aDetalle(c);
    }

    /** Tarea programada diaria: abre el control que corresponda a la fecha (sábados y días 15/30). */
    @Transactional
    public void crearProgramados() {
        LocalDate hoy = LocalDate.now();
        if (ReglasPersonal.esDiaDeControlSemanal(hoy) && !controles.existsByTipoAndFecha(TipoControl.SEMANAL, hoy)
                && controles.findFirstByTipoAndEstado(TipoControl.SEMANAL, EstadoControl.ABIERTO).isEmpty()) {
            crear(TipoControl.SEMANAL, null);
        }
        if (ReglasPersonal.esDiaDeControlQuincenal(hoy) && !controles.existsByTipoAndFecha(TipoControl.QUINCENAL, hoy)
                && controles.findFirstByTipoAndEstado(TipoControl.QUINCENAL, EstadoControl.ABIERTO).isEmpty()) {
            crear(TipoControl.QUINCENAL, null);
        }
    }

    private ControlInventario buscar(Long id) {
        return controles.findById(id).orElseThrow(() -> ApiException.noEncontrado("Control de inventario no encontrado"));
    }

    private void exigirAbierto(ControlInventario c) {
        if (c.getEstado() != EstadoControl.ABIERTO) throw ApiException.reglaDeNegocio("Este control ya está cerrado");
    }

    private ControlResponse aControl(ControlInventario c) {
        return new ControlResponse(c.getId(), c.getTipo(), c.getFecha(), c.getEstado().name(), c.getObservacion(),
                detalles.countByControl_Id(c.getId()), detalles.countByControl_IdAndStockContadoIsNotNull(c.getId()), c.getCreadoEn(), c.getCerradoEn());
    }

    private ControlDetalleResponse aDetalle(ControlInventario c) {
        List<LineaControlResponse> lineas = detalles.findByControl_IdOrderByInsumo_NombreAsc(c.getId()).stream()
                .map(d -> new LineaControlResponse(d.getId(), d.getInsumo().getId(), d.getInsumo().getNombre(), d.getInsumo().getUnidadMedida(),
                        d.getStockSistema(), d.getStockContado(), d.getDiferencia(), d.isAjustado())).toList();
        return new ControlDetalleResponse(aControl(c), lineas);
    }
}
