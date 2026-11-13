package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.reporte.HorarioAtencion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioAtencionRepository extends JpaRepository<HorarioAtencion, Long> {
    List<HorarioAtencion> findAllByOrderByOrdenAsc();
}
