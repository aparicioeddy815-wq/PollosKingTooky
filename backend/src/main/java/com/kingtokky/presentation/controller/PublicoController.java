package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.PublicoDtos.Horario;
import com.kingtokky.application.dto.PublicoDtos.InicioPublico;
import com.kingtokky.application.dto.PublicoDtos.MenuPublico;
import com.kingtokky.application.dto.PublicoDtos.SobreNosotros;
import com.kingtokky.application.dto.PublicoDtos.TurnosPublico;
import com.kingtokky.application.dto.PublicoDtos.Ubicacion;
import com.kingtokky.application.evento.EventoOperativo;
import com.kingtokky.application.service.PublicoService;
import com.kingtokky.infrastructure.sse.GestorEventos;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/publico")
@Tag(name = "Sitio público", description = "Información visible sin iniciar sesión")
public class PublicoController {

    private final PublicoService publico;
    private final GestorEventos eventos;

    public PublicoController(PublicoService publico, GestorEventos eventos) {
        this.publico = publico;
        this.eventos = eventos;
    }

    @GetMapping("/inicio")
    @Operation(summary = "Portada: producto más vendido, horarios y ubicación")
    public InicioPublico inicio() {
        return publico.inicio();
    }

    @GetMapping("/menu")
    @Operation(summary = "Menú de PedidosYA por categorías, indicando qué está disponible hoy")
    public MenuPublico menu() {
        return publico.menu();
    }

    @GetMapping("/horarios")
    public List<Horario> horarios() {
        return publico.listarHorarios();
    }

    @GetMapping("/ubicacion")
    public Ubicacion ubicacion() {
        return publico.ubicacion();
    }

    @GetMapping("/sobre-nosotros")
    public SobreNosotros sobreNosotros() {
        return publico.sobreNosotros();
    }

    @GetMapping("/turnos")
    @Operation(summary = "Números de turno en preparación y listos")
    public TurnosPublico turnos() {
        return publico.turnos();
    }

    @GetMapping(value = "/eventos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Flujo en tiempo real (SSE): turnos-actualizados, timbre y menu-actualizado")
    public SseEmitter eventos() {
        return eventos.suscribir(EventoOperativo.CANAL_PUBLICO);
    }
}
