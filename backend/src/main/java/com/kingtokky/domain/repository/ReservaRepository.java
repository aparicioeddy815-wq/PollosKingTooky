package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.reserva.EstadoReserva;
import com.kingtokky.domain.model.reserva.Reserva;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    @Query("""
            select r from Reserva r
            where r.cliente.idUsuario = :idCliente and (:todos = true or r.estado = :estado)
            order by r.fechaEncuentro desc, r.hora desc
            """)
    Page<Reserva> misReservas(@Param("idCliente") Long idCliente, @Param("todos") boolean todos,
                              @Param("estado") EstadoReserva estado, Pageable pageable);

    @Query("""
            select r from Reserva r
            where (:todosEstados = true or r.estado = :estado) and (:todasFechas = true or r.fechaEncuentro = :fecha)
            order by r.fechaEncuentro desc, r.hora desc
            """)
    Page<Reserva> gestion(@Param("todosEstados") boolean todosEstados, @Param("estado") EstadoReserva estado,
                          @Param("todasFechas") boolean todasFechas, @Param("fecha") LocalDate fecha, Pageable pageable);

    /** Personas ya comprometidas en una franja horaria [desde, hasta). */
    @Query("""
            select coalesce(sum(r.personas), 0L) from Reserva r
            where r.fechaEncuentro = :fecha and r.hora >= :desde and r.hora < :hasta and r.estado in :estados
            """)
    Long personasEnFranja(@Param("fecha") LocalDate fecha, @Param("desde") LocalTime desde, @Param("hasta") LocalTime hasta,
                          @Param("estados") Collection<EstadoReserva> estados);

    List<Reserva> findByEstadoAndLimitePagoBefore(EstadoReserva estado, LocalDateTime limite);

    long countByCodigoStartingWith(String prefijo);

    long countByCliente_IdUsuario(Long idCliente);

    long countByCliente_IdUsuarioAndEstado(Long idCliente, EstadoReserva estado);

    @Query("""
            select r from Reserva r
            where r.cliente.idUsuario = :idCliente and r.estado in :estados and r.fechaEncuentro >= :desde
            order by r.fechaEncuentro, r.hora
            """)
    List<Reserva> proximas(@Param("idCliente") Long idCliente, @Param("estados") Collection<EstadoReserva> estados,
                           @Param("desde") LocalDate desde, Pageable pageable);
}
