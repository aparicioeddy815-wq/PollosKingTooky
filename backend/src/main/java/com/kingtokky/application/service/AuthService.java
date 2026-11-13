package com.kingtokky.application.service;

import com.kingtokky.application.dto.AuthDtos.LoginRequest;
import com.kingtokky.application.dto.AuthDtos.RegistroRequest;
import com.kingtokky.application.dto.AuthDtos.SesionResponse;
import com.kingtokky.application.dto.comun.MensajeResponse;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.seguridad.AccesoUsuario;
import com.kingtokky.domain.model.seguridad.EstadoUsuario;
import com.kingtokky.domain.model.seguridad.Permiso;
import com.kingtokky.domain.model.seguridad.ProveedorAcceso;
import com.kingtokky.domain.model.seguridad.RefreshToken;
import com.kingtokky.domain.model.seguridad.Rol;
import com.kingtokky.domain.model.seguridad.TokenRecuperacion;
import com.kingtokky.domain.model.seguridad.Usuario;
import com.kingtokky.domain.port.NotificadorCorreo;
import com.kingtokky.domain.port.VerificadorGoogle;
import com.kingtokky.domain.repository.AccesoUsuarioRepository;
import com.kingtokky.domain.repository.RefreshTokenRepository;
import com.kingtokky.domain.repository.RolRepository;
import com.kingtokky.domain.repository.TokenRecuperacionRepository;
import com.kingtokky.domain.repository.UsuarioRepository;
import com.kingtokky.infrastructure.config.KingTokkyProperties;
import com.kingtokky.infrastructure.security.JwtService;
import com.kingtokky.infrastructure.security.TokenUtil;
import com.kingtokky.presentation.exception.ApiException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final int MAX_INTENTOS = 5;
    private static final int MINUTOS_BLOQUEO = 10;
    private static final String ROL_CLIENTE = "ROL-CLIENTE";

    private record Intentos(int cantidad, LocalDateTime desbloqueo) {}

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final RefreshTokenRepository refreshTokens;
    private final TokenRecuperacionRepository recuperaciones;
    private final AccesoUsuarioRepository accesos;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final AuditoriaService auditoria;
    private final NotificadorCorreo correo;
    private final VerificadorGoogle google;
    private final KingTokkyProperties propiedades;
    private final Map<String, Intentos> intentosFallidos = new ConcurrentHashMap<>();

    public AuthService(UsuarioRepository usuarios, RolRepository roles, RefreshTokenRepository refreshTokens,
                       TokenRecuperacionRepository recuperaciones, AccesoUsuarioRepository accesos, PasswordEncoder encoder,
                       JwtService jwt, AuditoriaService auditoria, NotificadorCorreo correo, VerificadorGoogle google,
                       KingTokkyProperties propiedades) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.refreshTokens = refreshTokens;
        this.recuperaciones = recuperaciones;
        this.accesos = accesos;
        this.encoder = encoder;
        this.jwt = jwt;
        this.auditoria = auditoria;
        this.correo = correo;
        this.google = google;
        this.propiedades = propiedades;
    }

    @Transactional
    public SesionResponse login(LoginRequest req) {
        String clave = req.nombreUsuario().trim();
        verificarBloqueo(clave);
        Usuario usuario = usuarios.findByNombreUsuarioIgnoreCaseOrCorreoIgnoreCase(clave, clave).orElse(null);
        boolean valido = usuario != null && usuario.getContrasenaHash() != null && encoder.matches(req.contrasena(), usuario.getContrasenaHash());
        if (!valido) {
            registrarFallo(clave);
            throw ApiException.noAutorizado("Usuario o contraseña incorrectos");
        }
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            auditoria.registrarComo(usuario.getIdUsuario(), usuario.getNombreUsuario(), usuario.getRol().getCodigo(), TipoEvento.SEGURIDAD,
                    "Seguridad", "Intento de ingreso con una cuenta " + usuario.getEstado().name().toLowerCase(), NivelBitacora.ALERTA);
            throw ApiException.prohibido("Tu cuenta está suspendida o inactiva. Comunícate con la administración");
        }
        intentosFallidos.remove(clave.toLowerCase());
        return abrirSesion(usuario, req.recordarme());
    }

    @Transactional
    public SesionResponse registrar(RegistroRequest req) {
        if (usuarios.existsByNombreUsuarioIgnoreCase(req.nombreUsuario())) {
            throw ApiException.conflicto("El nombre de usuario ya está en uso");
        }
        if (usuarios.existsByCorreoIgnoreCase(req.correo())) {
            throw ApiException.conflicto("Ya existe una cuenta con ese correo");
        }
        Usuario u = new Usuario();
        u.setNombreUsuario(req.nombreUsuario().trim());
        u.setNombreCompleto(req.nombreCompleto().trim());
        u.setCorreo(req.correo().trim().toLowerCase());
        u.setContrasenaHash(encoder.encode(req.contrasena()));
        u.setRol(rolCliente());
        usuarios.save(u);
        auditoria.registrarComo(u.getIdUsuario(), u.getNombreUsuario(), ROL_CLIENTE, TipoEvento.CREACION, "Seguridad",
                "Se registró una nueva cuenta de cliente", NivelBitacora.INFO);
        return abrirSesion(u, false);
    }

    @Transactional
    public SesionResponse loginGoogle(String idToken) {
        VerificadorGoogle.CuentaGoogle cuenta = google.verificar(idToken);
        Usuario usuario = usuarios.findByCorreoIgnoreCase(cuenta.correo()).orElse(null);
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setNombreUsuario(nombreUsuarioDisponible(cuenta.correo()));
            usuario.setNombreCompleto(cuenta.nombre());
            usuario.setCorreo(cuenta.correo().toLowerCase());
            usuario.setFotoUrl(cuenta.foto());
            usuario.setProveedor(ProveedorAcceso.GOOGLE);
            usuario.setRol(rolCliente());
            usuarios.save(usuario);
        } else if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw ApiException.prohibido("Tu cuenta está suspendida o inactiva. Comunícate con la administración");
        }
        return abrirSesion(usuario, true);
    }

    @Transactional
    public SesionResponse refrescar(String refreshToken) {
        RefreshToken actual = refreshTokens.findByTokenHash(TokenUtil.sha256(refreshToken))
                .filter(t -> !t.isRevocado() && t.getExpiraEn().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> ApiException.noAutorizado("La sesión venció. Vuelve a iniciar sesión"));
        Usuario usuario = actual.getUsuario();
        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            actual.setRevocado(true);
            throw ApiException.prohibido("Tu cuenta está suspendida o inactiva");
        }
        actual.setRevocado(true);
        return emitirTokens(usuario, actual.isRecordarme());
    }

    @Transactional
    public void cerrarSesion(String refreshToken) {
        refreshTokens.findByTokenHash(TokenUtil.sha256(refreshToken)).ifPresent(t -> {
            t.setRevocado(true);
            Usuario u = t.getUsuario();
            auditoria.registrarComo(u.getIdUsuario(), u.getNombreUsuario(), u.getRol().getCodigo(), TipoEvento.CIERRE_SESION,
                    "Seguridad", "Cerró sesión", NivelBitacora.INFO);
        });
    }

    @Transactional
    public MensajeResponse olvideContrasena(String correoUsuario) {
        usuarios.findByCorreoIgnoreCase(correoUsuario.trim()).filter(u -> u.getProveedor() == ProveedorAcceso.LOCAL).ifPresent(u -> {
            String token = TokenUtil.generar();
            TokenRecuperacion t = new TokenRecuperacion();
            t.setUsuario(u);
            t.setTokenHash(TokenUtil.sha256(token));
            t.setExpiraEn(LocalDateTime.now().plusMinutes(60));
            recuperaciones.save(t);
            String enlace = propiedades.frontend().url() + "/restablecer-contrasena?token=" + token;
            correo.enviar(u.getCorreo(), "Restablece tu contraseña de King Tokky",
                    "Hola " + u.getNombreCompleto() + ",\n\nRecibimos una solicitud para restablecer tu contraseña. "
                            + "Ingresa a este enlace (vale por 60 minutos):\n\n" + enlace
                            + "\n\nSi no fuiste tú, ignora este mensaje.\n\nKing Tokky");
        });
        // Siempre la misma respuesta, exista o no el correo, para no revelar qué cuentas existen
        return new MensajeResponse("Si el correo está registrado, te enviamos un enlace para restablecer tu contraseña.");
    }

    @Transactional
    public MensajeResponse restablecerContrasena(String token, String nuevaContrasena) {
        TokenRecuperacion t = recuperaciones.findByTokenHash(TokenUtil.sha256(token))
                .filter(r -> !r.isUsado() && r.getExpiraEn().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> ApiException.solicitudInvalida("El enlace venció o ya fue utilizado. Solicita uno nuevo"));
        Usuario u = t.getUsuario();
        u.setContrasenaHash(encoder.encode(nuevaContrasena));
        t.setUsado(true);
        auditoria.registrarComo(u.getIdUsuario(), u.getNombreUsuario(), u.getRol().getCodigo(), TipoEvento.SEGURIDAD, "Seguridad",
                "Restableció su contraseña mediante el enlace de recuperación", NivelBitacora.ALERTA);
        return new MensajeResponse("Tu contraseña fue actualizada. Ya puedes iniciar sesión.");
    }

    // ------------------------------------------------------------------ internos

    private SesionResponse abrirSesion(Usuario usuario, boolean recordarme) {
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuario.setTotalAccesos(usuario.getTotalAccesos() + 1);
        AccesoUsuario acceso = new AccesoUsuario();
        acceso.setUsuario(usuario);
        acceso.setIp(AuditoriaService.ipActual());
        accesos.save(acceso);
        auditoria.registrarComo(usuario.getIdUsuario(), usuario.getNombreUsuario(), usuario.getRol().getCodigo(),
                TipoEvento.INICIO_SESION, "Seguridad", "Inició sesión correctamente", NivelBitacora.INFO);
        return emitirTokens(usuario, recordarme);
    }

    private SesionResponse emitirTokens(Usuario usuario, boolean recordarme) {
        String refresh = TokenUtil.generar();
        RefreshToken rt = new RefreshToken();
        rt.setUsuario(usuario);
        rt.setTokenHash(TokenUtil.sha256(refresh));
        rt.setRecordarme(recordarme);
        rt.setUltimoUso(LocalDateTime.now());
        long dias = recordarme ? propiedades.jwt().refreshRecordarmeDias() : propiedades.jwt().refreshDias();
        rt.setExpiraEn(LocalDateTime.now().plusDays(dias));
        refreshTokens.save(rt);
        Rol rol = usuario.getRol();
        return new SesionResponse(jwt.generarAcceso(usuario), refresh, "Bearer", usuario.getIdUsuario(), usuario.getNombreUsuario(),
                usuario.getNombreCompleto(), usuario.getCorreo(), rol.getNombre(), rol.getCodigo(), usuario.getFotoUrl(),
                rol.getPermisos().stream().map(Permiso::getCodigo).sorted().toList());
    }

    private Rol rolCliente() {
        return roles.findByCodigo(ROL_CLIENTE).orElseThrow(() -> new IllegalStateException("Falta el rol " + ROL_CLIENTE + " en la base de datos"));
    }

    private String nombreUsuarioDisponible(String correoGoogle) {
        String base = correoGoogle.split("@")[0].replaceAll("[^A-Za-z0-9._-]", "");
        if (base.length() < 3) base = "cliente" + base;
        if (base.length() > 40) base = base.substring(0, 40);
        String candidato = base;
        int n = 1;
        while (usuarios.existsByNombreUsuarioIgnoreCase(candidato)) {
            candidato = base + (++n);
        }
        return candidato;
    }

    private void verificarBloqueo(String clave) {
        Intentos i = intentosFallidos.get(clave.toLowerCase());
        if (i != null && i.desbloqueo() != null && i.desbloqueo().isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos fallidos. Intenta de nuevo en unos minutos");
        }
    }

    private void registrarFallo(String clave) {
        String llave = clave.toLowerCase();
        Intentos previo = intentosFallidos.get(llave);
        int cantidad = (previo == null || (previo.desbloqueo() != null && previo.desbloqueo().isBefore(LocalDateTime.now()))) ? 1 : previo.cantidad() + 1;
        LocalDateTime hasta = cantidad >= MAX_INTENTOS ? LocalDateTime.now().plusMinutes(MINUTOS_BLOQUEO) : null;
        intentosFallidos.put(llave, new Intentos(cantidad, hasta));
        boolean bloqueo = hasta != null;
        auditoria.registrarSistema(TipoEvento.SEGURIDAD, "Seguridad",
                bloqueo ? "Bloqueo temporal por múltiples intentos fallidos para el usuario \"" + clave + "\""
                        : "Intento de acceso fallido para el usuario \"" + clave + "\"",
                bloqueo ? NivelBitacora.CRITICO : NivelBitacora.ALERTA);
    }
}
