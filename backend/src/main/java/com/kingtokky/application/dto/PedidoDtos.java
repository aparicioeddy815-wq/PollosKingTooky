package com.kingtokky.application.dto;

import com.kingtokky.domain.model.pedido.CanalPedido;
import com.kingtokky.domain.model.pedido.EstadoPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class PedidoDtos {

    private PedidoDtos() {}

    public record ItemPedidoRequest(@NotNull Long idProducto, @Min(1) @Max(50) int cantidad, @Size(max = 500) String observaciones) {}

    /** "tipoPedido" conserva el nombre que usa el frontend para el canal de venta. */
    public record PedidoRequest(@NotNull CanalPedido tipoPedido, Long idUsuarioCliente, @NotEmpty @Valid List<ItemPedidoRequest> items) {}

    public record CancelarPedidoRequest(@Size(max = 200) String motivo) {}

    public record DetallePedidoResponse(Long idProducto, String producto, int cantidad, BigDecimal precioUnitario, BigDecimal subtotal, String observaciones) {}

    public record PedidoResponse(Long idPedido, int numeroTurno, LocalDate fechaTurno, EstadoPedido estado, CanalPedido tipoPedido,
                                 BigDecimal total, LocalDateTime fechaHora, LocalDateTime horaPreparacion, LocalDateTime horaListo,
                                 LocalDateTime horaEntrega, long minutosTranscurridos, String cliente, String codigoReserva,
                                 List<DetallePedidoResponse> detalles) {}

    public record ComandasResponse(List<PedidoResponse> enPreparacion, List<PedidoResponse> listos, long pendientes, double tiempoPromedioDespachoMin) {}
}
