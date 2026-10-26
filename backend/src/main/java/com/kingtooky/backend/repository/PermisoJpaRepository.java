package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.PermisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermisoJpaRepository extends JpaRepository<PermisoEntity, Integer> {
}