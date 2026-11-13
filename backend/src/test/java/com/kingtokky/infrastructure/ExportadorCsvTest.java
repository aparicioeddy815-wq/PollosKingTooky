package com.kingtokky.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.kingtokky.infrastructure.export.ExportadorCsv;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExportadorCsvTest {

    private final ExportadorCsv exportador = new ExportadorCsv();

    @Test
    void escribeCabecerasFilasYEscapaComasYComillas() {
        byte[] bytes = exportador.generar(List.of("Producto", "Nota"), List.of(List.of("Combo Rey", "con \"extra\" salsa, sin ensalada")));
        String texto = new String(bytes, StandardCharsets.UTF_8);

        assertThat(texto).startsWith("\uFEFF").contains("Producto,Nota");
        assertThat(texto).contains("Combo Rey,\"con \"\"extra\"\" salsa, sin ensalada\"");
    }

    @Test
    void losValoresNulosQuedanVacios() {
        java.util.List<Object> fila = new java.util.ArrayList<>();
        fila.add("A");
        fila.add(null);
        String texto = new String(exportador.generar(List.of("x", "y"), List.of(fila)), StandardCharsets.UTF_8);
        assertThat(texto).contains("A,\r\n");
    }
}
