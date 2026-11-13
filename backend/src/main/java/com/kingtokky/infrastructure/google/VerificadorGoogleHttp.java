package com.kingtokky.infrastructure.google;

import com.kingtokky.domain.port.VerificadorGoogle;
import com.kingtokky.infrastructure.config.KingTokkyProperties;
import com.kingtokky.presentation.exception.ApiException;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Valida el ID token de Google consultando el servicio oficial de Google. */
@Component
public class VerificadorGoogleHttp implements VerificadorGoogle {

    private final String clientId;
    private final RestClient cliente = RestClient.create();

    public VerificadorGoogleHttp(KingTokkyProperties propiedades) {
        this.clientId = propiedades.google().clientId();
    }

    @Override
    public CuentaGoogle verificar(String idToken) {
        if (clientId == null || clientId.isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "El inicio de sesión con Google no está configurado (falta GOOGLE_CLIENT_ID)");
        }
        Map<String, Object> datos;
        try {
            datos = cliente.get()
                    .uri("https://oauth2.googleapis.com/tokeninfo?id_token={token}", idToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (RestClientException e) {
            throw ApiException.noAutorizado("No se pudo validar tu cuenta de Google");
        }
        if (datos == null || !clientId.equals(datos.get("aud")) || !"true".equals(String.valueOf(datos.get("email_verified")))) {
            throw ApiException.noAutorizado("La cuenta de Google no es válida para este sistema");
        }
        String correo = String.valueOf(datos.get("email"));
        String nombre = datos.get("name") != null ? String.valueOf(datos.get("name")) : correo;
        String foto = datos.get("picture") != null ? String.valueOf(datos.get("picture")) : null;
        return new CuentaGoogle(correo, nombre, foto);
    }
}
