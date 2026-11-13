package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.menu.RecetaInsumo;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecetaInsumoRepository extends JpaRepository<RecetaInsumo, Long> {
    List<RecetaInsumo> findByProducto_IdIn(Collection<Long> idsProducto);
}
