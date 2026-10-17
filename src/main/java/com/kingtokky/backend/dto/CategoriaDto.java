package com.kingtokky.backend.dto;

import com.kingtokky.backend.entities.Categoria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDto {

    private Integer idCategoria;
    private String nombreCategoria;

    public CategoriaDto(Categoria categoria) {
        this.idCategoria = categoria.getIdCategoria();
        this.nombreCategoria = categoria.getNombreCategoria();
    }
}