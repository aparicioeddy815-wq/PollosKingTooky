package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.AuthDtos.GoogleRequest;
import com.kingtokky.application.dto.AuthDtos.LoginRequest;
import com.kingtokky.application.dto.AuthDtos.OlvideContrasenaRequest;
import com.kingtokky.application.dto.AuthDtos.PerfilResponse;
import com.kingtokky.application.dto.AuthDtos.RefreshRequest;
import com.kingtokky.application.dto.AuthDtos.RegistroRequest;
import com.kingtokky.application.dto.AuthDtos.RestablecerContrasenaRequest;
import com.kingtokky.application.dto.AuthDtos.SesionResponse;
import com.kingtokky.application.dto.comun.MensajeResponse;
import com.kingtokky.application.service.AuthService;
import com.kingtokky.application.service.PerfilService;
import com.kingtokky.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Login, registro, renovación de sesión, Google y recuperación de contraseña")
public class AuthController {

    private final AuthService auth;
    private final PerfilService perfil;

    public AuthController(AuthService auth, PerfilService perfil) {
        this.auth = auth;
        this.perfil = perfil;
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión con usuario o correo y contraseña (opción «Recordarme» extiende la sesión a 30 días)")
    public SesionResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }

    @PostMapping("/register")
    @Operation(summary = "Registra un cliente nuevo y abre su sesión")
    public ResponseEntity<SesionResponse> registrar(@Valid @RequestBody RegistroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.registrar(req));
    }

    @PostMapping("/google")
    @Operation(summary = "Inicia sesión con una cuenta de Google (envía el ID token que entrega Google Identity Services)")
    public SesionResponse google(@Valid @RequestBody GoogleRequest req) {
        return auth.loginGoogle(req.idToken());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Cambia un refresh token por un nuevo par de tokens")
    public SesionResponse refrescar(@Valid @RequestBody RefreshRequest req) {
        return auth.refrescar(req.refreshToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "Cierra la sesión revocando el refresh token")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        auth.cerrarSesion(req.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/olvide-contrasena")
    @Operation(summary = "Envía por correo un enlace para restablecer la contraseña")
    public MensajeResponse olvide(@Valid @RequestBody OlvideContrasenaRequest req) {
        return auth.olvideContrasena(req.correo());
    }

    @PostMapping("/restablecer-contrasena")
    @Operation(summary = "Define una nueva contraseña con el token recibido por correo")
    public MensajeResponse restablecer(@Valid @RequestBody RestablecerContrasenaRequest req) {
        return auth.restablecerContrasena(req.token(), req.nuevaContrasena());
    }

    @GetMapping("/me")
    @Operation(summary = "Datos del usuario autenticado")
    public PerfilResponse yo(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return perfil.obtener(usuario.idUsuario());
    }
}
