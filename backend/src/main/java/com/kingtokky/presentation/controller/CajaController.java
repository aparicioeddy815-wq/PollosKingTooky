package com.kingtokky.presentation.controller;

import com.kingtokky.application.service.CajaService;
import com.kingtokky.domain.model.reporte.CierreCaja;
import com.kingtokky.domain.model.reporte.TipoCierre;
import com.kingtokky.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caja/cierres")
@PreAuthorize("hasAuthority('CAJA_CIERRE')")
@Tag(name = "Caja", description = "Cierres de caja intermedios y finales")
public class CajaController {

    public record CierreRequest(@NotNull TipoCierre tipo, @Size(max = 300) String observacion) {}

    public record CierreResponse(Long idCierre, LocalDateTime fechaHora, TipoCierre tipo, BigDecimal totalVentas, int totalPedidos, String observacion) {}

    private final CajaService caja;

    public CajaController(CajaService caja) {
        this.caja = caja;
    }

    @GetMapping
    @Operation(summary = "Cierres de caja de un día (hoy por defecto)")
    public List<CierreResponse> listar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return caja.delDia(fecha).stream().map(this::aRespuesta).toList();
    }

    @PostMapping
    @Operation(summary = "Registra un cierre de caja con las ventas acumuladas del día")
    public ResponseEntity<CierreResponse> registrar(@Valid @RequestBody CierreRequest req, @AuthenticationPrincipal UsuarioAutenticado u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(aRespuesta(caja.registrar(req.tipo(), req.observacion(), u.idUsuario())));
    }

    private CierreResponse aRespuesta(CierreCaja c) {
        return new CierreResponse(c.getId(), c.getFechaHora(), c.getTipo(), c.getTotalVentas(), c.getTotalPedidos(), c.getObservacion());
    }
}
