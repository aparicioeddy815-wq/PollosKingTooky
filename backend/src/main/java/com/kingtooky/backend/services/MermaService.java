package com.kingtooky.backend.services;

import com.kingtooky.backend.application.dto.MermaRequest;
import com.kingtooky.backend.application.dto.MermaResponse;
import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.infrastructure.entity.MermaEntity;
import com.kingtooky.backend.infrastructure.entity.MovimientoInventarioEntity;
import com.kingtooky.backend.repository.MermaJpaRepository;
import com.kingtooky.backend.security.application.SecurityUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class MermaService {

    private final MermaJpaRepository mermaRepository;
    private final MovimientoInventarioService movimientoService;
    private final AuditoriaService auditoriaService;

    public MermaService(MermaJpaRepository mermaRepository, MovimientoInventarioService movimientoService,
                        AuditoriaService auditoriaService) {
        this.mermaRepository = mermaRepository;
        this.movimientoService = movimientoService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public MermaResponse registrar(MermaRequest request) {
        MovimientoInventarioEntity mov = movimientoService.aplicar(request.getIdInsumo(),
                MovimientoInventarioService.MERMA, request.getCantidad(), null, null, request.getMotivo().trim());

        MermaEntity m = new MermaEntity();
        m.setInsumo(mov.getInsumo());
        m.setCantidad(mov.getCantidad());
        m.setMotivo(request.getMotivo().trim());
        m.setIdUsuario(SecurityUtil.idUsuarioActual());
        MermaEntity guardada = mermaRepository.save(m);
        auditoriaService.registrar("MERMA", "insumo", request.getIdInsumo(),
                "Merma de " + mov.getCantidad() + " " + mov.getInsumo().getUnidadMedida() + " de "
                        + mov.getInsumo().getNombre() + ": " + guardada.getMotivo());
        return aRespuesta(guardada);
    }

    @Transactional(readOnly = true)
    public PageResponse<MermaResponse> listar(Integer idInsumo, LocalDate desde, LocalDate hasta, Pageable pageable) {
        LocalDateTime d = (desde != null ? desde : LocalDate.of(2000, 1, 1)).atStartOfDay();
        LocalDateTime h = (hasta != null ? hasta : LocalDate.of(2999, 12, 31)).plusDays(1).atStartOfDay();
        return PageUtil.toResponse(mermaRepository.buscar(idInsumo != null ? idInsumo : 0, d, h, pageable), this::aRespuesta);
    }

    private MermaResponse aRespuesta(MermaEntity m) {
        MermaResponse r = new MermaResponse();
        r.setIdMerma(m.getIdMerma());
        r.setIdInsumo(m.getInsumo().getIdInsumo());
        r.setNombreInsumo(m.getInsumo().getNombre());
        r.setIdUsuario(m.getIdUsuario());
        r.setCantidad(m.getCantidad());
        r.setMotivo(m.getMotivo());
        r.setFechaHora(m.getFechaHora());
        return r;
    }
}