package com.kingtokky.infrastructure.security;

import com.kingtokky.domain.model.seguridad.Permiso;
import com.kingtokky.domain.model.seguridad.Usuario;
import com.kingtokky.infrastructure.config.KingTokkyProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

/** Genera y valida los tokens de acceso (JWT firmado con HS256). */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long accesoMinutos;

    public JwtService(KingTokkyProperties propiedades) {
        this.clave = Keys.hmacShaKeyFor(propiedades.jwt().secreto().getBytes(StandardCharsets.UTF_8));
        this.accesoMinutos = propiedades.jwt().accesoMinutos();
    }

    public String generarAcceso(Usuario usuario) {
        List<String> permisos = usuario.getRol().getPermisos().stream().map(Permiso::getCodigo).sorted().toList();
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getIdUsuario()))
                .claim("usuario", usuario.getNombreUsuario())
                .claim("rol", usuario.getRol().getCodigo())
                .claim("permisos", permisos)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(accesoMinutos, ChronoUnit.MINUTES)))
                .signWith(clave)
                .compact();
    }

    /** Devuelve el usuario del token, o vacío si el token es inválido o venció. */
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
            List<String> permisos = claims.get("permisos", List.class);
            return Optional.of(new UsuarioAutenticado(
                    Long.valueOf(claims.getSubject()),
                    claims.get("usuario", String.class),
                    claims.get("rol", String.class),
                    permisos == null ? List.of() : permisos));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
