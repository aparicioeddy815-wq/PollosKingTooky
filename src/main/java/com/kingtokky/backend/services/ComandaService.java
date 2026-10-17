package com.kingtokky.backend.services;

import com.kingtokky.backend.dto.ComandaDto;
import com.kingtokky.backend.entities.Comanda;
import com.kingtokky.backend.repositories.ComandaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComandaService {

    private final ComandaRepository comandaRepository;

    public List<ComandaDto> listarComandas() {
        return comandaRepository.findAll().stream()
                .map(ComandaDto::new)
                .collect(Collectors.toList());
    }

    public ComandaDto crearComanda(ComandaDto comandaDto) {
        Comanda comanda = new Comanda();
        comanda.setNumeroComanda(comandaDto.getNumeroComanda());
        comanda.setIdUsuario(comandaDto.getIdUsuario());
        comanda.setEstado(comandaDto.getEstado() != null ? comandaDto.getEstado() : "EN_PREPARACION");
        comanda.setFechaHora(LocalDateTime.now());
        comanda.setObservaciones(comandaDto.getObservaciones());

        Comanda guardada = comandaRepository.save(comanda);
        return new ComandaDto(guardada);
    }
}