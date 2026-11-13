package com.kingtokky.domain.repository;

import com.kingtokky.domain.model.seguridad.RefreshToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /** Usuarios con al menos una sesión vigente usada recientemente ("sesiones en vivo"). */
    @Query("""
            select count(distinct r.usuario.idUsuario) from RefreshToken r
            where r.revocado = false and r.expiraEn > :ahora and r.ultimoUso >= :desde
            """)
    long contarSesionesVivas(@Param("ahora") LocalDateTime ahora, @Param("desde") LocalDateTime desde);

    @Modifying
    @Query("delete from RefreshToken r where r.expiraEn < :limite")
    int borrarVencidos(@Param("limite") LocalDateTime limite);
}
