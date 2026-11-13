package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.menu.CategoriaProducto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, Long> {
    List<CategoriaProducto> findAllByOrderByOrdenAsc();
    Optional<CategoriaProducto> findByCodigo(String codigo);
}
