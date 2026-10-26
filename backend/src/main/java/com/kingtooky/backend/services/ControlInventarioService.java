package com.kingtooky.backend.services;

import com.kingtooky.backend.application.dto.ControlProgramacionResponse;
import com.kingtooky.backend.application.dto.ControlRequest;
import com.kingtooky.backend.application.dto.ControlResponse;
import com.kingtooky.backend.application.dto.DetalleControlRequest;
import com.kingtooky.backend.application.dto.DetalleControlResponse;
import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.infrastructure.entity.ControlInventarioEntity;
import com.kingtooky.backend.infrastructure.entity.DetalleControlInventarioEntity;
import com.kingtooky.backend.infrastructure.entity.InsumoEntity;
import com.kingtooky.backend.presentation.exception.RecursoNoEncontradoException;
import com.kingtooky.backend.presentation.exception.ReglaNegocioException;
import com.kingtooky.backend.repository.ControlInventarioJpaRepository;
import com.kingtooky.backend.repository.DetalleControlInventarioJpaRepository;
import com.kingtooky.backend.repository.InsumoJpaRepository;
import com.kingtooky.backend.security.application.SecurityUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class ControlInventarioService {

    public static final String ABIERTO = "ABIERTO";
    public static final String CERRADO = "CERRADO";

    private final ControlInventarioJpaRepository controlRepository;
    private final DetalleControlInventarioJpaRepository detalleRepository;
    private final InsumoJpaRepository insumoRepository;
    private final MovimientoInventarioService movimientoService;
    private final AuditoriaService auditoriaService;

    public ControlInventarioService(ControlInventarioJpaRepository controlRepository,
                                    DetalleControlInventarioJpaRepository detalleRepository,
                                    InsumoJpaRepository insumoRepository,
                                    MovimientoInventarioService movimientoService,
                                    AuditoriaService auditoriaService) {
        this.controlRepository = controlRepository;
        this.detalleRepository = detalleRepository;
        this.insumoRepository = insumoRepository;
        this.movimientoService = movimientoService;
        this.auditoriaService = auditoriaService;
    }

    public ControlProgramacionResponse programacion(LocalDate fecha) {
        LocalDate f = fecha != null ? fecha : LocalDate.now();
        int dia = f.getDayOfMonth();
        int ultimo = f.lengthOfMonth();
        ControlProgramacionResponse r = new ControlProgramacionResponse();
        r.setFecha(f);
        r.setSemanalCorresponde(f.getDayOfWeek() == DayOfWeek.SATURDAY);
        r.setQuincenalCorresponde(dia == 15 || dia == 30 || (ultimo < 30 && dia == ultimo));
        return r;
    }

    @Transactional
    public ControlResponse iniciar(ControlRequest request) {
        boolean yaAbierto = controlRepository.buscar(request.getTipo(), ABIERTO, Pageable.ofSize(1)).hasContent();
        if (yaAbierto) {
            throw new ReglaNegocioException("Ya existe un control " + request.getTipo() + " ABIERTO. Cierrelo antes de iniciar otro",
                    HttpStatus.CONFLICT);
        }
        ControlInventarioEntity c = new ControlInventarioEntity();
        c.setIdUsuario(SecurityUtil.idUsuarioActual());
        c.setTipo(request.getTipo());
        c.setFecha(LocalDate.now());
        c.setEstado(ABIERTO);
        c.setObservaciones(request.getObservaciones());
        ControlInventarioEntity guardado = controlRepository.save(c);
        auditoriaService.registrar("INICIAR_CONTROL", "control_inventario", guardado.getIdControl(),
                "Control " + guardado.getTipo() + " iniciado");
        return aRespuesta(guardado);
    }

    @Transactional
    public ControlResponse registrarConteo(Integer idControl, DetalleControlRequest request) {
        ControlInventarioEntity c = buscar(idControl);
        validarAbierto(c);
        InsumoEntity insumo = insumoRepository.findById(request.getIdInsumo())
                .orElseThrow(() -> new RecursoNoEncontradoException("Insumo no encontrado: " + request.getIdInsumo()));

        DetalleControlInventarioEntity d = detalleRepository
                .findByIdControlAndInsumo_IdInsumo(idControl, insumo.getIdInsumo())
                .orElseGet(() -> {
                    DetalleControlInventarioEntity nuevo = new DetalleControlInventarioEntity();
                    nuevo.setIdControl(idControl);
                    nuevo.setInsumo(insumo);
                    return nuevo;
                });
        d.setStockSistema(insumo.getStockActual());
        d.setStockFisico(request.getStockFisico().setScale(2, RoundingMode.HALF_UP));
        d.setDiferencia(d.getStockFisico().subtract(d.getStockSistema()));
        detalleRepository.save(d);
        return aRespuesta(c);
    }

    @Transactional
    public ControlResponse cerrar(Integer idControl, boolean ajustarStock) {
        ControlInventarioEntity c = buscar(idControl);
        validarAbierto(c);
        List<DetalleControlInventarioEntity> detalles = detalleRepository.findByIdControl(idControl);
        if (detalles.isEmpty()) {
            throw new ReglaNegocioException("No se puede cerrar un control sin conteos registrados");
        }
        for (DetalleControlInventarioEntity d : detalles) {
            InsumoEntity actual = insumoRepository.findById(d.getInsumo().getIdInsumo())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Insumo no encontrado: " + d.getInsumo().getIdInsumo()));
            d.setStockSistema(actual.getStockActual());
            d.setDiferencia(d.getStockFisico().subtract(actual.getStockActual()));
            detalleRepository.save(d);
            if (ajustarStock) {
                movimientoService.aplicarAjuste(actual.getIdInsumo(), d.getStockFisico(),
                        "Ajuste por control " + c.getTipo() + " #" + idControl);
            }
        }
        c.setEstado(CERRADO);
        ControlInventarioEntity guardado = controlRepository.save(c);
        auditoriaService.registrar("CERRAR_CONTROL", "control_inventario", idControl,
                "Control " + c.getTipo() + " cerrado" + (ajustarStock ? " con ajuste de stock" : " sin ajuste"));
        return aRespuesta(guardado);
    }

    @Transactional(readOnly = true)
    public ControlResponse obtener(Integer idControl) {
        return aRespuesta(buscar(idControl));
    }

    @Transactional(readOnly = true)
    public PageResponse<ControlResponse> listar(String tipo, String estado, Pageable pageable) {
        return PageUtil.toResponse(
                controlRepository.buscar(PageUtil.texto(tipo).toUpperCase(), PageUtil.texto(estado).toUpperCase(), pageable),
                this::aRespuesta);
    }

    private ControlInventarioEntity buscar(Integer id) {
        return controlRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Control de inventario no encontrado: " + id));
    }

    private void validarAbierto(ControlInventarioEntity c) {
        if (!ABIERTO.equals(c.getEstado())) {
            throw new ReglaNegocioException("El control ya esta cerrado", HttpStatus.CONFLICT);
        }
    }

    private ControlResponse aRespuesta(ControlInventarioEntity c) {
        ControlResponse r = new ControlResponse();
        r.setIdControl(c.getIdControl());
        r.setIdUsuario(c.getIdUsuario());
        r.setTipo(c.getTipo());
        r.setFecha(c.getFecha());
        r.setEstado(c.getEstado());
        r.setObservaciones(c.getObservaciones());
        for (DetalleControlInventarioEntity d : detalleRepository.findByIdControl(c.getIdControl())) {
            DetalleControlResponse dr = new DetalleControlResponse();
            dr.setIdDetalleControl(d.getIdDetalleControl());
            dr.setIdInsumo(d.getInsumo().getIdInsumo());
            dr.setNombreInsumo(d.getInsumo().getNombre());
            dr.setStockSistema(d.getStockSistema());
            dr.setStockFisico(d.getStockFisico());
            dr.setDiferencia(d.getDiferencia());
            r.getDetalles().add(dr);
        }
        return r;
    }
}