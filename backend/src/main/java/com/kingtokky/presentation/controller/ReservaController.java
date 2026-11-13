package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.ReservaDtos.DiaReserva;
import com.kingtokky.application.dto.ReservaDtos.HoraReserva;
import com.kingtokky.application.dto.ReservaDtos.ReprogramarRequest;
import com.kingtokky.application.dto.ReservaDtos.ReservaRequest;
import com.kingtokky.application.dto.ReservaDtos.ReservaResponse;
import com.kingtokky.application.dto.ReservaDtos.ResumenReservasResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.application.service.ReservaService;
import com.kingtokky.domain.model.reserva.EstadoReserva;
import com.kingtokky.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Reserva en 3 pasos (fecha, hora, platos); el pago se registra en caja")
public class ReservaController {

    private final ReservaService reservas;

    public ReservaController(ReservaService reservas) {
        this.reservas = reservas;
    }

    // ---- asistente del cliente
    @GetMapping("/dias")
    @PreAuthorize("hasAuthority('RESERVA_CREAR')")
    @Operation(summary = "Paso 1: días de la semana disponibles para reservar")
    public List<DiaReserva> dias() {
        return reservas.diasDisponibles();
    }

    @GetMapping("/horarios")
    @PreAuthorize("hasAuthority('RESERVA_CREAR')")
    @Operation(summary = "Paso 2: horas del día elegido con su cupo restante")
    public List<HoraReserva> horarios(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return reservas.horariosDelDia(fecha);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('RESERVA_CREAR')")
    @Operation(summary = "Paso 3: confirma la reserva con los platos elegidos (queda pendiente de pago en caja)")
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody ReservaRequest req, @AuthenticationPrincipal UsuarioAutenticado u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservas.crear(req, u.idUsuario()));
    }

    @GetMapping("/mis")
    @PreAuthorize("hasAuthority('RESERVA_CREAR')")
    @Operation(summary = "Mis reservas, con filtro opcional por estado")
    public PaginaResponse<ReservaResponse> mis(@AuthenticationPrincipal UsuarioAutenticado u, @RequestParam(required = false) EstadoReserva estado,
                                               @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return reservas.misReservas(u.idUsuario(), estado, page, size);
    }

    @GetMapping("/mis/resumen")
    @PreAuthorize("hasAuthority('RESERVA_CREAR')")
    @Operation(summary = "Resumen para el inicio del cliente: total de reservas, próxima visita y aviso del día")
    public ResumenReservasResponse resumen(@AuthenticationPrincipal UsuarioAutenticado u) {
        return reservas.resumen(u.idUsuario());
    }

    @PatchMapping("/{id}/reprogramar")
    @PreAuthorize("hasAuthority('RESERVA_CREAR') or hasAuthority('RESERVA_GESTIONAR')")
    @Operation(summary = "Cambia fecha y hora (solo reservas pendientes de pago, con al menos 60 min de anticipación)")
    public ReservaResponse reprogramar(@PathVariable Long id, @Valid @RequestBody ReprogramarRequest req, @AuthenticationPrincipal UsuarioAutenticado u) {
        return reservas.reprogramar(id, req, u);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('RESERVA_CREAR') or hasAuthority('RESERVA_GESTIONAR')")
    public ReservaResponse cancelar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado u) {
        return reservas.cancelar(id, u);
    }

    @GetMapping("/{id}/recibo")
    @PreAuthorize("hasAuthority('RESERVA_CREAR') or hasAuthority('RESERVA_GESTIONAR')")
    @Operation(summary = "Descarga el recibo de la reserva en PDF")
    public ResponseEntity<org.springframework.core.io.Resource> recibo(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado u) {
        return Descargas.de("recibo-reserva-" + id + ".pdf", "application/pdf", reservas.recibo(id, u));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('RESERVA_CREAR') or hasAuthority('RESERVA_GESTIONAR')")
    public ReservaResponse obtener(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado u) {
        return reservas.obtener(id, u);
    }

    // ---- gestión del personal
    @GetMapping
    @PreAuthorize("hasAuthority('RESERVA_GESTIONAR')")
    @Operation(summary = "Todas las reservas del local, filtradas por estado y fecha")
    public PaginaResponse<ReservaResponse> gestion(@RequestParam(required = false) EstadoReserva estado,
                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                                   @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return reservas.gestion(estado, fecha, page, size);
    }

    @PatchMapping("/{id}/pagar")
    @PreAuthorize("hasAuthority('RESERVA_GESTIONAR')")
    @Operation(summary = "Registra el pago en caja y envía los platos a cocina como comanda")
    public ReservaResponse pagar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado u) {
        return reservas.pagar(id, u.idUsuario());
    }

    @PatchMapping("/{id}/completar")
    @PreAuthorize("hasAuthority('RESERVA_GESTIONAR')")
    public ReservaResponse completar(@PathVariable Long id) {
        return reservas.completar(id);
    }
}
