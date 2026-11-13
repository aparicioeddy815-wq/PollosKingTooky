package com.kingtokky.presentation.exception;

import java.time.LocalDateTime;
import java.util.List;

/** Cuerpo estándar de error; coincide con la interfaz ErrorApi del frontend Angular. */
public record ErrorApi(LocalDateTime timestamp, int status, String error, String mensaje, String ruta, List<String> detalles) {

    public static ErrorApi de(int status, String error, String mensaje, String ruta, List<String> detalles) {
        return new ErrorApi(LocalDateTime.now(), status, error, mensaje, ruta, detalles);
    }
}
