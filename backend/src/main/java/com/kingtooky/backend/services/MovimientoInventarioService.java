package com.kingtooky.backend.services;

import com.kingtooky.backend.application.dto.MovimientoRequest;
import com.kingtooky.backend.application.dto.MovimientoResponse;
import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.domain.ReglaStock;
import com.kingtooky.backend.infrastructure.entity.InsumoEntity;
import com.kingtooky.backend.infrastructure.entity.MovimientoInventarioEntity;
import com.kingtooky.backend.presentation.exception.RecursoNoEncontradoException;
import com.kingtooky.backend.presentation.exception.ReglaNegocioException;
import com.kingtooky.backend.repository.InsumoJpaRepository;
import com.kingtooky.backend.repository.MovimientoInventarioJpaRepository;
import com.kingtooky.backend.repository.ProveedorJpaRepository;
import com.kingtooky.backend.security.application.SecurityUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class MovimientoInventarioService {

    public static final String ENTRADA = "ENTRADA";
    public static final String SALIDA = "SALIDA";
    public static final String MERMA = "MERMA";
    public static final String AJUSTE = "AJUSTE";

    private final InsumoJpaRepository insumoRepository;
    private final MovimientoInventarioJpaRepository movimientoRepository;
    private final ProveedorJpaRepository proveedorRepository;
    private final AlertaStockService alertaService;
    private final AuditoriaService auditoriaService;
    private final JdbcTemplate jdbcTemplate;

    public MovimientoInventarioService(InsumoJpaRepository insumoRepository,
                                       MovimientoInventarioJpaRepository movimientoRepository,
                                       ProveedorJpaRepository proveedorRepository,
                                       AlertaStockService alertaService, AuditoriaService auditoriaService,
                                       JdbcTemplate jdbcTemplate) {
        this.insumoRepository = insumoRepository;
        this.movimientoRepository = movimientoRepository;
        this.proveedorRepository = proveedorRepository;
        this.alertaService = alertaService;
        this.auditoriaService = auditoriaService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public MovimientoResponse registrarEntrada(MovimientoRequest request) {
        if (request.getIdProveedor() != null && !proveedorRepository.existsById(request.getIdProveedor())) {
            throw new RecursoNoEncontradoException("Proveedor no encontrado: " + request.getIdProveedor());
        }
        validarPedido(request.getIdPedido());
        MovimientoInventarioEntity m = aplicar(request.getIdInsumo(), ENTRADA, request.getCantidad(),
                request.getIdProveedor(), request.getIdPedido(), motivoOpcional(request.getMotivo(), "Ingreso de inventario"));
        auditoriaService.registrar("ENTRADA", "insumo", request.getIdInsumo(),
                "Entrada de " + m.getCantidad() + " " + m.getInsumo().getUnidadMedida() + " de " + m.getInsumo().getNombre());
        return aRespuesta(m);
    }

    @Transactional
    public MovimientoResponse registrarSalida(MovimientoRequest request) {
        validarPedido(request.getIdPedido());
        MovimientoInventarioEntity m = aplicar(request.getIdInsumo(), SALIDA, request.getCantidad(),
                null, request.getIdPedido(), motivoOpcional(request.getMotivo(), "Salida de inventario"));
        auditoriaService.registrar("SALIDA", "insumo", request.getIdInsumo(),
                "Salida de " + m.getCantidad() + " " + m.getInsumo().getUnidadMedida() + " de " + m.getInsumo().getNombre());
        return aRespuesta(m);
    }

    @Transactional(readOnly = true)
    public PageResponse<MovimientoResponse> listar(Integer idInsumo, String tipo, LocalDate desde, LocalDate hasta,
                                                   Pageable pageable) {
        LocalDateTime d = (desde != null ? desde : LocalDate.of(2000, 1, 1)).atStartOfDay();
        LocalDateTime h = (hasta != null ? hasta : LocalDate.of(2999, 12, 31)).plusDays(1).atStartOfDay();
        return PageUtil.toResponse(
                movimientoRepository.buscar(idInsumo != null ? idInsumo : 0, PageUtil.texto(tipo).toUpperCase(), d, h, pageable),
                this::aRespuesta);
    }

    @Transactional(readOnly = true)
    public PageResponse<MovimientoResponse> kardex(Integer idInsumo, LocalDate desde, LocalDate hasta, Pageable pageable) {
        if (!insumoRepository.existsById(idInsumo)) {
            throw new RecursoNoEncontradoException("Insumo no encontrado: " + idInsumo);
        }
        return listar(idInsumo, null, desde, hasta, pageable);
    }

    @Transactional
    public MovimientoInventarioEntity aplicar(Integer idInsumo, String tipo, BigDecimal cantidad,
                                              Integer idProveedor, Integer idPedido, String motivo) {
        InsumoEntity insumo = insumoRepository.findByIdForUpdate(idInsumo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Insumo no encontrado: " + idInsumo));
        if (insumo.getEstado() == null || !insumo.getEstado().equalsIgnoreCase("activo")) {
            throw new ReglaNegocioException("El insumo '" + insumo.getNombre() + "' esta inactivo", HttpStatus.CONFLICT);
        }
        BigDecimal cant = cantidad.setScale(2, RoundingMode.HALF_UP);
        if (cant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor a cero (maximo 2 decimales)");
        }

        BigDecimal nuevoStock;
        if (ENTRADA.equals(tipo)) {
            nuevoStock = insumo.getStockActual().add(cant);
        } else if (SALIDA.equals(tipo) || MERMA.equals(tipo)) {
            if (!ReglaStock.stockSuficiente(insumo.getStockActual(), cant)) {
                throw new ReglaNegocioException("Stock insuficiente de '" + insumo.getNombre() + "': disponible "
                        + insumo.getStockActual() + " " + insumo.getUnidadMedida() + ", requerido " + cant, HttpStatus.CONFLICT);
            }
            nuevoStock = insumo.getStockActual().subtract(cant);
        } else {
            throw new ReglaNegocioException("Tipo de movimiento no soportado: " + tipo);
        }

        insumo.setStockActual(nuevoStock);
        insumoRepository.save(insumo);
        MovimientoInventarioEntity m = guardarMovimiento(insumo, tipo, cant, nuevoStock, idProveedor, idPedido, motivo);
        alertaService.evaluar(insumo);
        return m;
    }

    @Transactional
    public MovimientoInventarioEntity aplicarAjuste(Integer idInsumo, BigDecimal stockFisico, String motivo) {
        InsumoEntity insumo = insumoRepository.findByIdForUpdate(idInsumo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Insumo no encontrado: " + idInsumo));
        BigDecimal fisico = stockFisico.setScale(2, RoundingMode.HALF_UP);
        BigDecimal diferencia = fisico.subtract(insumo.getStockActual());
        if (diferencia.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        insumo.setStockActual(fisico);
        insumoRepository.save(insumo);
        MovimientoInventarioEntity m = guardarMovimiento(insumo, AJUSTE, diferencia.abs(), fisico, null, null, motivo);
        alertaService.evaluar(insumo);
        return m;
    }

    @Transactional(readOnly = true)
    public void validarPedido(Integer idPedido) {
        if (idPedido == null) {
            return;
        }
        Integer c = jdbcTemplate.queryForObject("select count(*) from pedido where id_pedido = ?", Integer.class, idPedido);
        if (c == null || c == 0) {
            throw new RecursoNoEncontradoException("Pedido no encontrado: " + idPedido);
        }
    }

    private MovimientoInventarioEntity guardarMovimiento(InsumoEntity insumo, String tipo, BigDecimal cantidad,
                                                         BigDecimal stockResultante, Integer idProveedor,
                                                         Integer idPedido, String motivo) {
        MovimientoInventarioEntity m = new MovimientoInventarioEntity();
        m.setInsumo(insumo);
        m.setTipo(tipo);
        m.setCantidad(cantidad);
        m.setStockResultante(stockResultante);
        m.setIdUsuario(SecurityUtil.idUsuarioActual());
        m.setIdProveedor(idProveedor);
        m.setIdPedido(idPedido);
        m.setMotivo(motivo);
        return movimientoRepository.save(m);
    }

    private String motivoOpcional(String motivo, String porDefecto) {
        return (motivo == null || motivo.isBlank()) ? porDefecto : motivo.trim();
    }

    public MovimientoResponse aRespuesta(MovimientoInventarioEntity m) {
        MovimientoResponse r = new MovimientoResponse();
        r.setIdMovimiento(m.getIdMovimiento());
        r.setIdInsumo(m.getInsumo().getIdInsumo());
        r.setNombreInsumo(m.getInsumo().getNombre());
        r.setUnidadMedida(m.getInsumo().getUnidadMedida());
        r.setIdUsuario(m.getIdUsuario());
        r.setIdProveedor(m.getIdProveedor());
        r.setIdPedido(m.getIdPedido());
        r.setTipo(m.getTipo());
        r.setCantidad(m.getCantidad());
        r.setStockResultante(m.getStockResultante());
        r.setMotivo(m.getMotivo());
        r.setFechaHora(m.getFechaHora());
        return r;
    }
}