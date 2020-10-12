package nl.b3p.brmo.loader.gml;

import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureStore;
import org.geotools.jdbc.JDBCDataStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testcases voor
 * <a href="http://mantis.b3p.nl/view.php?id=11620">mantis-11620</a>. Deze
 * testcase test ove er geen vervallen object in de tabel terecht komen. Om de
 * tests te runnen gebruik je:
 * {@code mvn -Dit.test=Mantis11620GmlLightLoaderIntegrationTest -Dtest.onlyITs=true integration-test -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=Mantis11620GmlLightLoaderIntegrationTest -Dtest.onlyITs=true integration-test -Ppostgresql > target/postgresql.log}
 * voor Postgis.
 *
 * Maak een override properties file aan voor jouw eigen database omgeving met
 * de naam {@code local.<DATABASETYPE>.properties} waarin je de properties
 * overschrijft die in {@code src/test/resources/<DATABASETYPE>.properties} zijn
 * vastgelegd.
 *
 * @author mprins
 */
public class Mantis11620GmlLightLoaderIntegrationTest extends TestingBase {

    private BGTGMLLightLoader ldr;

    private final Lock sequential = new ReentrantLock();

    /**
     * set up test object.
     *
     * @throws IOException als laden van property file mislukt
     */
    @BeforeEach
    public void setUp() throws Exception {
        loadProps();

        sequential.lock();

        ldr = new BGTGMLLightLoader();
        ldr.setDbConnProps(params);
        ldr.setCreateTables(false);

        clearTables();
    }

    @AfterEach
    public void resetDatabase() throws Exception {
        clearTables();
        sequential.unlock();
    }

    /**
     * test parsen en laden van het test bestand met vervallen object. Het
     * resultaat moet een lege tabel zijn (0 features geschreven).
     *
     * @throws Exception if any
     */
    @Test
    public void testProcessGMLFile() throws Exception {
        File gml = new File(BGTGMLLightLoaderIntegrationTest.class.getResource("/mantis-11620/bgt_wegdeel.gml").toURI());
        int result = ldr.processGMLFile(gml);
        int expected = 0;

        assertEquals(expected, result, "Aantal geschreven features moet 0 zijn want object is vervallen.");

        // doublecheck met geotools want die hebben we toch al
        JDBCDataStore dataStore = null;
        try {
            dataStore = (JDBCDataStore) DataStoreFinder.getDataStore(params);
            FeatureStore store = (FeatureStore) dataStore.getFeatureSource(isOracle ? "wegdeel".toUpperCase() : "wegdeel");
            assertEquals(expected, store.getFeatures().size(), "Aantal features is groter dan verwacht. ");
            dataStore.dispose();
        } finally {
            if (dataStore != null) {
                dataStore.dispose();
            }
        }
    }
}
