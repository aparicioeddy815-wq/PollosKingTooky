package com.kingtokky.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.kingtokky.domain.model.seguridad.Permiso;
import com.kingtokky.domain.model.seguridad.Rol;
import com.kingtokky.domain.model.seguridad.Usuario;
import com.kingtokky.infrastructure.config.KingTokkyProperties;
import com.kingtokky.infrastructure.security.JwtService;
import com.kingtokky.infrastructure.security.UsuarioAutenticado;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService servicio(String secreto) {
        var jwt = new KingTokkyProperties.Jwt(secreto, 30, 1, 30);
        return new JwtService(new KingTokkyProperties(jwt, null, null, null, null, null, null, null));
    }

    private Usuario gerente() {
        Permiso permiso = new Permiso();
        permiso.setCodigo("PANEL_GERENTE");
        Rol rol = new Rol();
        rol.setCodigo("ROL-GERENTE");
        rol.setNombre("GERENTE");
        rol.getPermisos().add(permiso);
        Usuario u = new Usuario();
        u.setIdUsuario(5L);
        u.setNombreUsuario("gerente");
        u.setRol(rol);
        return u;
    }

    @Test
    void elTokenGeneradoSeValidaYTraeUsuarioRolYPermisos() {
        JwtService jwt = servicio("clave-de-prueba-con-mas-de-treinta-y-dos-caracteres");
        String token = jwt.generarAcceso(gerente());

        Optional<UsuarioAutenticado> resultado = jwt.validar(token);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().idUsuario()).isEqualTo(5L);
        assertThat(resultado.get().nombreUsuario()).isEqualTo("gerente");
        assertThat(resultado.get().rolCodigo()).isEqualTo("ROL-GERENTE");
        assertThat(resultado.get().tienePermiso("PANEL_GERENTE")).isTrue();
        assertThat(resultado.get().tienePermiso("PANEL_ADMIN")).isFalse();
    }

    @Test
    void unTokenFirmadoConOtraClaveSeRechaza() {
        String token = servicio("una-clave-secreta-distinta-de-treinta-y-dos-o-mas").generarAcceso(gerente());
        assertThat(servicio("clave-de-prueba-con-mas-de-treinta-y-dos-caracteres").validar(token)).isEmpty();
    }

    @Test
    void unTokenInventadoSeRechaza() {
        assertThat(servicio("clave-de-prueba-con-mas-de-treinta-y-dos-caracteres").validar("esto.no.es-un-jwt")).isEmpty();
    }
}
