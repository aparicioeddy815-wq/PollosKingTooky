package com.kingtooky.backend.security.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final Map<String, Date> tokens = new ConcurrentHashMap<>();

    public void invalidar(String token, Date expiracion) {
        tokens.put(token, expiracion);
    }

    public boolean estaInvalidado(String token) {
        return tokens.containsKey(token);
    }

    @Scheduled(fixedRate = 3600000)
    public void limpiarExpirados() {
        Date ahora = new Date();
        tokens.entrySet().removeIf(e -> e.getValue().before(ahora));
    }
}