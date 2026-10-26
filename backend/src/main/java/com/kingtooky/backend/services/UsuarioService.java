package com.kingtooky.backend.services;

import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.application.dto.UsuarioRequest;
import com.kingtooky.backend.application.dto.UsuarioResponse;
import com.kingtooky.backend.application.dto.UsuarioUpdateRequest;
import com.kingtooky.backend.infrastructure.entity.RolEntity;
import com.kingtooky.backend.infrastructure.entity.UsuarioEntity;
import com.kingtooky.backend.presentation.exception.RecursoNoEncontradoException;
import com.kingtooky.backend.presentation.exception.ReglaNegocioException;
import com.kingtooky.backend.repository.RolJpaRepository;
import com.kingtooky.backend.repository.UsuarioJpaRepository;
import com.kingtooky.backend.security.application.SecurityUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioJpaRepository usuarioRepository;
    private final RolJpaRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioJpaRepository usuarioRepository, RolJpaRepository rolRepository,
                          PasswordEncoder passwordEncoder, AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listar(String q, Integer estado, Pageable pageable) {
        return PageUtil.toResponse(
                usuarioRepository.buscar(PageUtil.texto(q), estado != null ? estado : -1, pageable),
                this::aRespuesta);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Integer id) {
        return aRespuesta(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        String nombre = request.getNombreUsuario().trim();
        if (usuarioRepository.existsByNombreUsuarioIgnoreCase(nombre)) {
            throw new ReglaNegocioException("El nombre de usuario ya existe");
        }
        UsuarioEntity u = new UsuarioEntity();
        u.setNombreUsuario(nombre);
        u.setNombreCompleto(request.getNombreCompleto().trim());
        u.setContrasena(passwordEncoder.encode(request.getContrasena()));
        u.setRol(buscarRol(request.getIdRol()));
        u.setEstado(1);
        UsuarioEntity guardado = usuarioRepository.save(u);
        auditoriaService.registrar("CREAR", "usuario", guardado.getIdUsuario(), "Usuario creado: " + nombre);
        return aRespuesta(guardado);
    }

    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioUpdateRequest request) {
        UsuarioEntity u = buscar(id);
        u.setNombreCompleto(request.getNombreCompleto().trim());
        u.setRol(buscarRol(request.getIdRol()));
        if (request.getContrasena() != null && !request.getContrasena().isBlank()) {
            u.setContrasena(passwordEncoder.encode(request.getContrasena()));
        }
        UsuarioEntity guardado = usuarioRepository.save(u);
        auditoriaService.registrar("EDITAR", "usuario", id, "Usuario modificado: " + u.getNombreUsuario());
        return aRespuesta(guardado);
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Integer id, boolean activo) {
        UsuarioEntity u = buscar(id);
        if (!activo && id.equals(SecurityUtil.idUsuarioActual())) {
            throw new ReglaNegocioException("No puede desactivar su propio usuario");
        }
        u.setEstado(activo ? 1 : 0);
        UsuarioEntity guardado = usuarioRepository.save(u);
        auditoriaService.registrar(activo ? "ACTIVAR" : "DESACTIVAR", "usuario", id, u.getNombreUsuario());
        return aRespuesta(guardado);
    }

    @Transactional
    public void eliminar(Integer id) {
        UsuarioEntity u = buscar(id);
        if (id.equals(SecurityUtil.idUsuarioActual())) {
            throw new ReglaNegocioException("No puede eliminar su propio usuario");
        }
        usuarioRepository.delete(u);
        usuarioRepository.flush();
        auditoriaService.registrar("ELIMINAR", "usuario", id, "Usuario eliminado: " + u.getNombreUsuario());
    }

    private UsuarioEntity buscar(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
    }

    private RolEntity buscarRol(Integer idRol) {
        return rolRepository.findById(idRol)
                .orElseThrow(() -> new RecursoNoEncontradoException("Rol no encontrado: " + idRol));
    }

    private UsuarioResponse aRespuesta(UsuarioEntity u) {
        UsuarioResponse r = new UsuarioResponse();
        r.setIdUsuario(u.getIdUsuario());
        r.setNombreUsuario(u.getNombreUsuario());
        r.setNombreCompleto(u.getNombreCompleto());
        r.setIdRol(u.getRol().getIdRol());
        r.setRol(u.getRol().getNombre());
        r.setEstado(u.getEstado());
        r.setFechaCreacion(u.getFechaCreacion());
        return r;
    }
}