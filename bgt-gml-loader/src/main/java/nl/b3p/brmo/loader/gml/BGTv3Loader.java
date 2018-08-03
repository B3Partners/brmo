/*
 * Copyright (C) 2018 B3Partners B.V.
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
package nl.b3p.brmo.loader.gml;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author matthijsln
 */
public class BGTv3Loader {

    private static final Log log = LogFactory.getLog(BGTv3Loader.class);

    /**
     * gegevens voor database verbinding tbv. Geotools.
     */
    private Properties dbConnProps;

    private String opmerkingen;

    public Properties getDbConnProps() {
        return dbConnProps;
    }

    public void setDbConnProps(Properties dbConnProps) {
        this.dbConnProps = dbConnProps;
    }

    public String getOpmerkingen() {
        return opmerkingen;
    }

    public void setOpmerkingen(String opmerkingen) {
        this.opmerkingen = opmerkingen;
    }

    public boolean processZipFile(File f) throws Exception {

        log.info(String.format("Processing file %s (%s)", f.getAbsolutePath(), FileUtils.byteCountToDisplaySize(f.length())));

        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(f))) {
            ZipEntry entry = zip.getNextEntry();
            if(entry == null) {
                log.warn("Geen bestanden in zipfile");
                return false;
            }
            do {
                String ext = FilenameUtils.getExtension(entry.getName());
                if(!ext.equals("gml") || !ext.equals("xml")) {
                    log.warn("Overslaan zip entry geen GML/XML bestand: " + entry.getName());
                } else {
                    log.info("Lezen bestand: " + entry.getName());
                    /*
                    result = storeFeatureCollection(new CloseShieldInputStream(zip), eName.toLowerCase());
                    opmerkingen.append(result)
                            .append(" features geladen uit: ")
                            .append(eName).append(", zipfile: ")
                            .append(zipExtract.getCanonicalPath())
                            .append("\n");
                    total += result;*/
                }
                entry = zip.getNextEntry();
            } while(entry != null);
        }

        return true;
    }
}
