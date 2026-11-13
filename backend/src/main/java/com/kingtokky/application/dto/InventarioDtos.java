package com.kingtokky.application.dto;

import com.kingtokky.domain.model.inventario.MotivoMerma;
import com.kingtokky.domain.model.inventario.TipoControl;
import com.kingtokky.domain.model.inventario.TipoMovimiento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class InventarioDtos {

    private InventarioDtos() {}

    public record InsumoResponse(Long idInsumo, String codigo, String nombre, Long idCategoria, String categoria, String tipo,
                                 String unidadMedida, BigDecimal stockActual, BigDecimal umbralMinimo, BigDecimal consumoHoy,
                                 BigDecimal consumoPromedioDiario, String estado, BigDecimal consumoProyectado,
                                 BigDecimal deficitProyectado, BigDecimal superavitSeguro, BigDecimal costoUnitario,
                                 BigDecimal valorStock, LocalDateTime actualizadoEn) {}

    public record ResumenInventarioResponse(long alertasUrgentes, long insumosBajos, long abastecimientoNecesario,
                                            BigDecimal valoracionInventario, long articulosAReponer, long totalInsumos, int diasProyeccion) {}

    public record EntradaRequest(@NotNull Long idInsumo, @NotNull @DecimalMin("0.001") BigDecimal cantidad, Long idProveedor,
                                 @DecimalMin("0.0") BigDecimal costoUnitario, @Size(max = 60) String documento) {}

    public record MermaRequest(@NotNull Long idInsumo, @NotNull @DecimalMin("0.001") BigDecimal cantidad, @NotNull MotivoMerma motivo,
                               @Size(max = 300) String observacion) {}

    public record MermaResponse(Long idMerma, Long idInsumo, String insumo, String unidadMedida, BigDecimal cantidad, MotivoMerma motivo,
                                String observacion, BigDecimal costoTotal, LocalDateTime fechaHora, String registradoPor) {}

    public record MovimientoResponse(Long idMovimiento, Long idInsumo, String insumo, String unidadMedida, TipoMovimiento tipo,
                                     BigDecimal variacion, BigDecimal stockResultante, LocalDateTime fechaHora, String usuario,
                                     Long idPedido, String proveedor, String documento, String motivo) {}

    public record AlertaResponse(Long idAlerta, Long idInsumo, String insumo, String unidadMedida, BigDecimal stockActual,
                                 BigDecimal umbralMinimo, String estado, LocalDateTime creadaEn, String mensaje) {}

    public record ProveedorResponse(Long idProveedor, String nombre, String telefono, String contacto) {}

    public record CategoriaInsumoResponse(Long idCategoria, String nombre, String tipo) {}

    // ---- controles físicos
    public record CrearControlRequest(@NotNull TipoControl tipo) {}

    public record LineaConteo(@NotNull Long idInsumo, @NotNull @DecimalMin("0.0") BigDecimal stockContado) {}

    public record ConteoRequest(@NotEmpty @Valid List<LineaConteo> lineas) {}

    public record ControlResponse(Long idControl, TipoControl tipo, LocalDate fecha, String estado, String observacion,
                                  long totalLineas, long lineasContadas, LocalDateTime creadoEn, LocalDateTime cerradoEn) {}

    public record LineaControlResponse(Long idDetalle, Long idInsumo, String insumo, String unidadMedida, BigDecimal stockSistema,
                                       BigDecimal stockContado, BigDecimal diferencia, boolean ajustado) {}

    public record ControlDetalleResponse(ControlResponse control, List<LineaControlResponse> lineas) {}

    public record ProgramacionControlesResponse(LocalDate proximoSemanal, LocalDate proximoQuincenal, boolean semanalAbierto, boolean quincenalAbierto) {}
}
