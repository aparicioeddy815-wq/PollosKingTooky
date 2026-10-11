package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.ProductoInsumoEntity;
import com.kingtooky.backend.infrastructure.entity.ProductoInsumoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoInsumoJpaRepository extends JpaRepository<ProductoInsumoEntity, ProductoInsumoId> {

    @Query("select pi from ProductoInsumoEntity pi where pi.id.idProducto = :idProducto")
    List<ProductoInsumoEntity> findByProducto(@Param("idProducto") Integer idProducto);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from ProductoInsumoEntity pi where pi.id.idProducto = :idProducto")
    void eliminarPorProducto(@Param("idProducto") Integer idProducto);
}