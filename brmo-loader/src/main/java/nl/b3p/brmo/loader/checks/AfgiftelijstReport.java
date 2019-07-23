/*
 * Copyright (C) 2019  B3Partners B.V.

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.brmo.loader.checks;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import nl.b3p.brmo.loader.entity.Bericht.STATUS;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author meine
 */
public class AfgiftelijstReport {

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private final SimpleDateFormat xlsDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss,SSSSSSSSS");//26-06-2019 01:50:58,355000000
    private static final Log log = LogFactory.getLog(AfgiftelijstReport.class);
    private String datum;

    public void createReport(List<Afgifte> afgiftes, String inputFileName, File output) throws FileNotFoundException {
        datum = sdf.format(new Date());
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(output));

        pdfDoc.setDefaultPageSize(PageSize.A4.rotate());
        Footer footerHandler = new Footer(pdfDoc);

        //Assign event-handlers
        pdfDoc.addEventHandler(PdfDocumentEvent.END_PAGE, footerHandler);

        try (Document doc = new Document(pdfDoc)) {
            createFirstPage(doc, inputFileName);
            createTable(afgiftes, pdfDoc, doc);

            footerHandler.writeTotal(pdfDoc);
        } catch (IOException ex) {
            log.error("Cannot");
        }
    }

    private void createFirstPage(Document doc, String input) throws IOException {
        PdfFont bold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        Style style = new Style()
                .setFont(bold)
                .setFontSize(16)
                .setFontColor(new DeviceRgb(21, 127, 204));
        Paragraph title = new Paragraph("BRMO Controle afgiftelijst");
        title.addStyle(style);

        Paragraph text = new Paragraph("Dit rapport is gegenereerd op " + datum + " op basis van de afgiftelijst " + input + ".");
        doc.add(title);
        doc.add(text);
    }

    protected void createTable(List<Afgifte> afgiftes, PdfDocument pdfDoc, Document doc) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);

        pdfDoc.addNewPage();
        doc.add(new AreaBreak());
        Table table = new Table(8).useAllAvailableWidth();
        table.setBorder(new SolidBorder(1));
        createHeaderRow(table);
        table.setFont(font);
        for (Afgifte afgifte : afgiftes) {
            createRow(afgifte, table);
        }
        doc.add(table);
    }

    private void createHeaderRow(Table table) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        table.setFont(font);
        
        Border b = new SolidBorder(2);

        table.addCell(new Cell().add(new Paragraph("Klantnr.").setFont(font))).setBorder(b);
        table.addCell(new Cell().add(new Paragraph("Contractnr.").setFont(font))).setBorder(b);
        table.addCell(new Cell().add(new Paragraph("Datum").setFont(font))).setBorder(b);
        table.addCell(new Cell().add(new Paragraph("Bestand").setFont(font))).setBorder(b);
        table.addCell(new Cell().add(new Paragraph("Rapport via URL").setFont(font))).setBorder(b);
        table.addCell(new Cell().add(new Paragraph("Geleverd").setFont(font))).setBorder(b);
        table.addCell(new Cell().add(new Paragraph("In staging").setFont(font))).setBorder(b);
        table.addCell(new Cell().add(new Paragraph("Status").setFont(font))).setBorder(b);
    }

    private void createRow(Afgifte afgifte, Table table) {
        Border b = new SolidBorder(1);
        table.addCell(afgifte.getKlantnummer()).setBorder(b);
        table.addCell(afgifte.getContractnummer()).setBorder(b);
        try {
            table.addCell(sdf.format(xlsDate.parse(afgifte.getDatum()))).setBorder(b);
        } catch (ParseException ex) {
            table.addCell(afgifte.getDatum()).setBorder(b);
        }
        table.addCell(afgifte.getBestandsnaam()).setBorder(b);
        table.addCell(afgifte.isRapport() ? "Ja" : "Nee").setBorder(b);
        table.addCell(afgifte.isGeleverd() ? "Ja" : "Nee").setBorder(b);
        table.addCell(afgifte.isFoundInStaging() ? "Ja" : "Nee").setBorder(b);
        table.addCell(getStatusString(afgifte)).setBorder(b);
    }

    private String getStatusString(Afgifte afgifte) {
        String res = "";
        Map<STATUS, Integer> stati = afgifte.getStatussen();
        for (STATUS status : stati.keySet()) {
            if (!res.isEmpty()) {
                res += "\n";
            }
            res += status.name() + ":" + stati.get(status);
        }
        if(res.isEmpty()){
            res = "-";
        }
        return res;
    }

    protected class Footer implements IEventHandler {

        protected PdfFormXObject placeholder;
        protected float side = 20;
        protected float x = 790;
        protected float y = 5;
        protected float space = 4.5f;
        protected float descent = 3;

        public Footer(PdfDocument pdf) {
            placeholder = new PdfFormXObject(new Rectangle(0, 0, side, side));
        }

        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdf = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdf.getPageNumber(page);
            Rectangle pageSize = page.getPageSize();
            PdfCanvas pdfCanvas = new PdfCanvas(page.getLastContentStream(), page.getResources(), pdf);
            Canvas canvas = new Canvas(pdfCanvas, pdf, pageSize);
            Paragraph p = new Paragraph().add("Pagina ").add(String.valueOf(pageNumber)).add(" van");
            Paragraph p2 = new Paragraph().add("B3Partners BRMO controlemodule - " + datum);
            canvas.showTextAligned(p2, 35, y, TextAlignment.LEFT);
            canvas.showTextAligned(p, x, y, TextAlignment.RIGHT);
            pdfCanvas.addXObject(placeholder, x + space, y - descent);
            pdfCanvas.release();
        }

        public void writeTotal(PdfDocument pdf) {
            Canvas canvas = new Canvas(placeholder, pdf);
            canvas.showTextAligned(String.valueOf(pdf.getNumberOfPages()), 0, descent, TextAlignment.LEFT);
        }
    }
}
