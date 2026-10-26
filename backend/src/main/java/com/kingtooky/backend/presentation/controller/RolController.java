package com.kingtooky.backend.presentation.controller;

import com.kingtooky.backend.application.dto.AsignarPermisosRequest;
import com.kingtooky.backend.application.dto.PermisoResponse;
import com.kingtooky.backend.application.dto.RolResponse;
import com.kingtooky.backend.services.RolService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RolController {

    private final RolService rolService;

    public RolController(RolService rolService) {
        this.rolService = rolService;
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAnyAuthority('USUARIO_LEER', 'ROL_GESTIONAR')")
    public List<RolResponse> roles() {
        return rolService.listarRoles();
    }

    @GetMapping("/permisos")
    @PreAuthorize("hasAuthority('ROL_GESTIONAR')")
    public List<PermisoResponse> permisos() {
        return rolService.listarPermisos();
    }

    @PutMapping("/roles/{id}/permisos")
    @PreAuthorize("hasAuthority('ROL_GESTIONAR')")
    public RolResponse asignarPermisos(@PathVariable Integer id, @Valid @RequestBody AsignarPermisosRequest request) {
        return rolService.asignarPermisos(id, request.getIdPermisos());
    }
}