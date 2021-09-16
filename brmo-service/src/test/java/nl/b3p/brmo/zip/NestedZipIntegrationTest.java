/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.zip;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.service.testutil.TestUtil;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * run:
 * {@code mvn -Dit.test=NestedZipIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql}
 *
 * @author mprins
 */
public class NestedZipIntegrationTest extends TestUtil {

    private static final Log LOG = LogFactory.getLog(NestedZipIntegrationTest.class);
    private BrmoFramework brmo;

    @BeforeEach
    public void setUp() throws Exception {
        brmo = new BrmoFramework(dsStaging, null);
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.emptyStagingDb();
        brmo.closeBrmoFramework();
        brmo = null;
    }

    @Test
    public void testLadenNestedZipFiles() throws Exception {
        InputStream input = NestedZipIntegrationTest.class.getResourceAsStream("/GH-317-BAG-van-GDS2/DNLDLXAM02-15967-5014117-31032017-01042017.zip");
        ZipInputStream zip = new ZipInputStream(input);
        ZipEntry entry = zip.getNextEntry();
        while (entry != null) {
            LOG.debug("gevonden " + entry.getName());
            if (!entry.getName().toLowerCase().startsWith("gem-wpl-relatie")
                    && !entry.getName().equalsIgnoreCase("Leveringsdocument-BAG-Mutaties.xml")) {
                if (entry.getName().toLowerCase().endsWith("zip")) {
                    LOG.debug("start verwerken " + entry.getName());
                    // alleen mutaties oppakken
                    ZipInputStream innerzip = new ZipInputStream(zip);
                    ZipEntry innerentry = innerzip.getNextEntry();
                    while (innerentry != null && innerentry.getName().toLowerCase().endsWith(".xml")) {
                        LOG.info("verwerken " + innerentry.getName());
                        brmo.loadFromStream("bag", CloseShieldInputStream.wrap(innerzip), "DNLDLXAM02-15967-5014117-31032017-01042017.zip/" + innerentry.getName(), (Long)null);
                        innerentry = innerzip.getNextEntry();
                    }
                    LOG.debug("einde verwerken " + entry.getName());
                } else {
                    LOG.warn("Overslaan van onbekend bestand in bag mutaties: " + entry.getName());
                }
            }
            entry = zip.getNextEntry();
        }
        zip.close();

        Assertions.assertEquals(
                2l,
                brmo.getCountLaadProcessen(null, null, "bag", null),
                "het aantal LP mag niet afwijken"
        );
        Assertions.assertEquals(
                /*<product_LVC:Nieuw> - 5723l + 3210l= 8933*/ 8921l /*<product_LVC:Mutatie-product>  10000l + 6301l*/,
                brmo.getCountBerichten(null, null, "bag", null),
                "het aantal Bericht mag niet afwijken"
        );
    }
}
