package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.DetalleControlInventarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DetalleControlInventarioJpaRepository extends JpaRepository<DetalleControlInventarioEntity, Integer> {

    List<DetalleControlInventarioEntity> findByIdControl(Integer idControl);

    Optional<DetalleControlInventarioEntity> findByIdControlAndInsumo_IdInsumo(Integer idControl, Integer idInsumo);
}