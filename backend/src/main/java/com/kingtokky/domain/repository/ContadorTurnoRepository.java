package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.pedido.ContadorTurno;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContadorTurnoRepository extends JpaRepository<ContadorTurno, LocalDate> {

    /** Bloquea la fila del día para que dos cajas no reciban el mismo número de turno. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from ContadorTurno c where c.fecha = :fecha")
    Optional<ContadorTurno> buscarParaActualizar(@Param("fecha") LocalDate fecha);
}
