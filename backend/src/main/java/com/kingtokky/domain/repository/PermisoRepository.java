package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.seguridad.Permiso;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
    List<Permiso> findByCodigoIn(Collection<String> codigos);
    List<Permiso> findAllByOrderByCodigoAsc();
}
