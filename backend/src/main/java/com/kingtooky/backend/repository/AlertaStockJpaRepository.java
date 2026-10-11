package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.AlertaStockEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertaStockJpaRepository extends JpaRepository<AlertaStockEntity, Integer> {

    @Query("select a from AlertaStockEntity a where (:estado = '' or a.estado = :estado) order by a.fechaHora desc, a.idAlerta desc")
    Page<AlertaStockEntity> buscar(@Param("estado") String estado, Pageable pageable);

    Optional<AlertaStockEntity> findFirstByInsumo_IdInsumoAndEstado(Integer idInsumo, String estado);

    List<AlertaStockEntity> findByInsumo_IdInsumoAndEstado(Integer idInsumo, String estado);

    long countByEstado(String estado);
}