package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.ControlInventarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ControlInventarioJpaRepository extends JpaRepository<ControlInventarioEntity, Integer> {

    @Query("select c from ControlInventarioEntity c where (:tipo = '' or c.tipo = :tipo) "
            + "and (:estado = '' or c.estado = :estado) order by c.fecha desc, c.idControl desc")
    Page<ControlInventarioEntity> buscar(@Param("tipo") String tipo, @Param("estado") String estado, Pageable pageable);
}