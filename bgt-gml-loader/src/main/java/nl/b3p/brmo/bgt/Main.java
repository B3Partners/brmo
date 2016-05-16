/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.bgt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import nl.b3p.brmo.loader.gml.BGTGMLLightLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.factory.GeoTools;

public class Main {

    private static final Log LOG = LogFactory.getLog(Main.class);
    static {
        GeoTools.init();
    }

    public static void main(String... args) {
        Properties prop = new Properties();
        try (FileInputStream fos = new FileInputStream("./bgtloader.properties")) {
            prop.load(fos);
            fos.close();
        } catch (FileNotFoundException ex) {
            LOG.fatal("Property file 'bgtloader.properties' is niet gevonden.", ex);
            System.exit(1);
        } catch (IOException io) {
            LOG.fatal("Laden van de property file is mislukt.", io);
            System.exit(1);
        }

        BGTGMLLightLoader ldr = new BGTGMLLightLoader();
        ldr.setDbConnProps(prop);
        ldr.setScanDirectory(prop.getProperty("scandirectory"));

        List<File> zips = ldr.scanDirectory();
        for (File zip : zips) {
            try {
                int actual = ldr.processZipFile(zip);
                LOG.info(actual + " objecten geladen uit bestand: " + zip);
            } catch (IOException ex) {
                LOG.error("Laden van bestand " + zip + " is mislukt.", ex);
            }
        }
    }
}
