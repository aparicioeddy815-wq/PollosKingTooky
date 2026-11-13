package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.comun.ArchivoDescarga;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/** Arma la respuesta HTTP de una descarga con el nombre de archivo correcto. */
final class Descargas {

    private Descargas() {}

    static ResponseEntity<org.springframework.core.io.Resource> de(ArchivoDescarga a) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(a.nombre()).build().toString())
                .contentType(MediaType.parseMediaType(a.tipoContenido()))
                .body(a.contenido());
    }

    static ResponseEntity<org.springframework.core.io.Resource> de(String nombre, String tipoContenido, byte[] bytes) {
        return de(new ArchivoDescarga(nombre, tipoContenido, new ByteArrayResource(bytes)));
    }
}
