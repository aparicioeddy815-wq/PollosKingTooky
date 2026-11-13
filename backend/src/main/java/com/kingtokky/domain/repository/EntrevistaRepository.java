package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.entrevista.Entrevista;
import com.kingtokky.domain.model.entrevista.EstadoEntrevista;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntrevistaRepository extends JpaRepository<Entrevista, Long> {
    List<Entrevista> findByFechaHoraBetweenOrderByFechaHoraAsc(LocalDateTime desde, LocalDateTime hasta);
    List<Entrevista> findByEstadoAndFechaHoraGreaterThanEqualOrderByFechaHoraAsc(EstadoEntrevista estado, LocalDateTime desde);
    Optional<Entrevista> findFirstByPostulante_IdOrderByFechaHoraDesc(Long idPostulante);
    long countByEstadoAndFechaHoraBetween(EstadoEntrevista estado, LocalDateTime desde, LocalDateTime hasta);
}
