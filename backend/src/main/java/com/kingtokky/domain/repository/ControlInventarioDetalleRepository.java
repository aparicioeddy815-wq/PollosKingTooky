package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.inventario.ControlInventarioDetalle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ControlInventarioDetalleRepository extends JpaRepository<ControlInventarioDetalle, Long> {
    List<ControlInventarioDetalle> findByControl_IdOrderByInsumo_NombreAsc(Long idControl);
    long countByControl_Id(Long idControl);
    long countByControl_IdAndStockContadoIsNotNull(Long idControl);
}
