package com.kingtokky.backend.services;

import com.kingtokky.backend.dto.CategoriaDto;
import com.kingtokky.backend.entities.Categoria;
import com.kingtokky.backend.repositories.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<CategoriaDto> listarCategorias() {
        return categoriaRepository.findAll().stream()
                .map(CategoriaDto::new)
                .collect(Collectors.toList());
    }

    public CategoriaDto guardarCategoria(CategoriaDto categoriaDto) {
        Categoria categoria = new Categoria();
        categoria.setNombreCategoria(categoriaDto.getNombreCategoria());

        Categoria guardada = categoriaRepository.save(categoria);
        return new CategoriaDto(guardada);
    }
}