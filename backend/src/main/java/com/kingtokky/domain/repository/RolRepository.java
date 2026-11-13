package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.seguridad.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByCodigo(String codigo);
}
