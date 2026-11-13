package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.seguridad.AccesoUsuario;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccesoUsuarioRepository extends JpaRepository<AccesoUsuario, Long> {
    long countByUsuario_IdUsuarioAndFechaHoraAfter(Long idUsuario, LocalDateTime desde);
}
