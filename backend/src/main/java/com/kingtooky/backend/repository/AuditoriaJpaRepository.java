package com.kingtooky.backend.repository;

import com.kingtooky.backend.infrastructure.entity.AuditoriaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditoriaJpaRepository extends JpaRepository<AuditoriaEntity, Long> {

    @Query("select a from AuditoriaEntity a where (:idUsuario = 0 or a.idUsuario = :idUsuario) "
            + "and (:accion = '' or a.accion = :accion) and (:entidad = '' or a.entidad = :entidad) "
            + "and a.fechaHora >= :desde and a.fechaHora < :hasta order by a.fechaHora desc, a.idAuditoria desc")
    Page<AuditoriaEntity> buscar(@Param("idUsuario") Integer idUsuario, @Param("accion") String accion,
                                 @Param("entidad") String entidad, @Param("desde") LocalDateTime desde,
                                 @Param("hasta") LocalDateTime hasta, Pageable pageable);
}