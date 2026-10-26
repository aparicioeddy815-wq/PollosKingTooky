package com.kingtooky.backend.services;

import com.kingtooky.backend.application.dto.AlertaResponse;
import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.domain.ReglaStock;
import com.kingtooky.backend.infrastructure.entity.AlertaStockEntity;
import com.kingtooky.backend.infrastructure.entity.InsumoEntity;
import com.kingtooky.backend.presentation.exception.RecursoNoEncontradoException;
import com.kingtooky.backend.repository.AlertaStockJpaRepository;
import com.kingtooky.backend.repository.InsumoJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlertaStockService {

    public static final String ACTIVA = "ACTIVA";
    public static final String ATENDIDA = "ATENDIDA";

    private static final Logger log = LoggerFactory.getLogger(AlertaStockService.class);

    private final AlertaStockJpaRepository alertaRepository;
    private final InsumoJpaRepository insumoRepository;

    public AlertaStockService(AlertaStockJpaRepository alertaRepository, InsumoJpaRepository insumoRepository) {
        this.alertaRepository = alertaRepository;
        this.insumoRepository = insumoRepository;
    }

    @Transactional
    public void evaluar(InsumoEntity insumo) {
        boolean activo = insumo.getEstado() != null && insumo.getEstado().equalsIgnoreCase("activo");
        boolean requiere = activo && ReglaStock.requiereAlerta(insumo.getStockActual(), insumo.getStockMinimo());

        if (requiere) {
            if (alertaRepository.findFirstByInsumo_IdInsumoAndEstado(insumo.getIdInsumo(), ACTIVA).isEmpty()) {
                AlertaStockEntity a = new AlertaStockEntity();
                a.setInsumo(insumo);
                a.setStockAlGenerar(insumo.getStockActual());
                a.setStockMinimo(insumo.getStockMinimo());
                a.setEstado(ACTIVA);
                alertaRepository.save(a);
            }
        } else {
            List<AlertaStockEntity> pendientes =
                    alertaRepository.findByInsumo_IdInsumoAndEstado(insumo.getIdInsumo(), ACTIVA);
            for (AlertaStockEntity a : pendientes) {
                a.setEstado(ATENDIDA);
                alertaRepository.save(a);
            }
        }
    }

    @Scheduled(cron = "${app.alertas.cron:0 */15 * * * *}")
    @Transactional
    public void barridoProgramado() {
        List<InsumoEntity> bajos = insumoRepository.findBajoStock();
        for (InsumoEntity i : bajos) {
            evaluar(i);
        }
        List<AlertaStockEntity> activas = alertaRepository.findAll().stream()
                .filter(a -> ACTIVA.equals(a.getEstado()) && a.getInsumo() != null).toList();
        for (AlertaStockEntity a : activas) {
            evaluar(a.getInsumo());
        }
        log.debug("Barrido de alertas: {} insumos bajo minimo", bajos.size());
    }

    @Transactional(readOnly = true)
    public PageResponse<AlertaResponse> listar(String estado, Pageable pageable) {
        return PageUtil.toResponse(alertaRepository.buscar(PageUtil.texto(estado).toUpperCase(), pageable), this::aRespuesta);
    }

    @Transactional(readOnly = true)
    public long contarActivas() {
        return alertaRepository.countByEstado(ACTIVA);
    }

    @Transactional
    public AlertaResponse atender(Integer idAlerta) {
        AlertaStockEntity a = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alerta no encontrada: " + idAlerta));
        a.setEstado(ATENDIDA);
        return aRespuesta(alertaRepository.save(a));
    }

    private AlertaResponse aRespuesta(AlertaStockEntity a) {
        AlertaResponse r = new AlertaResponse();
        r.setIdAlerta(a.getIdAlerta());
        if (a.getInsumo() != null) {
            r.setIdInsumo(a.getInsumo().getIdInsumo());
            r.setNombreInsumo(a.getInsumo().getNombre());
            r.setUnidadMedida(a.getInsumo().getUnidadMedida());
        }
        r.setIdGuarnicion(a.getIdGuarnicion());
        r.setStockAlGenerar(a.getStockAlGenerar());
        r.setStockMinimo(a.getStockMinimo());
        r.setFechaHora(a.getFechaHora());
        r.setEstado(a.getEstado());
        return r;
    }
}