package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.reporte.ReporteGenerado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReporteGeneradoRepository extends JpaRepository<ReporteGenerado, Long> {
    Page<ReporteGenerado> findAllByOrderByGeneradoEnDesc(Pageable pageable);
}
