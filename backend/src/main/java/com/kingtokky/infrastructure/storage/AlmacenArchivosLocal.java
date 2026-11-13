package com.kingtokky.infrastructure.storage;

import com.kingtokky.domain.port.AlmacenArchivos;
import com.kingtokky.infrastructure.config.KingTokkyProperties;
import com.kingtokky.presentation.exception.ApiException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/** Guarda los archivos en una carpeta del servidor (en Docker, un volumen). */
@Component
public class AlmacenArchivosLocal implements AlmacenArchivos {

    private final Path raiz;

    public AlmacenArchivosLocal(KingTokkyProperties propiedades) {
        this.raiz = Path.of(propiedades.almacenamiento().directorio()).toAbsolutePath().normalize();
    }

    @Override
    public String guardar(String subcarpeta, String nombreOriginal, InputStream contenido) throws IOException {
        Path carpeta = resolver(subcarpeta);
        Files.createDirectories(carpeta);
        String nombre = UUID.randomUUID() + extension(nombreOriginal);
        Files.copy(contenido, carpeta.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
        return subcarpeta + "/" + nombre;
    }

    @Override
    public Resource cargar(String rutaRelativa) {
        return new FileSystemResource(resolver(rutaRelativa));
    }

    @Override
    public boolean existe(String rutaRelativa) {
        return rutaRelativa != null && Files.isRegularFile(resolver(rutaRelativa));
    }

    private Path resolver(String rutaRelativa) {
        Path destino = raiz.resolve(rutaRelativa).normalize();
        if (!destino.startsWith(raiz)) {
            throw ApiException.solicitudInvalida("Ruta de archivo no permitida");
        }
        return destino;
    }

    private String extension(String nombre) {
        if (nombre == null || !nombre.contains(".")) return "";
        String ext = nombre.substring(nombre.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        return ext.matches("\\.[a-z0-9]{1,6}") ? ext : "";
    }
}
