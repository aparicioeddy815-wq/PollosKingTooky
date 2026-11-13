package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.inventario.Proveedor;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findAllByOrderByNombreAsc();
}
