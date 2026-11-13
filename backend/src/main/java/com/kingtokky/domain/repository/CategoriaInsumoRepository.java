package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.inventario.CategoriaInsumo;
import com.kingtokky.domain.model.inventario.TipoInsumo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaInsumoRepository extends JpaRepository<CategoriaInsumo, Long> {
    List<CategoriaInsumo> findByTipoOrderByNombreAsc(TipoInsumo tipo);
}
