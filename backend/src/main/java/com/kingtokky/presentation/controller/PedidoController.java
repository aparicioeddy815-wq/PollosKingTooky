package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.PedidoDtos.CancelarPedidoRequest;
import com.kingtokky.application.dto.PedidoDtos.ComandasResponse;
import com.kingtokky.application.dto.PedidoDtos.PedidoRequest;
import com.kingtokky.application.dto.PedidoDtos.PedidoResponse;
import com.kingtokky.application.service.PedidoService;
import com.kingtokky.domain.model.pedido.EstadoPedido;
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
@RequestMapping("/api/pedidos")
@Tag(name = "Despacho y comandas", description = "Pedidos con número de turno diario, estados y avisos")
public class PedidoController {

    private final PedidoService pedidos;

    public PedidoController(PedidoService pedidos) {
        this.pedidos = pedidos;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PEDIDO_LEER')")
    @Operation(summary = "Pedidos de una fecha (hoy por defecto), opcionalmente filtrados por estado")
    public List<PedidoResponse> listar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                       @RequestParam(required = false) EstadoPedido estado) {
        return pedidos.listar(fecha, estado);
    }

    @GetMapping("/comandas")
    @PreAuthorize("hasAuthority('PEDIDO_LEER')")
    @Operation(summary = "Tablero de despacho: comandas en preparación y listas, con tiempo promedio de despacho")
    public ComandasResponse comandas() {
        return pedidos.comandas();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PEDIDO_LEER')")
    public PedidoResponse obtener(@PathVariable Long id) {
        return pedidos.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PEDIDO_CREAR')")
    @Operation(summary = "Registra un pedido; asigna el siguiente número de turno del día y descuenta insumos")
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest req, @AuthenticationPrincipal UsuarioAutenticado u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidos.crear(req, u.idUsuario()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('PEDIDO_ESTADO')")
    @Operation(summary = "Cambia el estado respetando el ciclo EN_PREPARACION → LISTO → ENTREGADO")
    public PedidoResponse cambiarEstado(@PathVariable Long id, @RequestParam EstadoPedido estado) {
        return pedidos.cambiarEstado(id, estado);
    }

    @PostMapping("/{id}/listo")
    @PreAuthorize("hasAuthority('PEDIDO_ESTADO')")
    @Operation(summary = "Marca el pedido como listo (dispara el aviso sonoro en las pantallas conectadas)")
    public PedidoResponse listo(@PathVariable Long id) {
        return pedidos.cambiarEstado(id, EstadoPedido.LISTO);
    }

    @PostMapping("/{id}/entregar")
    @PreAuthorize("hasAuthority('PEDIDO_ESTADO')")
    public PedidoResponse entregar(@PathVariable Long id) {
        return pedidos.cambiarEstado(id, EstadoPedido.ENTREGADO);
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('PEDIDO_ESTADO')")
    @Operation(summary = "Anula una comanda en preparación (queda registrada en la bitácora)")
    public PedidoResponse cancelar(@PathVariable Long id, @RequestBody(required = false) CancelarPedidoRequest req) {
        return pedidos.cancelar(id, req == null ? null : req.motivo());
    }

    @PostMapping("/{id}/timbre")
    @PreAuthorize("hasAuthority('PEDIDO_ESTADO')")
    @Operation(summary = "Toca el timbre para llamar al cliente de un pedido listo")
    public PedidoResponse timbre(@PathVariable Long id) {
        return pedidos.timbre(id);
    }
}
