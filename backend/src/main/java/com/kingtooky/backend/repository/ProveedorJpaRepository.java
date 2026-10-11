package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.ProveedorEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorJpaRepository extends JpaRepository<ProveedorEntity, Integer> {

    @Query("select p from ProveedorEntity p where lower(p.nombre) like lower(concat('%', :q, '%'))")
    Page<ProveedorEntity> buscar(@Param("q") String q, Pageable pageable);
}