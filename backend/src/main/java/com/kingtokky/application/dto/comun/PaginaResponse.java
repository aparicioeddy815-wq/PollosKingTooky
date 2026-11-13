package com.kingtokky.application.dto.comun;

import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

/** Página de resultados; coincide con la interfaz Pagina<T> del frontend Angular. */
public record PaginaResponse<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static <E, T> PaginaResponse<T> de(Page<E> page, Function<E, T> conversor) {
        return new PaginaResponse<>(page.getContent().stream().map(conversor).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
