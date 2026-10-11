package com.kingtooky.backend.presentation.controller;

import com.kingtooky.backend.application.dto.PageResponse;
import com.kingtooky.backend.application.dto.UsuarioRequest;
import com.kingtooky.backend.application.dto.UsuarioResponse;
import com.kingtooky.backend.application.dto.UsuarioUpdateRequest;
import com.kingtooky.backend.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public PageResponse<UsuarioResponse> listar(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) Integer estado,
                                                @PageableDefault(size = 20, sort = "nombreUsuario") Pageable pageable) {
        return usuarioService.listar(q, estado, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public UsuarioResponse obtener(@PathVariable Integer id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USUARIO_ESCRIBIR')")
    public UsuarioResponse crear(@Valid @RequestBody UsuarioRequest request) {
        return usuarioService.crear(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_ESCRIBIR')")
    public UsuarioResponse actualizar(@PathVariable Integer id, @Valid @RequestBody UsuarioUpdateRequest request) {
        return usuarioService.actualizar(id, request);
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasAuthority('USUARIO_ESCRIBIR')")
    public UsuarioResponse activar(@PathVariable Integer id) {
        return usuarioService.cambiarEstado(id, true);
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasAuthority('USUARIO_ESCRIBIR')")
    public UsuarioResponse desactivar(@PathVariable Integer id) {
        return usuarioService.cambiarEstado(id, false);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('USUARIO_ESCRIBIR')")
    public void eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
    }
}