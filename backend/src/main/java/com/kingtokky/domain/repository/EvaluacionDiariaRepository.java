package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.cliente.EvaluacionDiaria;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EvaluacionDiariaRepository extends JpaRepository<EvaluacionDiaria, Long> {

    Optional<EvaluacionDiaria> findByUsuario_IdUsuarioAndFecha(Long idUsuario, LocalDate fecha);

    @Query("select avg(e.estrellas) from EvaluacionDiaria e where e.fecha between :desde and :hasta")
    Double promedioEntre(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
