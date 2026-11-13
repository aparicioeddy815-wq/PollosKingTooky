package com.kingtokky.application.dto;

import com.kingtokky.domain.model.cliente.CategoriaQueja;
import com.kingtokky.domain.model.cliente.EstadoQueja;
import com.kingtokky.domain.model.cliente.TipoQueja;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ClienteDtos {

    private ClienteDtos() {}

    public record ProductoSalon(Long idProducto, String nombre, String descripcion, BigDecimal precio, String categoria,
                                String imagenUrl, boolean disponible, boolean meGusta, long vecesConsumidas) {}

    public record CategoriaSalon(String codigo, String nombre, List<ProductoSalon> productos) {}

    public record MeGustaResponse(Long idProducto, boolean meGusta) {}

    public record QuejaRequest(@NotNull TipoQueja tipo, @NotNull CategoriaQueja categoria, Integer numeroPedido,
                               @Min(1) @Max(5) Integer valoracion, @NotBlank @Size(max = 1000) String mensaje) {}

    public record QuejaResponse(Long idQueja, Long idUsuario, String cliente, TipoQueja tipo, CategoriaQueja categoria, Integer numeroPedido,
                                Integer valoracion, String mensaje, EstadoQueja estado, String respuesta, LocalDateTime fechaCreacion,
                                LocalDateTime fechaRespuesta, String respondidoPor) {}

    public record EvaluacionRequest(@Min(1) @Max(5) int estrellas, @Size(max = 500) String comentario) {}

    public record EvaluacionResponse(LocalDate fecha, int estrellas, String comentario) {}

    public record PasoAyuda(int numero, String titulo, String detalle) {}

    public record AyudaResponse(String whatsappCorporativo, String correoContacto, String mensajeWhatsapp, List<PasoAyuda> pasos) {}
}
