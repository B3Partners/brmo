package nl.b3p.brmo.loader.gml;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumingThat;

/**
 * Testcases voor {@link BGTGMLLightLoader}. Om de tests te runnen gebruik je:
 * {@code mvn -Dit.test=BGTLightZipsLoaderIntegrationTest -Dtest.onlyITs=true integration-test -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=BGTLightZipsLoaderIntegrationTest -Dtest.onlyITs=true integration-test -Ppostgresql > target/postgresql.log}
 * voor Postgis.
 *
 * @author mprins
 */
public class BGTLightZipsLoaderIntegrationTest extends TestingBase {

    private static final Log LOG = LogFactory.getLog(BGTGMLLightLoaderIntegrationTest.class);
    private final Lock sequential = new ReentrantLock();
    private BGTGMLLightLoader ldr;

    /**
     * set up test object.
     *
     * @throws IOException als laden van property file mislukt
     */
    @BeforeEach
    public void setUp() throws Exception {
        loadProps();

        ldr = new BGTGMLLightLoader();
        ldr.setDbConnProps(params);

        sequential.lock();

        ldr.truncateTables();
    }

    /**
     * opruimen na de test.
     *
     * @throws Exception in geval van database fout
     */
    @AfterEach
    public void cleanUp() throws Exception {
        ldr.resetStatus();
        ldr = null;

        clearTables();

        sequential.unlock();
    }

    /**
     * Test of een combi download van twee kaartbladen dezlfde aantallen
     * features oplevert als twee losse zipfiles.
     *
     * @throws Exception if any
     */
    @Test
    public void testCombiKaartbladTegenLosseKaartbladenInZipFiles() throws Exception {
        ldr.setScanDirectory(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/schiermonnikoog/").getFile());
        List<File> zips = ldr.scanDirectory();
        // Verwacht aantal zipfiles
        assumingThat(zips.size() == 3, () -> {

            final int expected = 399;
            int actualCombi = 0;
            int actualLos = 0;

            for (File zip : zips) {
                // kaaartbladen combi-zip laden
                if (zip.getName().contains("57830-57831.zip")) {
                    LOG.info("Laden van 'combi' zipfile/kaartbladen: " + zip.getName());
                    actualCombi = ldr.processZipFile(zip);
                }
            }
            final Map<String, Long> countsCombi = countRows();

            // maak een nieuwe loader omdat de interne cache van de loader anders niet leeg is
            // en leeg de tabellen
            ldr = new BGTGMLLightLoader();
            ldr.setDbConnProps(params);
            ldr.truncateTables();

            for (File zip : zips) {
                // losse kaart bladen laden uit de anadres 2 zips
                if (!zip.getName().contains("57830-57831.zip")) {
                    LOG.info("Laden van 'losse' zipfile/kaartblad: " + zip.getName());
                    // los is 345 + 54 = 399
                    actualLos += ldr.processZipFile(zip);
                }
            }
            final Map<String, Long> countsLos = countRows();

            assertEquals(expected, actualCombi, "Verwacht aantal geschreven features klopt niet");
            assertEquals(expected, actualLos, "Verwacht aantal geschreven features klopt niet");
            assertEquals(countsCombi, countsLos, "Gecombineerde kaartblad set komt niet overeen met losse geladen kaartbladen");
        });
    }

    /**
     * Laadt 2 kaartbladen, een keer uit een gecombineerde zip en ook als twee
     * losse zips. Test of
     *
     * @throws Exception soms
     */
    @Test
    public void testCombiKaartbladPlusLosseKaartbladenInZipFiles() throws Exception {
        final int expected = 399;
        ldr.setScanDirectory(BGTGMLLightLoaderIntegrationTest.class.getResource("/gmllight/schiermonnikoog/").getFile());
        List<File> zips = ldr.scanDirectory();
        // Verwacht aantal zipfiles
        assumingThat(zips.size() == 3, () -> {
            for (File zip : zips) {
                // alle kaartbladen  laden
                LOG.info("Laden van 'combi' zipfile/kaartbladen: " + zip.getName());
                ldr.processZipFile(zip);
            }
            final Map<String, Long> countsCombi = countRows();

            long actual = 0;
            actual = countsCombi.values().stream().map((count) -> count).reduce(actual, (accumulator, _item) -> accumulator + _item);
            assertEquals(expected, actual, "Verwacht aantal geschreven features klopt niet");
        });

    }

}
