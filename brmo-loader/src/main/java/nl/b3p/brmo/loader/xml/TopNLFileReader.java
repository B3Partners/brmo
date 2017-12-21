/*
 * Copyright (C) 2017 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.brmo.loader.xml;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;

/**
 *
 * @author Mark Prins
 */
public class TopNLFileReader extends BrmoXMLReader {

    private long fileSize = 0;

    public TopNLFileReader(String fileName, String soort) throws Exception {
        if (fileName != null) {
            this.setBestandsNaam(fileName);
        }
        this.soort = (soort == null ? BrmoFramework.BR_TOPNL : soort);
        this.init();
    }

    /**
     *
     * @throws Exception als het bestand niet bestaat of leeg is
     */
    @Override
    public void init() throws Exception {
        File input = new File(this.getBestandsNaam());
        fileSize = input.length();
        this.setBestandsDatum(new Date());
    }

    public long getFileSize() {
        return fileSize;
    }

    /**
     * probeer uit de naam af te leiden wat gebied en zoomnivo zijn.
     *
     * @param naam naam van de zipfile met gml bestanden.
     */
    @Override
    public void setBestandsNaam(String naam) {
        super.setBestandsNaam(naam);
        StringTokenizer parts = new StringTokenizer(
                naam.substring(naam.lastIndexOf(File.separator) + 1, naam.length()),
                "_-.", false);

        if (parts.countTokens() > 2) {
            this.setGebied("" + parts.nextToken() + " blad " + parts.nextToken());
        } else {
            this.setGebied("Nederland");
        }
    }

    @Override
    public boolean hasNext() throws Exception {
        return false;
    }

    @Override
    public Bericht next() throws Exception {
        return null;
    }
}
