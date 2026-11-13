package com.kingtokky.presentation.controller;

import com.kingtokky.application.dto.InventarioDtos.AlertaResponse;
import com.kingtokky.application.dto.InventarioDtos.CategoriaInsumoResponse;
import com.kingtokky.application.dto.InventarioDtos.ConteoRequest;
import com.kingtokky.application.dto.InventarioDtos.ControlDetalleResponse;
import com.kingtokky.application.dto.InventarioDtos.ControlResponse;
import com.kingtokky.application.dto.InventarioDtos.CrearControlRequest;
import com.kingtokky.application.dto.InventarioDtos.EntradaRequest;
import com.kingtokky.application.dto.InventarioDtos.InsumoResponse;
import com.kingtokky.application.dto.InventarioDtos.MermaRequest;
import com.kingtokky.application.dto.InventarioDtos.MermaResponse;
import com.kingtokky.application.dto.InventarioDtos.MovimientoResponse;
import com.kingtokky.application.dto.InventarioDtos.ProgramacionControlesResponse;
import com.kingtokky.application.dto.InventarioDtos.ProveedorResponse;
import com.kingtokky.application.dto.InventarioDtos.ResumenInventarioResponse;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.application.service.ControlInventarioService;
import com.kingtokky.application.service.ExportacionService;
import com.kingtokky.application.service.InventarioService;
import com.kingtokky.domain.model.inventario.TipoInsumo;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Existencias, entradas, mermas, kardex, alertas, proyección y controles físicos")
public class InventarioController {

    private final InventarioService inventario;
    private final ControlInventarioService controles;
    private final ExportacionService exportacion;

    public InventarioController(InventarioService inventario, ControlInventarioService controles, ExportacionService exportacion) {
        this.inventario = inventario;
        this.controles = controles;
        this.exportacion = exportacion;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    @Operation(summary = "Insumos con estado (CRITICO/BAJO/NORMAL), consumo de hoy y proyección de consumo vs. existencias")
    public List<InsumoResponse> listar(@RequestParam(required = false) String q, @RequestParam(required = false) Long categoria,
                                       @RequestParam(defaultValue = "ALIMENTO") TipoInsumo tipo, @RequestParam(required = false) String estado) {
        return inventario.listar(q, categoria, tipo, estado);
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    @Operation(summary = "Tarjetas: alertas urgentes, abastecimiento, valoración del inventario y artículos a reponer")
    public ResumenInventarioResponse resumen() {
        return inventario.resumen();
    }

    @GetMapping("/categorias")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    public List<CategoriaInsumoResponse> categorias(@RequestParam(defaultValue = "ALIMENTO") TipoInsumo tipo) {
        return inventario.listarCategorias(tipo);
    }

    @GetMapping("/proveedores")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    public List<ProveedorResponse> proveedores() {
        return inventario.listarProveedores();
    }

    @GetMapping("/alertas")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    @Operation(summary = "Alertas de stock abiertas")
    public List<AlertaResponse> alertas() {
        return inventario.alertasAbiertas();
    }

    @GetMapping("/movimientos")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    @Operation(summary = "Kardex: entradas, consumos, mermas y ajustes (de un insumo o de todos)")
    public PaginaResponse<MovimientoResponse> movimientos(@RequestParam(required = false) Long idInsumo, @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return inventario.kardex(idInsumo, page, size);
    }

    @PostMapping("/entradas")
    @PreAuthorize("hasAuthority('INVENTARIO_ESCRIBIR')")
    @Operation(summary = "Registra una entrada de mercadería")
    public InsumoResponse entrada(@Valid @RequestBody EntradaRequest req, @AuthenticationPrincipal UsuarioAutenticado u) {
        return inventario.registrarEntrada(req, u.idUsuario());
    }

    @GetMapping("/mermas")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    @Operation(summary = "Historial de mermas (últimos 30 días por defecto)")
    public PaginaResponse<MermaResponse> mermas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                                                @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return inventario.historialMermas(desde, hasta, page, size);
    }

    @PostMapping("/mermas")
    @PreAuthorize("hasAuthority('INVENTARIO_ESCRIBIR')")
    @Operation(summary = "Registra una merma (descuenta stock, la valora y la deja en el kardex)")
    public ResponseEntity<MermaResponse> merma(@Valid @RequestBody MermaRequest req, @AuthenticationPrincipal UsuarioAutenticado u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventario.registrarMerma(req, u.idUsuario()));
    }

    @GetMapping("/exportar")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    @Operation(summary = "Descarga el inventario en csv, xlsx o pdf")
    public ResponseEntity<org.springframework.core.io.Resource> exportar(@RequestParam(defaultValue = "xlsx") String formato,
                                                                         @RequestParam(defaultValue = "ALIMENTO") TipoInsumo tipo) {
        List<List<Object>> filas = inventario.listar(null, null, tipo, null).stream().map(i -> List.<Object>of(i.codigo(), i.nombre(), i.categoria(),
                i.unidadMedida(), i.stockActual(), i.umbralMinimo(), i.estado(), i.consumoPromedioDiario(), i.deficitProyectado(), i.valorStock())).toList();
        return Descargas.de(exportacion.exportar(formato, "inventario", "Inventario", List.of("Código", "Insumo", "Categoría", "Unidad", "Stock",
                "Mínimo", "Estado", "Consumo prom./día", "Déficit proyectado", "Valor (Bs)"), filas));
    }

    @GetMapping("/mermas/exportar")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    public ResponseEntity<org.springframework.core.io.Resource> exportarMermas(@RequestParam(defaultValue = "csv") String formato,
                                                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        List<List<Object>> filas = inventario.mermasParaExportar(desde, hasta).stream().map(m -> List.<Object>of(m.fechaHora().toString().replace('T', ' '),
                m.insumo(), m.cantidad(), m.unidadMedida(), m.motivo().name(), m.costoTotal(), m.registradoPor())).toList();
        return Descargas.de(exportacion.exportar(formato, "mermas", "Mermas", List.of("Fecha", "Insumo", "Cantidad", "Unidad", "Motivo", "Costo (Bs)", "Registrado por"), filas));
    }

    // ------------------------------------------------------------------ controles físicos

    @GetMapping("/controles")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    public List<ControlResponse> listarControles(@RequestParam(defaultValue = "20") int limite) {
        return controles.listar(limite);
    }

    @GetMapping("/controles/programacion")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    @Operation(summary = "Próximo control semanal (sábado) y quincenal (días 15 y 30)")
    public ProgramacionControlesResponse programacion() {
        return controles.programacion();
    }

    @GetMapping("/controles/{id}")
    @PreAuthorize("hasAuthority('INVENTARIO_LEER')")
    public ControlDetalleResponse control(@PathVariable Long id) {
        return controles.obtener(id);
    }

    @PostMapping("/controles")
    @PreAuthorize("hasAuthority('INVENTARIO_ESCRIBIR')")
    @Operation(summary = "Abre un control semanal (alimentos) o quincenal (utensilios, mesas y vasos)")
    public ResponseEntity<ControlDetalleResponse> crearControl(@Valid @RequestBody CrearControlRequest req, @AuthenticationPrincipal UsuarioAutenticado u) {
        return ResponseEntity.status(HttpStatus.CREATED).body(controles.crear(req.tipo(), u.idUsuario()));
    }

    @PutMapping("/controles/{id}/conteo")
    @PreAuthorize("hasAuthority('INVENTARIO_ESCRIBIR')")
    @Operation(summary = "Guarda lo contado físicamente; calcula la diferencia contra el sistema")
    public ControlDetalleResponse conteo(@PathVariable Long id, @Valid @RequestBody ConteoRequest req) {
        return controles.registrarConteo(id, req);
    }

    @PostMapping("/controles/{id}/cerrar")
    @PreAuthorize("hasAuthority('INVENTARIO_ESCRIBIR')")
    @Operation(summary = "Cierra el control y aplica las diferencias como ajustes de inventario")
    public ControlDetalleResponse cerrar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado u) {
        return controles.cerrar(id, u.idUsuario());
    }
}
