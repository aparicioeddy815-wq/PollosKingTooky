package com.kingtooky.backend.presentation.controller;

import com.kingtooky.backend.application.dto.AuthRequest;
import com.kingtooky.backend.application.dto.AuthResponse;
import com.kingtooky.backend.application.dto.RefreshRequest;
import com.kingtooky.backend.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refrescar(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) RefreshRequest body) {
        String access = (authorization != null && authorization.startsWith("Bearer ")) ? authorization.substring(7) : null;
        authService.logout(access, body != null ? body.getRefreshToken() : null);
        return ResponseEntity.ok(Map.of("mensaje", "Sesion cerrada"));
    }

    @GetMapping("/me")
    public AuthResponse me() {
        return authService.perfilActual();
    }
}