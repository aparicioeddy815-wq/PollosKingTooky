package com.kingtokky.domain.port;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.Resource;

/** Puerto de salida para guardar y recuperar archivos (fotos de perfil, CV, documentos). */
public interface AlmacenArchivos {

    /** Guarda el contenido y devuelve la ruta relativa dentro del almacén. */
    String guardar(String subcarpeta, String nombreOriginal, InputStream contenido) throws IOException;

    Resource cargar(String rutaRelativa);

    boolean existe(String rutaRelativa);
}
