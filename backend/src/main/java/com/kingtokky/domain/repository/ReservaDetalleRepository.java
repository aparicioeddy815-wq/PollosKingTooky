package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.reserva.ReservaDetalle;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservaDetalleRepository extends JpaRepository<ReservaDetalle, Long> {
    List<ReservaDetalle> findByReserva_Id(Long idReserva);
    List<ReservaDetalle> findByReserva_IdIn(Collection<Long> idsReserva);
}
