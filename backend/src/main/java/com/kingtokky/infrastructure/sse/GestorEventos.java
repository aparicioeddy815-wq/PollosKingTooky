package com.kingtokky.infrastructure.sse;

import com.kingtokky.application.evento.EventoOperativo;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** Mantiene las conexiones SSE abiertas y reparte los eventos una vez confirmada la transacción. */
@Component
public class GestorEventos {

    private static final Logger log = LoggerFactory.getLogger(GestorEventos.class);

    private final Map<String, List<SseEmitter>> emisores = new ConcurrentHashMap<>();

    public SseEmitter suscribir(String canal) {
        List<SseEmitter> lista = emisores.computeIfAbsent(canal, c -> new CopyOnWriteArrayList<>());
        SseEmitter emisor = new SseEmitter(0L);
        lista.add(emisor);
        emisor.onCompletion(() -> lista.remove(emisor));
        emisor.onTimeout(() -> lista.remove(emisor));
        emisor.onError(e -> lista.remove(emisor));
        try {
            emisor.send(SseEmitter.event().name("conectado").data("ok"));
        } catch (IOException e) {
            lista.remove(emisor);
        }
        return emisor;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void alOcurrirEvento(EventoOperativo evento) {
        List<SseEmitter> lista = emisores.getOrDefault(evento.canal(), List.of());
        for (SseEmitter emisor : lista) {
            try {
                emisor.send(SseEmitter.event().name(evento.nombre()).data(evento.datos()));
            } catch (IOException | IllegalStateException e) {
                lista.remove(emisor);
            }
        }
        log.debug("Evento {} enviado a {} clientes del canal {}", evento.nombre(), lista.size(), evento.canal());
    }

    /** Latido para que proxies y navegadores no cierren conexiones inactivas. */
    @Scheduled(fixedRate = 20000)
    public void latido() {
        emisores.values().forEach(lista -> {
            for (SseEmitter emisor : lista) {
                try {
                    emisor.send(SseEmitter.event().comment("latido"));
                } catch (IOException | IllegalStateException e) {
                    lista.remove(emisor);
                }
            }
        });
    }
}
