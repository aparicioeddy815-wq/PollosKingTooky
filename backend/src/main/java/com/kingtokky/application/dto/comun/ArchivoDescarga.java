package com.kingtokky.application.dto.comun;

import org.springframework.core.io.Resource;

/** Archivo listo para devolver como descarga HTTP. */
public record ArchivoDescarga(String nombre, String tipoContenido, Resource contenido) {}
