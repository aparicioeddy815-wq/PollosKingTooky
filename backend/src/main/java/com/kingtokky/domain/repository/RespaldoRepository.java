package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.reporte.Respaldo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RespaldoRepository extends JpaRepository<Respaldo, Long> {
    Page<Respaldo> findAllByOrderByFechaHoraDesc(Pageable pageable);
}
