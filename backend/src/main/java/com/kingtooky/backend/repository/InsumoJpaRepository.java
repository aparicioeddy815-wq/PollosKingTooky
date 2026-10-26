package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.InsumoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsumoJpaRepository extends JpaRepository<InsumoEntity, Integer> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdInsumoNot(String nombre, Integer idInsumo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from InsumoEntity i where i.idInsumo = :id")
    Optional<InsumoEntity> findByIdForUpdate(@Param("id") Integer id);

    @Query("select i from InsumoEntity i where lower(i.nombre) like lower(concat('%', :q, '%')) "
            + "and (:tipo = '' or i.tipo = :tipo) "
            + "and (:estado = '' or lower(i.estado) = lower(:estado)) "
            + "and (:bajoStock = false or i.stockActual <= i.stockMinimo)")
    Page<InsumoEntity> buscar(@Param("q") String q, @Param("tipo") String tipo, @Param("estado") String estado,
                              @Param("bajoStock") boolean bajoStock, Pageable pageable);

    @Query("select i from InsumoEntity i where i.stockActual <= i.stockMinimo and lower(i.estado) = 'activo'")
    List<InsumoEntity> findBajoStock();
}