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

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author Meine Toonen
 */
public class AfgiftelijstParser {
    public List<Afgifte> parse(InputStream input) throws IOException {
        return parseExcel(input);
    }

    private List<Afgifte> parseExcel(InputStream input) throws IOException {
        List<Afgifte> afgiftes = new ArrayList<>();
        Workbook wb = WorkbookFactory.create(input);

        Sheet sheet = wb.getSheetAt(0);
        boolean first = true;
        for (Row row : sheet) {
            if(first){
                first = false;
                continue;
            }
            Cell c = row.getCell(0);
            if(c == null){
                break;
            }
            Afgifte afgifte = new Afgifte();
            afgiftes.add(afgifte);
            
            afgifte.setKlantnummer(getStringValue(row.getCell(0)));
            afgifte.setContractnummer(getStringValue(row.getCell(1)));
            afgifte.setDatum(row.getCell(2).getStringCellValue());
            afgifte.setBestandsnaam(row.getCell(3).getStringCellValue());
            afgifte.setRapport(row.getCell(4).getStringCellValue().equals("J"));
            afgifte.setGeleverd(row.getCell(5).getStringCellValue().equals("J"));
        }

        return afgiftes;
    }
    
    private final DecimalFormat decimalFormat = new DecimalFormat("0");
    private String getStringValue(Cell c){
        switch(c.getCellType()){
            case STRING:
                return c.getStringCellValue();
            case NUMERIC:
                return decimalFormat.format(c.getNumericCellValue());
            default:
                return null;
        }
    }
}
