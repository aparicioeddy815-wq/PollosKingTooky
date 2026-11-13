package com.kingtokky.presentation.exception;

import org.springframework.http.HttpStatus;

/** Error de negocio con el código HTTP que debe recibir el frontend. */
public class ApiException extends RuntimeException {

    private final HttpStatus estado;

    public ApiException(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }

    public static ApiException noEncontrado(String mensaje) {
        return new ApiException(HttpStatus.NOT_FOUND, mensaje);
    }

    public static ApiException solicitudInvalida(String mensaje) {
        return new ApiException(HttpStatus.BAD_REQUEST, mensaje);
    }

    public static ApiException conflicto(String mensaje) {
        return new ApiException(HttpStatus.CONFLICT, mensaje);
    }

    public static ApiException reglaDeNegocio(String mensaje) {
        return new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, mensaje);
    }

    public static ApiException noAutorizado(String mensaje) {
        return new ApiException(HttpStatus.UNAUTHORIZED, mensaje);
    }

    public static ApiException prohibido(String mensaje) {
        return new ApiException(HttpStatus.FORBIDDEN, mensaje);
    }
}
