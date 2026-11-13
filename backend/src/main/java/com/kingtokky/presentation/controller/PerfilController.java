package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.AuthDtos.CambiarContrasenaRequest;
import com.kingtokky.application.dto.AuthDtos.PerfilRequest;
import com.kingtokky.application.dto.AuthDtos.PerfilResponse;
import com.kingtokky.application.dto.comun.MensajeResponse;
import com.kingtokky.application.service.PerfilService;
import com.kingtokky.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/perfil")
@Tag(name = "Perfil", description = "Datos propios, contraseña y foto (cualquier usuario autenticado)")
public class PerfilController {

    private final PerfilService perfil;

    public PerfilController(PerfilService perfil) {
        this.perfil = perfil;
    }

    @GetMapping
    @Operation(summary = "Mi perfil, con fecha de ingreso, último acceso y frecuencia de visitas")
    public PerfilResponse obtener(@AuthenticationPrincipal UsuarioAutenticado u) {
        return perfil.obtener(u.idUsuario());
    }

    @PutMapping
    @Operation(summary = "Actualiza mi nombre, correo y foto (URL)")
    public PerfilResponse actualizar(@AuthenticationPrincipal UsuarioAutenticado u, @Valid @RequestBody PerfilRequest req) {
        return perfil.actualizar(u.idUsuario(), req);
    }

    @PostMapping("/contrasena")
    @Operation(summary = "Cambia mi contraseña")
    public MensajeResponse cambiarContrasena(@AuthenticationPrincipal UsuarioAutenticado u, @Valid @RequestBody CambiarContrasenaRequest req) {
        return perfil.cambiarContrasena(u.idUsuario(), req);
    }

    @PostMapping(value = "/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Sube mi foto de perfil (PNG, JPG o WEBP, máximo 5 MB; campo «foto»)")
    public PerfilResponse subirFoto(@AuthenticationPrincipal UsuarioAutenticado u, @RequestPart("foto") MultipartFile foto) {
        return perfil.subirFoto(u.idUsuario(), foto);
    }
}
