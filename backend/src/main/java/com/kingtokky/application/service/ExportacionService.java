package com.kingtokky.application.service;

import com.kingtokky.application.dto.comun.ArchivoDescarga;
import com.kingtokky.infrastructure.export.ExportadorCsv;
import com.kingtokky.infrastructure.export.ExportadorExcel;
import com.kingtokky.infrastructure.export.ExportadorPdf;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

/** Convierte cualquier tabla (cabeceras + filas) en CSV, Excel o PDF según el formato pedido. */
@Service
public class ExportacionService {

    private final ExportadorCsv csv;
    private final ExportadorExcel excel;
    private final ExportadorPdf pdf;

    public ExportacionService(ExportadorCsv csv, ExportadorExcel excel, ExportadorPdf pdf) {
        this.csv = csv;
        this.excel = excel;
        this.pdf = pdf;
    }

    public ArchivoDescarga exportar(String formato, String nombreBase, String titulo, List<String> cabeceras, List<List<Object>> filas) {
        String fmt = formato == null ? "csv" : formato.toLowerCase(Locale.ROOT);
        String fecha = LocalDate.now().toString();
        return switch (fmt) {
            case "xlsx", "excel" -> new ArchivoDescarga(nombreBase + "-" + fecha + ".xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    new ByteArrayResource(excel.generar(titulo.length() > 31 ? titulo.substring(0, 31) : titulo, cabeceras, filas)));
            case "pdf" -> new ArchivoDescarga(nombreBase + "-" + fecha + ".pdf", "application/pdf",
                    new ByteArrayResource(pdf.documento(titulo, "", List.of(new ExportadorPdf.Seccion("", cabeceras, filas)))));
            default -> new ArchivoDescarga(nombreBase + "-" + fecha + ".csv", "text/csv; charset=UTF-8",
                    new ByteArrayResource(csv.generar(cabeceras, filas)));
        };
    }
}
