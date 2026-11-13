package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.auditoria.Bitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BitacoraRepository extends JpaRepository<Bitacora, Long> {

    String FILTRO = """
            select b from Bitacora b
            where b.fechaHora >= :desde and b.fechaHora < :hasta
              and (:todosTipos = true or b.tipo in :tipos)
              and (:todosActores = true or b.rolCodigo = :rol)
              and (:patron = '' or lower(b.accion) like :patron or lower(b.usuarioNombre) like :patron)
            order by b.fechaHora desc, b.id desc
            """;

    @Query(FILTRO)
    Page<Bitacora> filtrar(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                           @Param("todosTipos") boolean todosTipos, @Param("tipos") Collection<TipoEvento> tipos,
                           @Param("todosActores") boolean todosActores, @Param("rol") String rol,
                           @Param("patron") String patron, Pageable pageable);

    @Query(FILTRO)
    List<Bitacora> filtrarTodas(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta,
                                @Param("todosTipos") boolean todosTipos, @Param("tipos") Collection<TipoEvento> tipos,
                                @Param("todosActores") boolean todosActores, @Param("rol") String rol,
                                @Param("patron") String patron, Pageable pageable);

    long countByFechaHoraBetween(LocalDateTime desde, LocalDateTime hasta);

    long countByTipoAndFechaHoraBetween(TipoEvento tipo, LocalDateTime desde, LocalDateTime hasta);

    List<Bitacora> findTop8ByOrderByFechaHoraDescIdDesc();
}
