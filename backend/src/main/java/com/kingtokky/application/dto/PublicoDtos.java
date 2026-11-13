package com.kingtokky.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class PublicoDtos {

    private PublicoDtos() {}

    public record ProductoPublico(Long idProducto, String nombre, String descripcion, BigDecimal precio, String imagenUrl, boolean disponible) {}

    public record CategoriaMenu(String codigo, String nombre, List<ProductoPublico> productos, String urlPedirAhora) {}

    public record MenuPublico(List<CategoriaMenu> categorias, LocalDateTime actualizado) {}

    public record Horario(String dias, String horario) {}

    public record Ubicacion(String direccion, double latitud, double longitud) {}

    public record InicioPublico(String nombreLocal, ProductoPublico productoMasVendido, List<Horario> horarios, Ubicacion ubicacion) {}

    public record SobreNosotros(String nombreLocal, String historia) {}

    public record TurnosPublico(List<Integer> enPreparacion, List<Integer> listos, LocalDateTime actualizado) {}
}
