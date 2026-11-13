package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.entrevista.EstadoVacante;
import com.kingtokky.domain.model.entrevista.Vacante;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VacanteRepository extends JpaRepository<Vacante, Long> {
    List<Vacante> findByEstadoOrderByUrgenciaAscCargoAsc(EstadoVacante estado);
    long countByEstado(EstadoVacante estado);
}
