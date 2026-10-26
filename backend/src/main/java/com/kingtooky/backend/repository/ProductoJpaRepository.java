package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.ProductoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoJpaRepository extends JpaRepository<ProductoEntity, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdProductoNot(String nombre, Integer idProducto);

    @Query("select p from ProductoEntity p where lower(p.nombre) like lower(concat('%', :q, '%')) "
            + "and (:filtrar = false or p.disponible = :disponible)")
    Page<ProductoEntity> buscar(@Param("q") String q, @Param("filtrar") boolean filtrar,
                                @Param("disponible") boolean disponible, Pageable pageable);
}