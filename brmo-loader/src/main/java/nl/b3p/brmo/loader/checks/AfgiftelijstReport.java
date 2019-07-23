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
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.b3p.brmo.loader.entity.Bericht.STATUS;

/**
 *
 * @author meine
 */
public class AfgiftelijstReport {
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    
    public void createReport(List<Afgifte> afgiftes, String input, File output) throws FileNotFoundException {
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(output));
        try (Document doc = new Document(pdfDoc)) {
            createFirstPage(doc, input);
            createTable(afgiftes, pdfDoc, doc);
        } catch (IOException ex) {
            Logger.getLogger(AfgiftelijstReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createFirstPage(Document doc, String input) {
        try {
            PdfFont bold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
            Style style = new Style()
                    .setFont(bold)
                    .setFontSize(12)
                    .setFontColor(new DeviceRgb(21, 127, 204));//ColorConstants.BLUE); //#157fcc
            Paragraph title = new Paragraph("BRMO Controle afgiftelijst");
            title.addStyle(style);
            
            Paragraph text = new Paragraph("Dit rapport is gegenereerd op " + sdf.format(new Date()) + " op basis van de afgiftelijst " + input + ".");
            doc.add(title);
            doc.add(text);
        } catch (IOException ex) {
            Logger.getLogger(AfgiftelijstReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
 
    protected void createTable(List<Afgifte> afgiftes, PdfDocument pdfDoc, Document doc) throws IOException {
        PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        
        pdfDoc.setDefaultPageSize(PageSize.A4.rotate());
        pdfDoc.addNewPage();
        doc.add(new AreaBreak());
        Table table = new Table(8,true).useAllAvailableWidth();
        createHeaderRow(table);
        table.setFont(font);
        for (Afgifte afgifte : afgiftes) {
            createRow(afgifte, table);
        }
        doc.add(table);
    }
    
    private void createHeaderRow(Table table) throws IOException{
        PdfFont font = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        table.setFont(font);
        table.addCell("Klantnr.").setFont(font);
        table.addCell("Contractnr.").setFont(font);
        table.addCell("Datum").setFont(font);
        table.addCell("Bestand").setFont(font);
        table.addCell("Rapport via URL").setFont(font);
        table.addCell("Geleverd").setFont(font);
        table.addCell("In staging").setFont(font);
        table.addCell("Status").setFont(font);
        
    }

    private void createRow(Afgifte afgifte, Table table){
        table.addCell(afgifte.getKlantnummer());
        table.addCell(afgifte.getContractnummer());
        table.addCell(afgifte.getDatum());
        table.addCell(afgifte.getBestandsnaam());
        table.addCell(afgifte.isRapport() ? "Ja" : "Nee");
        table.addCell(afgifte.isGeleverd() ? "Ja" : "Nee");
        table.addCell(afgifte.isFoundInStaging() ? "Ja" : "Nee");
        table.addCell(getStatusString(afgifte));
    }
    
    private String getStatusString(Afgifte afgifte){
        String res = "";
        Map<STATUS, Integer> stati = afgifte.getStatussen();
        for (STATUS status : stati.keySet()) {
            if(!res.isEmpty()){
                res += "\n";
            }
            res += status.name() + ":" + stati.get(status);
        }
        return res;
    }
    
}
