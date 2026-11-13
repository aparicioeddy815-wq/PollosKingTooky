package com.kingtokky.infrastructure.config;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuración propia del sistema (prefijo "kingtokky" en application.yml). */
@ConfigurationProperties(prefix = "kingtokky")
public record KingTokkyProperties(
        Jwt jwt,
        Cors cors,
        Frontend frontend,
        Google google,
        Almacenamiento almacenamiento,
        Respaldo respaldo,
        Reservas reservas,
        Inventario inventario) {

    public record Jwt(String secreto, long accesoMinutos, long refreshDias, long refreshRecordarmeDias) {}

    public record Cors(List<String> origenesPermitidos) {}

    public record Frontend(String url) {}

    public record Google(String clientId) {}

    public record Almacenamiento(String directorio, String urlPublicaBase) {}

    public record Respaldo(boolean habilitado, String directorio) {}

    public record Reservas(int anticipacionMinimaMin, int diasMaxAnticipacion, int limiteReprogramarMin,
                           String horaInicio, String horaFin, int pasoMinutos, int maxPendientesPorCliente) {}

    public record Inventario(int diasProyeccion, BigDecimal factorStockBajo) {}
}
