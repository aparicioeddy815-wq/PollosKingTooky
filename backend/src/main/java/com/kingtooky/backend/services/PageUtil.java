package com.kingtooky.backend.services;

import com.kingtooky.backend.application.dto.PageResponse;
import org.springframework.data.domain.Page;

import java.util.function.Function;

public final class PageUtil {

    private PageUtil() {
    }

    public static <E, R> PageResponse<R> toResponse(Page<E> page, Function<E, R> mapper) {
        PageResponse<R> r = new PageResponse<>();
        r.setContenido(page.getContent().stream().map(mapper).toList());
        r.setPagina(page.getNumber());
        r.setTamano(page.getSize());
        r.setTotalElementos(page.getTotalElements());
        r.setTotalPaginas(page.getTotalPages());
        return r;
    }

    public static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}