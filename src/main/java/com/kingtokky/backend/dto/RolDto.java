package com.kingtokky.backend.dto;

import com.kingtokky.backend.entities.Rol;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolDto {

    private Long id;
    private String nombre;
    private String descripcion;

    public RolDto(Rol rol) {
        this.id = rol.getId();
        this.nombre = rol.getNombre();
        this.descripcion = rol.getDescripcion();
    }
}