package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.inventario.ControlInventario;
import com.kingtokky.domain.model.inventario.EstadoControl;
import com.kingtokky.domain.model.inventario.TipoControl;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ControlInventarioRepository extends JpaRepository<ControlInventario, Long> {
    List<ControlInventario> findAllByOrderByFechaDescIdDesc(Pageable pageable);
    Optional<ControlInventario> findFirstByTipoAndEstado(TipoControl tipo, EstadoControl estado);
    boolean existsByTipoAndFecha(TipoControl tipo, LocalDate fecha);
    Optional<ControlInventario> findFirstByTipoAndEstadoOrderByFechaDesc(TipoControl tipo, EstadoControl estado);
}
