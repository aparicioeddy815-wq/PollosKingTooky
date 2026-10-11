package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.UsuarioEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, Integer> {

    Optional<UsuarioEntity> findByNombreUsuario(String nombreUsuario);

    boolean existsByNombreUsuarioIgnoreCase(String nombreUsuario);

    boolean existsByNombreUsuarioIgnoreCaseAndIdUsuarioNot(String nombreUsuario, Integer idUsuario);

    @Query("select u from UsuarioEntity u where "
            + "(lower(u.nombreUsuario) like lower(concat('%', :q, '%')) or lower(u.nombreCompleto) like lower(concat('%', :q, '%'))) "
            + "and (:estado = -1 or u.estado = :estado)")
    Page<UsuarioEntity> buscar(@Param("q") String q, @Param("estado") Integer estado, Pageable pageable);
}