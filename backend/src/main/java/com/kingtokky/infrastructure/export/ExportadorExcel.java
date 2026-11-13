package com.kingtokky.infrastructure.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/** Genera libros de Excel (.xlsx) con una o varias hojas. */
@Component
public class ExportadorExcel {

    public record Hoja(String nombre, List<String> cabeceras, List<List<Object>> filas) {}

    public byte[] generar(List<Hoja> hojas) {
        try (Workbook libro = new XSSFWorkbook(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Font negrita = libro.createFont();
            negrita.setBold(true);
            CellStyle estiloCabecera = libro.createCellStyle();
            estiloCabecera.setFont(negrita);
            estiloCabecera.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            estiloCabecera.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            for (Hoja h : hojas) {
                Sheet hoja = libro.createSheet(h.nombre().length() > 31 ? h.nombre().substring(0, 31) : h.nombre());
                int[] anchos = new int[h.cabeceras().size()];
                Row cabecera = hoja.createRow(0);
                for (int c = 0; c < h.cabeceras().size(); c++) {
                    Cell celda = cabecera.createCell(c);
                    celda.setCellValue(h.cabeceras().get(c));
                    celda.setCellStyle(estiloCabecera);
                    anchos[c] = h.cabeceras().get(c).length();
                }
                for (int f = 0; f < h.filas().size(); f++) {
                    Row fila = hoja.createRow(f + 1);
                    List<Object> valores = h.filas().get(f);
                    for (int c = 0; c < valores.size(); c++) {
                        Object v = valores.get(c);
                        Cell celda = fila.createCell(c);
                        if (v instanceof Number n) celda.setCellValue(n.doubleValue());
                        else celda.setCellValue(v == null ? "" : v.toString());
                        if (c < anchos.length) anchos[c] = Math.max(anchos[c], v == null ? 0 : v.toString().length());
                    }
                }
                // El ancho se calcula a mano: autoSizeColumn necesita fuentes del sistema y falla en contenedores mínimos
                for (int c = 0; c < anchos.length; c++) hoja.setColumnWidth(c, Math.min(60, anchos[c] + 3) * 256);
                hoja.createFreezePane(0, 1);
            }
            libro.write(salida);
            return salida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public byte[] generar(String nombreHoja, List<String> cabeceras, List<List<Object>> filas) {
        return generar(List.of(new Hoja(nombreHoja, cabeceras, filas)));
    }
}
