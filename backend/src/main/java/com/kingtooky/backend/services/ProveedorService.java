package com.kingtooky.backend.services;

import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.application.dto.ProveedorRequest;
import com.kingtooky.backend.application.dto.ProveedorResponse;
import com.kingtooky.backend.infrastructure.entity.ProveedorEntity;
import com.kingtooky.backend.presentation.exception.RecursoNoEncontradoException;
import com.kingtooky.backend.presentation.exception.ReglaNegocioException;
import com.kingtooky.backend.repository.ProveedorJpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProveedorService {

    private final ProveedorJpaRepository proveedorRepository;
    private final AuditoriaService auditoriaService;

    public ProveedorService(ProveedorJpaRepository proveedorRepository, AuditoriaService auditoriaService) {
        this.proveedorRepository = proveedorRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProveedorResponse> listar(String q, Pageable pageable) {
        return PageUtil.toResponse(proveedorRepository.buscar(PageUtil.texto(q), pageable), this::aRespuesta);
    }

    @Transactional(readOnly = true)
    public ProveedorResponse obtener(Integer id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public ProveedorResponse crear(ProveedorRequest request) {
        ProveedorEntity p = new ProveedorEntity();
        aplicar(p, request);
        p.setEstado("activo");
        ProveedorEntity guardado = proveedorRepository.save(p);
        auditoriaService.registrar("CREAR", "proveedor", guardado.getIdProveedor(), "Proveedor creado: " + guardado.getNombre());
        return aRespuesta(guardado);
    }

    @Transactional
    public ProveedorResponse actualizar(Integer id, ProveedorRequest request) {
        ProveedorEntity p = buscar(id);
        aplicar(p, request);
        ProveedorEntity guardado = proveedorRepository.save(p);
        auditoriaService.registrar("EDITAR", "proveedor", id, "Proveedor modificado: " + guardado.getNombre());
        return aRespuesta(guardado);
    }

    @Transactional
    public ProveedorResponse cambiarEstado(Integer id, String estado) {
        String e = estado.trim().toLowerCase();
        if (!e.equals("activo") && !e.equals("inactivo")) {
            throw new ReglaNegocioException("El estado debe ser 'activo' o 'inactivo'");
        }
        ProveedorEntity p = buscar(id);
        p.setEstado(e);
        auditoriaService.registrar(e.equals("activo") ? "ACTIVAR" : "DESACTIVAR", "proveedor", id, p.getNombre());
        return aRespuesta(proveedorRepository.save(p));
    }

    private ProveedorEntity buscar(Integer id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado: " + id));
    }

    private void aplicar(ProveedorEntity p, ProveedorRequest request) {
        p.setNombre(request.getNombre().trim());
        p.setTelefono(request.getTelefono());
        p.setCorreo(request.getCorreo());
        p.setDireccion(request.getDireccion());
    }

    private ProveedorResponse aRespuesta(ProveedorEntity p) {
        ProveedorResponse r = new ProveedorResponse();
        r.setIdProveedor(p.getIdProveedor());
        r.setNombre(p.getNombre());
        r.setTelefono(p.getTelefono());
        r.setCorreo(p.getCorreo());
        r.setDireccion(p.getDireccion());
        r.setEstado(p.getEstado());
        return r;
    }
}