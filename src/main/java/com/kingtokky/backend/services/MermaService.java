package com.kingtokky.backend.services;

import com.kingtokky.backend.dto.MermaDto;
import com.kingtokky.backend.entities.Merma;
import com.kingtokky.backend.repositories.MermaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MermaService {

    private final MermaRepository mermaRepository;

    public List<MermaDto> listarMermas() {
        return mermaRepository.findAll().stream()
                .map(MermaDto::new)
                .collect(Collectors.toList());
    }

    public MermaDto registrarMerma(MermaDto mermaDto) {
        Merma merma = new Merma();
        merma.setIdInsumo(mermaDto.getIdInsumo());
        merma.setCantidadPerdida(mermaDto.getCantidadPerdida());
        merma.setMotivo(mermaDto.getMotivo());
        merma.setFechaRegistro(LocalDateTime.now());

        Merma guardada = mermaRepository.save(merma);
        return new MermaDto(guardada);
    }
}