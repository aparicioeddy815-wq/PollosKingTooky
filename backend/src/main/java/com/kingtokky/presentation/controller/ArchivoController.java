package com.kingtokky.presentation.controller;

import com.kingtokky.domain.port.AlmacenArchivos;
import com.kingtokky.presentation.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.TimeUnit;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/archivos")
@Tag(name = "Archivos", description = "Fotos de perfil (públicas)")
public class ArchivoController {

    private final AlmacenArchivos almacen;

    public ArchivoController(AlmacenArchivos almacen) {
        this.almacen = almacen;
    }

    @GetMapping("/fotos/{nombre:.+}")
    @Operation(summary = "Devuelve una foto de perfil")
    public ResponseEntity<Resource> foto(@PathVariable String nombre) {
        String ruta = "fotos/" + nombre;
        if (nombre.contains("/") || nombre.contains("..") || !almacen.existe(ruta)) {
            throw ApiException.noEncontrado("Foto no encontrada");
        }
        String minusculas = nombre.toLowerCase();
        MediaType tipo = minusculas.endsWith(".png") ? MediaType.IMAGE_PNG
                : minusculas.endsWith(".webp") ? MediaType.parseMediaType("image/webp") : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(tipo).cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS)).body(almacen.cargar(ruta));
    }
}
