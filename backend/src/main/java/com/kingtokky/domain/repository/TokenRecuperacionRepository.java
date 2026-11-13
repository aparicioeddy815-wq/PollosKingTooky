package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.seguridad.TokenRecuperacion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {
    Optional<TokenRecuperacion> findByTokenHash(String tokenHash);
}
