package com.kingtokky.backend.services;

import com.kingtokky.backend.dto.RolDto;
import com.kingtokky.backend.entities.Rol;
import com.kingtokky.backend.repositories.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    // Lista todos los roles y los lleva a DTO
    public List<RolDto> listarRoles() {
        return rolRepository.findAll().stream()
                .map(RolDto::new)
                .collect(Collectors.toList());
    }

    public RolDto guardarRol(RolDto rolDto) {
        Rol rol = new Rol();
        rol.setNombre(rolDto.getNombre());
        rol.setDescripcion(rolDto.getDescripcion());

        Rol rolGuardado = rolRepository.save(rol);
        return new RolDto(rolGuardado);
    }
}