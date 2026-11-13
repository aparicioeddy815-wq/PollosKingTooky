package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.cliente.EstadoQueja;
import com.kingtokky.domain.model.cliente.Queja;
import com.kingtokky.domain.model.cliente.TipoQueja;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuejaRepository extends JpaRepository<Queja, Long> {

    @Query("""
            select q from Queja q
            where (:todos = true or q.tipo = :tipo) and q.creadoEn >= :desde and q.creadoEn < :hasta
            order by q.creadoEn desc
            """)
    Page<Queja> filtrar(@Param("todos") boolean todos, @Param("tipo") TipoQueja tipo,
                        @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta, Pageable pageable);

    @Query("""
            select q from Queja q
            where (:todos = true or q.tipo = :tipo) and q.creadoEn >= :desde and q.creadoEn < :hasta
            order by q.creadoEn desc
            """)
    List<Queja> filtrarTodas(@Param("todos") boolean todos, @Param("tipo") TipoQueja tipo,
                             @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    Page<Queja> findByCliente_IdUsuarioOrderByCreadoEnDesc(Long idCliente, Pageable pageable);

    long countByCreadoEnBetween(LocalDateTime desde, LocalDateTime hasta);

    long countByTipoAndCreadoEnBetween(TipoQueja tipo, LocalDateTime desde, LocalDateTime hasta);

    long countByEstadoNot(EstadoQueja estado);

    @Query("select q.creadoEn, q.respondidoEn from Queja q where q.respondidoEn is not null and q.creadoEn >= :desde and q.creadoEn < :hasta")
    List<Object[]> tiemposDeRespuesta(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
