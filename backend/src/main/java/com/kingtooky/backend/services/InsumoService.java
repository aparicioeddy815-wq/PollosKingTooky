package com.kingtooky.backend.services;

import com.kingtooky.backend.application.dto.InsumoRequest;
import com.kingtooky.backend.application.dto.InsumoResponse;
import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.domain.ReglaStock;
import com.kingtooky.backend.infrastructure.entity.InsumoEntity;
import com.kingtooky.backend.presentation.exception.RecursoNoEncontradoException;
import com.kingtooky.backend.presentation.exception.ReglaNegocioException;
import com.kingtooky.backend.repository.InsumoJpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class InsumoService {

    private final InsumoJpaRepository insumoRepository;
    private final MovimientoInventarioService movimientoService;
    private final AlertaStockService alertaService;
    private final AuditoriaService auditoriaService;

    public InsumoService(InsumoJpaRepository insumoRepository, MovimientoInventarioService movimientoService,
                         AlertaStockService alertaService, AuditoriaService auditoriaService) {
        this.insumoRepository = insumoRepository;
        this.movimientoService = movimientoService;
        this.alertaService = alertaService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public PageResponse<InsumoResponse> listar(String q, String tipo, String estado, boolean bajoStock, Pageable pageable) {
        return PageUtil.toResponse(
                insumoRepository.buscar(PageUtil.texto(q), PageUtil.texto(tipo).toUpperCase(), PageUtil.texto(estado),
                        bajoStock, pageable),
                this::aRespuesta);
    }

    @Transactional(readOnly = true)
    public InsumoResponse obtener(Integer id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public InsumoResponse crear(InsumoRequest request) {
        String nombre = request.getNombre().trim();
        if (insumoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe un insumo con ese nombre");
        }
        InsumoEntity i = new InsumoEntity();
        i.setNombre(nombre);
        i.setUnidadMedida(request.getUnidadMedida().trim());
        i.setStockMinimo(request.getStockMinimo().setScale(2, RoundingMode.HALF_UP));
        i.setStockActual(BigDecimal.ZERO.setScale(2));
        i.setTipo(request.getTipo() != null ? request.getTipo() : "INSUMO");
        i.setEstado("activo");
        InsumoEntity guardado = insumoRepository.save(i);

        BigDecimal inicial = request.getStockActual();
        if (inicial != null && inicial.compareTo(BigDecimal.ZERO) > 0) {
            movimientoService.aplicar(guardado.getIdInsumo(), MovimientoInventarioService.ENTRADA, inicial,
                    null, null, "Stock inicial");
            guardado = buscar(guardado.getIdInsumo());
        }
        alertaService.evaluar(guardado);
        auditoriaService.registrar("CREAR", "insumo", guardado.getIdInsumo(), "Insumo creado: " + nombre);
        return aRespuesta(guardado);
    }

    @Transactional
    public InsumoResponse actualizar(Integer id, InsumoRequest request) {
        InsumoEntity i = buscar(id);
        String nombre = request.getNombre().trim();
        if (insumoRepository.existsByNombreIgnoreCaseAndIdInsumoNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe un insumo con ese nombre");
        }
        i.setNombre(nombre);
        i.setUnidadMedida(request.getUnidadMedida().trim());
        i.setStockMinimo(request.getStockMinimo().setScale(2, RoundingMode.HALF_UP));
        if (request.getTipo() != null) {
            i.setTipo(request.getTipo());
        }
        InsumoEntity guardado = insumoRepository.save(i);
        alertaService.evaluar(guardado);
        auditoriaService.registrar("EDITAR", "insumo", id, "Insumo modificado: " + nombre);
        return aRespuesta(guardado);
    }

    @Transactional
    public InsumoResponse cambiarEstado(Integer id, String estado) {
        String e = estado.trim().toLowerCase();
        if (!e.equals("activo") && !e.equals("inactivo")) {
            throw new ReglaNegocioException("El estado debe ser 'activo' o 'inactivo'");
        }
        InsumoEntity i = buscar(id);
        i.setEstado(e);
        InsumoEntity guardado = insumoRepository.save(i);
        alertaService.evaluar(guardado);
        auditoriaService.registrar(e.equals("activo") ? "ACTIVAR" : "DESACTIVAR", "insumo", id, i.getNombre());
        return aRespuesta(guardado);
    }

    private InsumoEntity buscar(Integer id) {
        return insumoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Insumo no encontrado: " + id));
    }

    private InsumoResponse aRespuesta(InsumoEntity i) {
        InsumoResponse r = new InsumoResponse();
        r.setIdInsumo(i.getIdInsumo());
        r.setNombre(i.getNombre());
        r.setUnidadMedida(i.getUnidadMedida());
        r.setStockActual(i.getStockActual());
        r.setStockMinimo(i.getStockMinimo());
        r.setEstado(i.getEstado());
        r.setTipo(i.getTipo());
        r.setBajoStock(ReglaStock.requiereAlerta(i.getStockActual(), i.getStockMinimo()));
        return r;
    }
}