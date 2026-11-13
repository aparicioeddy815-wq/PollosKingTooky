package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.reporte.CierreCaja;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CierreCajaRepository extends JpaRepository<CierreCaja, Long> {
    List<CierreCaja> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime desde, LocalDateTime hasta);
}
