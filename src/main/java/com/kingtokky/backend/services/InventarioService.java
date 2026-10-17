package com.kingtokky.backend.services;

import com.kingtokky.backend.dto.InventarioDto;
import com.kingtokky.backend.entities.Inventario;
import com.kingtokky.backend.repositories.InventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventarioService {

    private final InventarioRepository inventarioRepository;

    public List<InventarioDto> listarInsumos() {
        return inventarioRepository.findAll().stream()
                .map(InventarioDto::new)
                .collect(Collectors.toList());
    }

    public InventarioDto guardarInsumo(InventarioDto inventarioDto) {
        Inventario inventario = new Inventario();
        inventario.setNombreInsumo(inventarioDto.getNombreInsumo());
        inventario.setCategoriaInsumo(inventarioDto.getCategoriaInsumo());
        inventario.setStockActual(inventarioDto.getStockActual());
        inventario.setUmbralMinimo(inventarioDto.getUmbralMinimo());
        inventario.setConsumoDia(inventarioDto.getConsumoDia());
        inventario.setUnidadMedida(inventarioDto.getUnidadMedida());
        inventario.setUltimaActualizacion(LocalDateTime.now());

        Inventario guardado = inventarioRepository.save(inventario);
        return new InventarioDto(guardado);
    }
}