package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.MermaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface MermaJpaRepository extends JpaRepository<MermaEntity, Integer> {

    @Query("select m from MermaEntity m where (:idInsumo = 0 or m.insumo.idInsumo = :idInsumo) "
            + "and m.fechaHora >= :desde and m.fechaHora < :hasta order by m.fechaHora desc, m.idMerma desc")
    Page<MermaEntity> buscar(@Param("idInsumo") Integer idInsumo, @Param("desde") LocalDateTime desde,
                             @Param("hasta") LocalDateTime hasta, Pageable pageable);
}