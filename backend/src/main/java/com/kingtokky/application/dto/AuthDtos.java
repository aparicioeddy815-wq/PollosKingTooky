package com.kingtokky.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class AuthDtos {

    private AuthDtos() {}

    public record LoginRequest(@NotBlank String nombreUsuario, @NotBlank String contrasena, boolean recordarme) {}

    public record RegistroRequest(
            @NotBlank @Pattern(regexp = "^[A-Za-z0-9._-]{3,50}$", message = "debe tener de 3 a 50 caracteres: letras, números, punto, guion o guion bajo") String nombreUsuario,
            @NotBlank @Size(max = 255) String nombreCompleto,
            @NotBlank @Email @Size(max = 255) String correo,
            @NotBlank @Size(min = 6, max = 100) String contrasena) {}

    public record RefreshRequest(@NotBlank String refreshToken) {}

    public record OlvideContrasenaRequest(@NotBlank @Email String correo) {}

    public record RestablecerContrasenaRequest(@NotBlank String token, @NotBlank @Size(min = 6, max = 100) String nuevaContrasena) {}

    public record GoogleRequest(@NotBlank String idToken) {}

    /** Respuesta de login/registro/refresh; los nombres coinciden con la interfaz Sesion del frontend. */
    public record SesionResponse(String accessToken, String refreshToken, String tokenType, Long idUsuario, String nombreUsuario,
                                 String nombreCompleto, String correo, String rol, String rolCodigo, String fotoUrl, List<String> permisos) {}

    public record PerfilResponse(Long idUsuario, String nombreUsuario, String nombreCompleto, String correo, String fotoUrl, String rol,
                                 LocalDate fechaIngreso, LocalDateTime ultimoAcceso, int totalAccesos, double accesosPorSemana) {}

    public record PerfilRequest(@NotBlank @Size(max = 255) String nombreCompleto, @NotBlank @Email @Size(max = 255) String correo,
                                @Size(max = 500) String fotoUrl) {}

    public record CambiarContrasenaRequest(@NotBlank String contrasenaActual, @NotBlank @Size(min = 6, max = 100) String nuevaContrasena) {}
}
