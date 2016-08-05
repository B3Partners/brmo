/*
 * Copyright (C) 2016 B3Partners B.V.
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
 * Reader implementatie voor BGT light formaat, geeft nooit berichten terug.
 *
 * @author mprins
 */
public final class BGTLightFileReader extends BrmoXMLReader {

    private long fileSize = 0;

    public BGTLightFileReader(String fileName) throws Exception {
        if (fileName != null) {
            this.setBestandsNaam(fileName);
        }
        this.init();
    }

    /**
     *
     * @throws Exception als het bestand niet bestaat of leeg is
     */
    @Override
    public void init() throws Exception {
         soort = BrmoFramework.BR_BGTLIGHT;
        File input = new File(this.getBestandsNaam());
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(input))) {
            fileSize = input.length();
            ZipEntry entry = zip.getNextEntry();
            while (entry != null) {
                if (entry.getName().toLowerCase().endsWith(".gml")) {
                    long t = entry.getTime();
                    if (t < 0) {
                        entry = zip.getNextEntry();
                    } else {
                        this.setBestandsDatum(new Date(t));
                        break;
                    }
                }
            }
        }
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
        // verwacht een naam bestaande uit: grid_nivo-datum.zip bijvoorbeeld 38468_0-20160429.zip
        StringTokenizer parts = new StringTokenizer(
                naam.substring(naam.lastIndexOf(File.separator) + 1, naam.length()),
                "_-.", false);

        if (parts.countTokens() > 3) {
            this.setGebied("grid " + parts.nextToken() + " aggrlevel " + parts.nextToken());
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
