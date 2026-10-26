package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.MovimientoInventarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MovimientoInventarioJpaRepository extends JpaRepository<MovimientoInventarioEntity, Integer> {

    @Query("select m from MovimientoInventarioEntity m where (:idInsumo = 0 or m.insumo.idInsumo = :idInsumo) "
            + "and (:tipo = '' or m.tipo = :tipo) and m.fechaHora >= :desde and m.fechaHora < :hasta "
            + "order by m.fechaHora desc, m.idMovimiento desc")
    Page<MovimientoInventarioEntity> buscar(@Param("idInsumo") Integer idInsumo, @Param("tipo") String tipo,
                                            @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                                            Pageable pageable);
}