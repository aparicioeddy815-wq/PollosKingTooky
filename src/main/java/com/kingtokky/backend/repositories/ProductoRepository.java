package com.kingtokky.backend.repositories;

import com.kingtokky.backend.entities.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
}