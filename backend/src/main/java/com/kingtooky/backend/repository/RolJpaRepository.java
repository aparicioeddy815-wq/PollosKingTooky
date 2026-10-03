package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolJpaRepository extends JpaRepository<RolEntity, Integer> {
}
