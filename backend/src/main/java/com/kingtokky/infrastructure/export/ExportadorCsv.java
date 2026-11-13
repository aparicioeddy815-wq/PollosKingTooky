package com.kingtokky.infrastructure.export;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.stereotype.Component;

/** Genera CSV en UTF-8 con BOM para que Excel respete las tildes. */
@Component
public class ExportadorCsv {

    public byte[] generar(List<String> cabeceras, List<List<Object>> filas) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append(String.join(",", cabeceras.stream().map(this::celda).toList())).append("\r\n");
        for (List<Object> fila : filas) {
            sb.append(String.join(",", fila.stream().map(v -> celda(v == null ? "" : v.toString())).toList())).append("\r\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String celda(String valor) {
        boolean requiereComillas = valor.contains(",") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r");
        String limpio = valor.replace("\"", "\"\"");
        return requiereComillas ? "\"" + limpio + "\"" : limpio;
    }
}
