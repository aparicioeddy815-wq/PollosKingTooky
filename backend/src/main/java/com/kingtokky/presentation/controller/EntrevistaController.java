package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.EntrevistaDtos.AgendaEntrevistasResponse;
import com.kingtokky.application.dto.EntrevistaDtos.AgendarEntrevistaRequest;
import com.kingtokky.application.dto.EntrevistaDtos.ContratarRequest;
import com.kingtokky.application.dto.EntrevistaDtos.DescartarRequest;
import com.kingtokky.application.dto.EntrevistaDtos.DocumentoResponse;
import com.kingtokky.application.dto.EntrevistaDtos.EntrevistaResponse;
import com.kingtokky.application.dto.EntrevistaDtos.ObservacionesRequest;
import com.kingtokky.application.dto.EntrevistaDtos.PostulanteRequest;
import com.kingtokky.application.dto.EntrevistaDtos.PostulanteResponse;
import com.kingtokky.application.dto.EntrevistaDtos.ResumenEntrevistadosResponse;
import com.kingtokky.application.dto.EntrevistaDtos.ResumenEntrevistasResponse;
import com.kingtokky.application.dto.EntrevistaDtos.VacanteResponse;
import com.kingtokky.application.service.EntrevistaService;
import com.kingtokky.application.service.ExportacionService;
import com.kingtokky.domain.model.entrevista.EstadoPostulante;
import com.kingtokky.domain.model.entrevista.TipoDocumento;
import com.kingtokky.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@Tag(name = "Entrevistas y selección", description = "Agenda del gerente y decisión final del administrador")
public class EntrevistaController {

    private final EntrevistaService servicio;
    private final ExportacionService exportacion;

    public EntrevistaController(EntrevistaService servicio, ExportacionService exportacion) {
        this.servicio = servicio;
        this.exportacion = exportacion;
    }

    // ------------------------------------------------------------------ gerente
    @GetMapping("/entrevistas/agenda")
    @PreAuthorize("hasAuthority('ENTREVISTA_GESTIONAR')")
    @Operation(summary = "Agenda: entrevistas de hoy con cuenta regresiva, próximas citas y tarjetas de resumen")
    public AgendaEntrevistasResponse agenda() {
        return servicio.agenda();
    }

    @GetMapping("/entrevistas/resumen")
    @PreAuthorize("hasAuthority('ENTREVISTA_GESTIONAR')")
    public ResumenEntrevistasResponse resumen() {
        return servicio.resumen();
    }

    @GetMapping("/entrevistas/vacantes")
    @PreAuthorize("hasAnyAuthority('ENTREVISTA_GESTIONAR','ENTREVISTA_DECIDIR')")
    public List<VacanteResponse> vacantes() {
        return servicio.vacantesAbiertas();
    }

    @PostMapping("/entrevistas")
    @PreAuthorize("hasAuthority('ENTREVISTA_GESTIONAR')")
    @Operation(summary = "Agenda una entrevista (presencial, virtual o telefónica)")
    public ResponseEntity<EntrevistaResponse> agendar(@Valid @RequestBody AgendarEntrevistaRequest req, @AuthenticationPrincipal UsuarioAutenticado u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.agendar(req, u.idUsuario()));
    }

    @PutMapping("/entrevistas/{id}/observaciones")
    @PreAuthorize("hasAuthority('ENTREVISTA_GESTIONAR')")
    @Operation(summary = "Guarda observaciones; «realizada» cierra la entrevista y «aprobar» da el visto bueno gerencial")
    public EntrevistaResponse observaciones(@PathVariable Long id, @Valid @RequestBody ObservacionesRequest req) {
        return servicio.registrarObservaciones(id, req);
    }

    @GetMapping("/postulantes")
    @PreAuthorize("hasAnyAuthority('ENTREVISTA_GESTIONAR','ENTREVISTA_DECIDIR')")
    public List<PostulanteResponse> postulantes(@RequestParam(required = false) Long idVacante, @RequestParam(required = false) EstadoPostulante estado) {
        return servicio.listarPostulantes(idVacante, estado);
    }

    @GetMapping("/postulantes/{id}")
    @PreAuthorize("hasAnyAuthority('ENTREVISTA_GESTIONAR','ENTREVISTA_DECIDIR')")
    public PostulanteResponse postulante(@PathVariable Long id) {
        return servicio.obtenerPostulante(id);
    }

    @PostMapping("/postulantes")
    @PreAuthorize("hasAuthority('ENTREVISTA_GESTIONAR')")
    public ResponseEntity<PostulanteResponse> registrar(@Valid @RequestBody PostulanteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrarPostulante(req));
    }

    @PostMapping(value = "/postulantes/{id}/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ENTREVISTA_GESTIONAR')")
    @Operation(summary = "Sube un documento del postulante: CV, CARNET o ANTECEDENTES (PDF/PNG/JPG, máximo 5 MB; campos «tipo» y «archivo»)")
    public ResponseEntity<DocumentoResponse> subirDocumento(@PathVariable Long id, @RequestParam TipoDocumento tipo,
                                                            @RequestPart("archivo") MultipartFile archivo, @AuthenticationPrincipal UsuarioAutenticado u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.subirDocumento(id, tipo, archivo, u.idUsuario()));
    }

    @GetMapping("/postulantes/documentos/{idDocumento}/descargar")
    @PreAuthorize("hasAnyAuthority('ENTREVISTA_GESTIONAR','ENTREVISTA_DECIDIR')")
    public ResponseEntity<org.springframework.core.io.Resource> documento(@PathVariable Long idDocumento) {
        return Descargas.de(servicio.descargarDocumento(idDocumento));
    }

    @GetMapping("/postulantes/{id}/cv")
    @PreAuthorize("hasAnyAuthority('ENTREVISTA_GESTIONAR','ENTREVISTA_DECIDIR')")
    @Operation(summary = "Descarga el CV del postulante")
    public ResponseEntity<org.springframework.core.io.Resource> cv(@PathVariable Long id) {
        return Descargas.de(servicio.descargarCv(id));
    }

    // ------------------------------------------------------------------ administrador
    @GetMapping("/entrevistados/resumen")
    @PreAuthorize("hasAuthority('ENTREVISTA_DECIDIR')")
    @Operation(summary = "Tarjetas del administrador: postulantes del ciclo, visto bueno gerencial y urgencia operativa")
    public ResumenEntrevistadosResponse resumenEntrevistados() {
        return servicio.resumenEntrevistados();
    }

    @PostMapping("/entrevistados/{id}/contratar")
    @PreAuthorize("hasAuthority('ENTREVISTA_DECIDIR')")
    @Operation(summary = "Aprobación definitiva: crea al empleado. Exige visto bueno del gerente y documentos completos")
    public PostulanteResponse contratar(@PathVariable Long id, @Valid @RequestBody ContratarRequest req, @AuthenticationPrincipal UsuarioAutenticado u) {
        return servicio.contratar(id, req, u.idUsuario());
    }

    @PostMapping("/entrevistados/{id}/descartar")
    @PreAuthorize("hasAuthority('ENTREVISTA_DECIDIR')")
    public PostulanteResponse descartar(@PathVariable Long id, @RequestBody(required = false) DescartarRequest req, @AuthenticationPrincipal UsuarioAutenticado u) {
        return servicio.descartar(id, req == null ? null : req.motivo(), u.idUsuario());
    }

    @GetMapping("/entrevistados/exportar")
    @PreAuthorize("hasAuthority('ENTREVISTA_DECIDIR')")
    @Operation(summary = "Informe de selección de personal en csv, xlsx o pdf")
    public ResponseEntity<org.springframework.core.io.Resource> informe(@RequestParam(defaultValue = "xlsx") String formato) {
        List<List<Object>> filas = servicio.listarPostulantes(null, null).stream().map(p -> List.<Object>of(p.nombreCompleto(), p.cargo(), p.urgencia().name(),
                p.estado().name(), p.documentosCompletos() ? "Sí" : "Faltan " + p.documentosFaltantes(),
                p.fechaEntrevista() == null ? "" : p.fechaEntrevista().toString().replace('T', ' '), p.evaluacionGerente() == null ? "" : p.evaluacionGerente())).toList();
        return Descargas.de(exportacion.exportar(formato, "informe-de-seleccion", "Informe de selección de personal",
                List.of("Postulante", "Cargo", "Urgencia", "Estado", "Documentos", "Entrevista", "Evaluación del gerente"), filas));
    }
}
