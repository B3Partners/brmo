/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.xml;

import java.io.File;
import java.util.StringTokenizer;
import nl.b3p.brmo.loader.entity.Bericht;

/**
 *
 * @author Mark Prins
 */
public final class BGTLightFileReader extends BrmoXMLReader {

    private boolean hasNext = false;

    public BGTLightFileReader(String fileName) throws Exception {
        if (fileName != null) {
            this.setBestandsNaam(fileName);
        }
        this.init();
    }

    @Override
    public void init() throws Exception {
        hasNext = true;
    }

    @Override
    public void setBestandsNaam(String naam) {
        // verwacht een naam bestaande uit: grid_nivo-datum.zip bijvoorbeeld 38468_0-20160429.zip
        StringTokenizer parts = new StringTokenizer(
                naam.substring(naam.lastIndexOf(File.separator) + 1, naam.length()),
                "_-.", false);

        if (parts.countTokens() > 3) {
            this.setGebied("grid " + parts.nextToken() + " aggrlevel " + parts.nextToken());
            this.setDatumAsString(parts.nextToken(), "yyyyMMdd");
        }
        super.setBestandsNaam(naam);
    }

    @Override
    public boolean hasNext() throws Exception {
        return hasNext;
    }

    @Override
    public Bericht next() throws Exception {
        hasNext = false;
        Bericht b = new Bericht(null);
        b.setDatum(this.getBestandsDatum());
        return b;
    }

}
