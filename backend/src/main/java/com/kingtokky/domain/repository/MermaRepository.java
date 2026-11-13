package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.inventario.Merma;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MermaRepository extends JpaRepository<Merma, Long> {

    Page<Merma> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime desde, LocalDateTime hasta, Pageable pageable);

    List<Merma> findByFechaHoraBetweenOrderByFechaHoraDesc(LocalDateTime desde, LocalDateTime hasta);

    @Query("select coalesce(sum(m.costoTotal), 0) from Merma m where m.fechaHora >= :desde and m.fechaHora < :hasta")
    BigDecimal costoEntre(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
