package com.kingtooky.backend.services;

import com.kingtooky.backend.application.dto.AuditoriaResponse;
import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.infrastructure.entity.AuditoriaEntity;
import com.kingtooky.backend.repository.AuditoriaJpaRepository;
import com.kingtooky.backend.security.application.SecurityUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private final AuditoriaJpaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaJpaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional
    public void registrar(String accion, String entidad, Object idEntidad, String detalle) {
        registrar(SecurityUtil.idUsuarioActual(), accion, entidad, idEntidad, detalle);
    }

    @Transactional
    public void registrar(Integer idUsuario, String accion, String entidad, Object idEntidad, String detalle) {
        AuditoriaEntity a = new AuditoriaEntity();
        a.setIdUsuario(idUsuario);
        a.setAccion(accion);
        a.setEntidad(entidad);
        a.setIdEntidad(idEntidad != null ? String.valueOf(idEntidad) : null);
        a.setDetalle(detalle);
        auditoriaRepository.save(a);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditoriaResponse> consultar(Integer idUsuario, String accion, String entidad,
                                                     LocalDate desde, LocalDate hasta, Pageable pageable) {
        LocalDateTime d = (desde != null ? desde : LocalDate.of(2000, 1, 1)).atStartOfDay();
        LocalDateTime h = (hasta != null ? hasta : LocalDate.of(2999, 12, 31)).plusDays(1).atStartOfDay();
        return PageUtil.toResponse(
                auditoriaRepository.buscar(idUsuario != null ? idUsuario : 0, PageUtil.texto(accion),
                        PageUtil.texto(entidad), d, h, pageable),
                this::aRespuesta);
    }

    private AuditoriaResponse aRespuesta(AuditoriaEntity a) {
        AuditoriaResponse r = new AuditoriaResponse();
        r.setIdAuditoria(a.getIdAuditoria());
        r.setIdUsuario(a.getIdUsuario());
        r.setAccion(a.getAccion());
        r.setEntidad(a.getEntidad());
        r.setIdEntidad(a.getIdEntidad());
        r.setDetalle(a.getDetalle());
        r.setFechaHora(a.getFechaHora());
        return r;
    }
}