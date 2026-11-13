package com.kingtokky.presentation.controller;

import com.kingtokky.application.evento.EventoOperativo;
import com.kingtokky.infrastructure.sse.GestorEventos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/stream")
@Tag(name = "Tiempo real", description = "Eventos SSE para las pantallas de despacho")
public class EventosController {

    private final GestorEventos eventos;

    public EventosController(GestorEventos eventos) {
        this.eventos = eventos;
    }

    @GetMapping(value = "/comandas", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('PEDIDO_LEER')")
    @Operation(summary = "SSE de comandas: pedido-creado, pedido-listo, pedido-actualizado y timbre. "
            + "EventSource no permite cabeceras, así que el token va en ?access_token=")
    public SseEmitter comandas() {
        return eventos.suscribir(EventoOperativo.CANAL_COMANDAS);
    }
}
