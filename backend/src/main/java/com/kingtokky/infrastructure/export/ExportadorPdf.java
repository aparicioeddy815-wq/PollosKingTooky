package com.kingtokky.infrastructure.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;

/** Genera documentos PDF sencillos: título, subtítulo y una o varias tablas. */
@Component
public class ExportadorPdf {

    public record Seccion(String titulo, List<String> cabeceras, List<List<Object>> filas) {}

    private static final Color NARANJA = new Color(0xF4, 0x7C, 0x18);
    private static final Color ROJO = new Color(0xD3, 0x2F, 0x1E);

    public byte[] documento(String titulo, String subtitulo, List<Seccion> secciones) {
        boolean ancho = secciones.stream().anyMatch(s -> s.cabeceras().size() > 6);
        Document doc = new Document(ancho ? PageSize.A4.rotate() : PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(doc, salida);
            doc.open();
            Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, ROJO);
            Font fSub = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font fSeccion = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, Color.DARK_GRAY);
            doc.add(new Paragraph("King Tokky · " + titulo, fTitulo));
            String generado = "Generado el " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            doc.add(new Paragraph((subtitulo == null || subtitulo.isBlank() ? "" : subtitulo + " · ") + generado, fSub));
            doc.add(new Paragraph(" "));
            for (Seccion s : secciones) {
                if (s.titulo() != null && !s.titulo().isBlank()) {
                    doc.add(new Paragraph(s.titulo(), fSeccion));
                    doc.add(new Paragraph(" ", fSub));
                }
                doc.add(tabla(s));
                doc.add(new Paragraph(" "));
            }
            doc.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        }
        return salida.toByteArray();
    }

    private PdfPTable tabla(Seccion s) {
        PdfPTable tabla = new PdfPTable(s.cabeceras().size());
        tabla.setWidthPercentage(100);
        Font fCabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Font fCelda = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, Color.BLACK);
        for (String cabecera : s.cabeceras()) {
            PdfPCell celda = new PdfPCell(new Phrase(cabecera, fCabecera));
            celda.setBackgroundColor(NARANJA);
            celda.setPadding(5);
            celda.setBorder(Rectangle.NO_BORDER);
            tabla.addCell(celda);
        }
        boolean alterna = false;
        for (List<Object> fila : s.filas()) {
            for (int c = 0; c < s.cabeceras().size(); c++) {
                Object valor = c < fila.size() ? fila.get(c) : "";
                PdfPCell celda = new PdfPCell(new Phrase(valor == null ? "" : valor.toString(), fCelda));
                celda.setPadding(4);
                celda.setBorderColor(new Color(0xEC, 0xD2, 0xCE));
                if (alterna) celda.setBackgroundColor(new Color(0xFF, 0xF4, 0xF1));
                if (valor instanceof Number) celda.setHorizontalAlignment(Element.ALIGN_RIGHT);
                tabla.addCell(celda);
            }
            alterna = !alterna;
        }
        tabla.setHeaderRows(1);
        return tabla;
    }
}
