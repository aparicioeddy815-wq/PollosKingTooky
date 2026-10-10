package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteJpaRepository extends JpaRepository<ClienteEntity, Integer> {
}
