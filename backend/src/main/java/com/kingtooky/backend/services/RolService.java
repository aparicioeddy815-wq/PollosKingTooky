package com.kingtooky.backend.services;

import com.kingtooky.backend.application.dto.PermisoResponse;
import com.kingtooky.backend.application.dto.RolResponse;
import com.kingtooky.backend.infrastructure.entity.PermisoEntity;
import com.kingtooky.backend.infrastructure.entity.RolEntity;
import com.kingtooky.backend.presentation.exception.RecursoNoEncontradoException;
import com.kingtooky.backend.repository.PermisoJpaRepository;
import com.kingtooky.backend.repository.RolJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RolService {

    private final RolJpaRepository rolRepository;
    private final PermisoJpaRepository permisoRepository;
    private final AuditoriaService auditoriaService;

    public RolService(RolJpaRepository rolRepository, PermisoJpaRepository permisoRepository,
                      AuditoriaService auditoriaService) {
        this.rolRepository = rolRepository;
        this.permisoRepository = permisoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<RolResponse> listarRoles() {
        return rolRepository.findAll().stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public List<PermisoResponse> listarPermisos() {
        return permisoRepository.findAll().stream().map(this::permisoARespuesta).toList();
    }

    @Transactional
    public RolResponse asignarPermisos(Integer idRol, List<Integer> idPermisos) {
        RolEntity rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + idRol));
        List<PermisoEntity> permisos = permisoRepository.findAllById(idPermisos);
        if (permisos.size() != new HashSet<>(idPermisos).size()) {
            throw new RecursoNoEncontradoException("Alguno de los permisos enviados no existe");
        }
        Set<PermisoEntity> nuevos = new HashSet<>(permisos);
        rol.setPermisos(nuevos);
        RolEntity guardado = rolRepository.save(rol);
        auditoriaService.registrar("ASIGNAR_PERMISOS", "rol", idRol,
                "Permisos del rol " + rol.getNombre() + " actualizados (" + nuevos.size() + ")");
        return aRespuesta(guardado);
    }

    private RolResponse aRespuesta(RolEntity rol) {
        RolResponse r = new RolResponse();
        r.setIdRol(rol.getIdRol());
        r.setNombre(rol.getNombre());
        r.setPermisos(rol.getPermisos().stream().map(this::permisoARespuesta)
                .sorted((a, b) -> a.getCodigo().compareTo(b.getCodigo())).toList());
        return r;
    }

    private PermisoResponse permisoARespuesta(PermisoEntity p) {
        PermisoResponse r = new PermisoResponse();
        r.setIdPermiso(p.getIdPermiso());
        r.setCodigo(p.getCodigo());
        r.setDescripcion(p.getDescripcion());
        return r;
    }
}