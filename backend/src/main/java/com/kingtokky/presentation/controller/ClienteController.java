package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.ClienteDtos.AyudaResponse;
import com.kingtokky.application.dto.ClienteDtos.CategoriaSalon;
import com.kingtokky.application.dto.ClienteDtos.EvaluacionRequest;
import com.kingtokky.application.dto.ClienteDtos.EvaluacionResponse;
import com.kingtokky.application.dto.ClienteDtos.MeGustaResponse;
import com.kingtokky.application.dto.ClienteDtos.ProductoSalon;
import com.kingtokky.application.dto.ClienteDtos.QuejaRequest;
import com.kingtokky.application.dto.ClienteDtos.QuejaResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.application.service.ClienteService;
import com.kingtokky.infrastructure.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cliente")
@PreAuthorize("hasAuthority('CLIENTE_PORTAL')")
@Tag(name = "Portal del cliente", description = "Menú del salón, favoritos, quejas y sugerencias, evaluación diaria y ayuda")
public class ClienteController {

    private final ClienteService cliente;

    public ClienteController(ClienteService cliente) {
        this.cliente = cliente;
    }

    @GetMapping("/menu-salon")
    @Operation(summary = "Menú del salón: lo de PedidosYA más platos exclusivos, con corazón y veces consumidas")
    public List<CategoriaSalon> menuSalon(@AuthenticationPrincipal UsuarioAutenticado u) {
        return cliente.menuSalon(u.idUsuario());
    }

    @GetMapping("/favoritos")
    public List<ProductoSalon> favoritos(@AuthenticationPrincipal UsuarioAutenticado u) {
        return cliente.misFavoritos(u.idUsuario());
    }

    @PostMapping("/favoritos/{idProducto}/me-gusta")
    @Operation(summary = "Alterna el corazón de un producto")
    public MeGustaResponse meGusta(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable Long idProducto) {
        return cliente.alternarMeGusta(u.idUsuario(), idProducto);
    }

    @PostMapping("/quejas")
    @Operation(summary = "Envía una sugerencia, queja o felicitación (con número de pedido y valoración opcionales)")
    public ResponseEntity<QuejaResponse> crearQueja(@AuthenticationPrincipal UsuarioAutenticado u, @Valid @RequestBody QuejaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente.crearQueja(u.idUsuario(), req));
    }

    @GetMapping("/quejas")
    public PaginaResponse<QuejaResponse> misQuejas(@AuthenticationPrincipal UsuarioAutenticado u, @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return cliente.misQuejas(u.idUsuario(), page, size);
    }

    @PostMapping("/evaluacion")
    @Operation(summary = "Califica el día de 1 a 5 estrellas (una vez por día; se puede corregir)")
    public EvaluacionResponse evaluar(@AuthenticationPrincipal UsuarioAutenticado u, @Valid @RequestBody EvaluacionRequest req) {
        return cliente.evaluar(u.idUsuario(), req);
    }

    @GetMapping("/evaluacion/hoy")
    @Operation(summary = "Mi evaluación de hoy, o vacío si todavía no califiqué")
    public EvaluacionResponse evaluacionDeHoy(@AuthenticationPrincipal UsuarioAutenticado u) {
        return cliente.evaluacionDeHoy(u.idUsuario());
    }

    @GetMapping("/ayuda")
    @Operation(summary = "Pasos para reservar y contacto de WhatsApp corporativo")
    public AyudaResponse ayuda() {
        return cliente.ayuda();
    }
}
