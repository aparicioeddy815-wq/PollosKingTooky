package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.inventario.AlertaStock;
import com.kingtokky.domain.model.inventario.EstadoAlerta;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertaStockRepository extends JpaRepository<AlertaStock, Long> {
    List<AlertaStock> findByEstadoOrderByCreadaEnDesc(EstadoAlerta estado);
    Optional<AlertaStock> findFirstByInsumo_IdAndEstado(Long idInsumo, EstadoAlerta estado);
    long countByEstado(EstadoAlerta estado);
}
