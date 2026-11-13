package com.kingtokky.application.service;

import com.kingtokky.application.dto.ReporteDtos.RespaldoResponse;
import com.kingtokky.application.dto.comun.ArchivoDescarga;
import com.kingtokky.application.dto.comun.PaginaResponse;
import com.kingtokky.domain.model.auditoria.NivelBitacora;
import com.kingtokky.domain.model.auditoria.TipoEvento;
import com.kingtokky.domain.model.reporte.EstadoRespaldo;
import com.kingtokky.domain.model.reporte.Respaldo;
import com.kingtokky.domain.repository.RespaldoRepository;
import com.kingtokky.infrastructure.config.KingTokkyProperties;
import com.kingtokky.presentation.exception.ApiException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Respaldo lógico de la base de datos: un archivo .sql con los datos de todas las tablas (INSERT) listo para
 * restaurar sobre el esquema de /init. Se ejecuta cada 12 horas y también a pedido del administrador.
 */
@Service
public class RespaldoService {

    private static final Logger log = LoggerFactory.getLogger(RespaldoService.class);
    private static final int ARCHIVOS_A_CONSERVAR = 30;
    private static final DateTimeFormatter SELLO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    private final JdbcTemplate jdbc;
    private final RespaldoRepository respaldos;
    private final AuditoriaService auditoria;
    private final KingTokkyProperties propiedades;

    public RespaldoService(JdbcTemplate jdbc, RespaldoRepository respaldos, AuditoriaService auditoria, KingTokkyProperties propiedades) {
        this.jdbc = jdbc;
        this.respaldos = respaldos;
        this.auditoria = auditoria;
        this.propiedades = propiedades;
    }

    public PaginaResponse<RespaldoResponse> listar(int pagina, int tamano) {
        return PaginaResponse.de(respaldos.findAllByOrderByFechaHoraDesc(PageRequest.of(pagina, Math.min(tamano, 100))), this::aRespuesta);
    }

    public RespaldoResponse ejecutar(boolean automatico) {
        long inicio = System.currentTimeMillis();
        LocalDateTime ahora = LocalDateTime.now();
        String archivo = "kingtokky_" + ahora.format(SELLO) + ".sql";
        Respaldo r = new Respaldo();
        r.setArchivo(archivo);
        try {
            Path carpeta = carpeta();
            Files.createDirectories(carpeta);
            Path destino = carpeta.resolve(archivo);
            volcar(destino);
            r.setEstado(EstadoRespaldo.EXITOSO);
            r.setTamanoBytes(Files.size(destino));
            r.setMensaje("Respaldo lógico completado");
            depurarAntiguos(carpeta);
        } catch (IOException | RuntimeException e) {
            log.error("Falló el respaldo de la base de datos", e);
            r.setEstado(EstadoRespaldo.FALLIDO);
            r.setMensaje(recortar(e.getMessage()));
        }
        r.setDuracionMs(System.currentTimeMillis() - inicio);
        respaldos.save(r);
        boolean exitoso = r.getEstado() == EstadoRespaldo.EXITOSO;
        String texto = exitoso ? (automatico ? "Respaldo automático generado: " : "Respaldo manual generado: ") + archivo : "Falló el respaldo: " + r.getMensaje();
        if (automatico) {
            auditoria.registrarSistema(TipoEvento.RESPALDO, "Respaldo", texto, exitoso ? NivelBitacora.INFO : NivelBitacora.CRITICO);
        } else {
            auditoria.registrar(TipoEvento.RESPALDO, "Respaldo", texto, exitoso ? NivelBitacora.INFO : NivelBitacora.CRITICO);
        }
        return aRespuesta(r);
    }

    public ArchivoDescarga descargar(Long id) {
        Respaldo r = respaldos.findById(id).orElseThrow(() -> ApiException.noEncontrado("Respaldo no encontrado"));
        Path ruta = carpeta().resolve(r.getArchivo()).normalize();
        if (r.getEstado() != EstadoRespaldo.EXITOSO || !ruta.startsWith(carpeta()) || !Files.isRegularFile(ruta)) {
            throw ApiException.noEncontrado("El archivo de este respaldo ya no está disponible");
        }
        return new ArchivoDescarga(r.getArchivo(), "application/sql", new FileSystemResource(ruta));
    }

    // ------------------------------------------------------------------ volcado

    private void volcar(Path destino) {
        jdbc.execute((java.sql.Connection con) -> {
            try (BufferedWriter out = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
                out.write("-- King Tokky · respaldo lógico generado el " + LocalDateTime.now() + "\n");
                out.write("-- Restaurar: crear una base vacía, ejecutar init/01-esquema.sql y luego este archivo con psql -f.\n");
                out.write("BEGIN;\n\n");
                List<String> tablas = jdbc.queryForList("""
                        SELECT c.relname FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
                        WHERE n.nspname = 'public' AND c.relkind = 'r' AND c.relname <> 'flyway_schema_history'
                        ORDER BY c.oid""", String.class);
                for (String tabla : tablas) {
                    volcarTabla(con, out, tabla);
                }
                List<java.util.Map<String, Object>> identidades = jdbc.queryForList(
                        "SELECT table_name, column_name FROM information_schema.columns WHERE table_schema = 'public' AND is_identity = 'YES'");
                out.write("\n-- Ajuste de secuencias\n");
                for (var fila : identidades) {
                    String t = String.valueOf(fila.get("table_name"));
                    String c = String.valueOf(fila.get("column_name"));
                    out.write("SELECT setval(pg_get_serial_sequence('" + t + "', '" + c + "'), COALESCE(MAX(\"" + c + "\"), 1)) FROM \"" + t + "\";\n");
                }
                out.write("\nCOMMIT;\n");
            } catch (IOException | SQLException e) {
                throw new IllegalStateException("No se pudo escribir el respaldo: " + e.getMessage(), e);
            }
            return null;
        });
    }

    private void volcarTabla(java.sql.Connection con, BufferedWriter out, String tabla) throws SQLException, IOException {
        out.write("-- Tabla " + tabla + "\n");
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM \"" + tabla + "\"")) {
            ResultSetMetaData md = rs.getMetaData();
            int columnas = md.getColumnCount();
            StringBuilder nombres = new StringBuilder();
            for (int i = 1; i <= columnas; i++) nombres.append(i > 1 ? ", " : "").append('"').append(md.getColumnName(i)).append('"');
            while (rs.next()) {
                StringBuilder valores = new StringBuilder();
                for (int i = 1; i <= columnas; i++) {
                    if (i > 1) valores.append(", ");
                    valores.append(literal(rs, md.getColumnType(i), i));
                }
                out.write("INSERT INTO \"" + tabla + "\" (" + nombres + ") VALUES (" + valores + ");\n");
            }
        }
        out.write("\n");
    }

    private String literal(ResultSet rs, int tipo, int indice) throws SQLException {
        String valor = rs.getString(indice);
        if (valor == null) return "NULL";
        return switch (tipo) {
            case Types.BIGINT, Types.INTEGER, Types.SMALLINT, Types.TINYINT, Types.NUMERIC, Types.DECIMAL, Types.DOUBLE, Types.FLOAT, Types.REAL -> valor;
            case Types.BIT, Types.BOOLEAN -> rs.getBoolean(indice) ? "true" : "false";
            default -> "'" + valor.replace("'", "''") + "'";
        };
    }

    private void depurarAntiguos(Path carpeta) throws IOException {
        try (Stream<Path> archivos = Files.list(carpeta)) {
            List<Path> viejos = archivos.filter(p -> p.getFileName().toString().startsWith("kingtokky_") && p.toString().endsWith(".sql"))
                    .sorted((a, b) -> b.getFileName().toString().compareTo(a.getFileName().toString())).skip(ARCHIVOS_A_CONSERVAR).toList();
            for (Path p : viejos) Files.deleteIfExists(p);
        }
    }

    private Path carpeta() {
        return Path.of(propiedades.respaldo().directorio()).toAbsolutePath().normalize();
    }

    private String recortar(String mensaje) {
        if (mensaje == null) return "Error desconocido";
        return mensaje.length() > 290 ? mensaje.substring(0, 290) : mensaje;
    }

    private RespaldoResponse aRespuesta(Respaldo r) {
        boolean disponible = r.getEstado() == EstadoRespaldo.EXITOSO && Files.isRegularFile(carpeta().resolve(r.getArchivo()));
        return new RespaldoResponse(r.getId(), r.getFechaHora(), r.getArchivo(), r.getTamanoBytes(), r.getEstado().name(), r.getDuracionMs(), r.getMensaje(), disponible);
    }
}
