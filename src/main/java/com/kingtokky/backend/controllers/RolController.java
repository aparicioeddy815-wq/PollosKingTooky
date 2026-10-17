package com.kingtokky.backend.controllers;

import com.kingtokky.backend.dto.RolDto;
import com.kingtokky.backend.services.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @GetMapping
    public List<RolDto> listar() {
        return rolService.listarRoles();
    }

    @PostMapping
    public RolDto guardar(@RequestBody RolDto rolDto) {
        return rolService.guardarRol(rolDto);
    }
}