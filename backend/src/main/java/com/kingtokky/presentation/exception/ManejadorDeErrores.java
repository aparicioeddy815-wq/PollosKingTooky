package com.kingtokky.presentation.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class ManejadorDeErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorDeErrores.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorApi> api(ApiException e, HttpServletRequest req) {
        return responder(e.getEstado(), e.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApi> validacion(MethodArgumentNotValidException e, HttpServletRequest req) {
        List<String> detalles = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return responder(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos", req, detalles);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorApi> solicitudMalFormada(Exception e, HttpServletRequest req) {
        return responder(HttpStatus.BAD_REQUEST, "La solicitud tiene un formato incorrecto o le faltan datos", req, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorApi> accesoDenegado(AccessDeniedException e, HttpServletRequest req) {
        return responder(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción", req, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorApi> metodoNoPermitido(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return responder(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP no permitido para esta ruta", req, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorApi> rutaInexistente(NoResourceFoundException e, HttpServletRequest req) {
        return responder(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", req, List.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorApi> tipoNoSoportado(HttpMediaTypeNotSupportedException e, HttpServletRequest req) {
        return responder(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "El tipo de contenido enviado no es compatible", req, List.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorApi> archivoGrande(MaxUploadSizeExceededException e, HttpServletRequest req) {
        return responder(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo supera el tamaño máximo de 5 MB", req, List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorApi> integridad(DataIntegrityViolationException e, HttpServletRequest req) {
        log.warn("Violación de integridad: {}", e.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT, "La operación choca con datos existentes (valor duplicado o registro en uso)", req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> inesperado(Exception e, HttpServletRequest req) {
        log.error("Error inesperado en {}", req.getRequestURI(), e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado en el servidor", req, List.of());
    }

    private ResponseEntity<ErrorApi> responder(HttpStatus estado, String mensaje, HttpServletRequest req, List<String> detalles) {
        ErrorApi cuerpo = ErrorApi.de(estado.value(), estado.getReasonPhrase(), mensaje, req.getRequestURI(), detalles);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
