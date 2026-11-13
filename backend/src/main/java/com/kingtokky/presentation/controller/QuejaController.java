package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.ClienteDtos.QuejaResponse;
import com.kingtokky.application.dto.QuejaGestionDtos.ResponderQuejaRequest;
import com.kingtokky.application.dto.QuejaGestionDtos.ResumenQuejasResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.application.service.ExportacionService;
import com.kingtokky.application.service.QuejaGestionService;
import com.kingtokky.domain.model.cliente.TipoQueja;
import com.kingtokky.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quejas")
@PreAuthorize("hasAuthority('QUEJA_GESTIONAR')")
@Tag(name = "Quejas y sugerencias (gerente)", description = "Bandeja de mensajes de los clientes")
public class QuejaController {

    private final QuejaGestionService quejas;
    private final ExportacionService exportacion;

    public QuejaController(QuejaGestionService quejas, ExportacionService exportacion) {
        this.quejas = quejas;
        this.exportacion = exportacion;
    }

    @GetMapping
    @Operation(summary = "Mensajes de clientes filtrados por tipo (RECLAMO, SUGERENCIA, FELICITACION) y rango de fechas")
    public PaginaResponse<QuejaResponse> listar(@RequestParam(required = false) TipoQueja tipo,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                                @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return quejas.listar(tipo, desde, hasta, page, size);
    }

    @GetMapping("/resumen")
    @Operation(summary = "Recibidas del mes, tiempo promedio de despacho vs. objetivo y satisfacción en sala")
    public ResumenQuejasResponse resumen() {
        return quejas.resumen();
    }

    @PatchMapping("/{id}/responder")
    public QuejaResponse responder(@PathVariable Long id, @Valid @RequestBody ResponderQuejaRequest req, @AuthenticationPrincipal UsuarioAutenticado u) {
        return quejas.responder(id, req.respuesta(), u.idUsuario());
    }

    @PatchMapping("/{id}/revision")
    @Operation(summary = "Marca el mensaje como «en revisión»")
    public QuejaResponse revision(@PathVariable Long id) {
        return quejas.marcarEnRevision(id);
    }

    @GetMapping("/exportar")
    @Operation(summary = "Descarga el informe de quejas y sugerencias en csv, xlsx o pdf")
    public ResponseEntity<org.springframework.core.io.Resource> exportar(@RequestParam(defaultValue = "csv") String formato,
                                                                         @RequestParam(required = false) TipoQueja tipo,
                                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<List<Object>> filas = quejas.listarParaExportar(tipo, desde, hasta).stream().map(q -> List.<Object>of(
                q.fechaCreacion().toString().replace('T', ' ').substring(0, 16), q.cliente(), q.tipo().name(), q.categoria().name(),
                q.numeroPedido() == null ? "" : q.numeroPedido(), q.valoracion() == null ? "" : q.valoracion(), q.mensaje(),
                q.estado().name(), q.respuesta() == null ? "" : q.respuesta())).toList();
        return Descargas.de(exportacion.exportar(formato, "quejas-y-sugerencias", "Quejas y sugerencias", List.of("Fecha", "Cliente", "Tipo",
                "Categoría", "Pedido", "Valoración", "Mensaje", "Estado", "Respuesta"), filas));
    }
}
