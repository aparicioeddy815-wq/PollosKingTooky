package com.kingtokky.backend.repositories;

import com.kingtokky.backend.entities.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventarioRepository extends JpaRepository<Inventario, Integer> {
}