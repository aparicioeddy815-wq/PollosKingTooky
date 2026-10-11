package com.kingtooky.backend.security.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final Key key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.access-expiration-ms:3600000}") long accessExpirationMs,
                   @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    private String generar(String username, String tipo, long expirationMs) {
        long ahora = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(username)
                .claim("tipo", tipo)
                .setIssuedAt(new Date(ahora))
                .setExpiration(new Date(ahora + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(String username) {
        return generar(username, TIPO_ACCESS, accessExpirationMs);
    }

    public String generateRefreshToken(String username) {
        return generar(username, TIPO_REFRESH, refreshExpirationMs);
    }

    private Claims claims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    public String extractTipo(String token) {
        return claims(token).get("tipo", String.class);
    }

    public Date extractExpiration(String token) {
        return claims(token).getExpiration();
    }

    public boolean validateToken(String token) {
        try {
            claims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        return validateToken(token) && TIPO_ACCESS.equals(extractTipo(token));
    }

    public boolean isRefreshToken(String token) {
        return validateToken(token) && TIPO_REFRESH.equals(extractTipo(token));
    }
}