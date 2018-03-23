package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

/**
 *
 * Draaien met:
 * {@code mvn -Dit.test=VerminderenStukdelenIntegrationTest -Dtest.onlyITs=true verify -Pmssql > target/mssql.log}
 * voor bijvoorbeeld MSSQL of
 * {@code mvn -Dit.test=VerminderenStukdelenIntegrationTest -Dtest.onlyITs=true verify -Ppostgres > target/postgres.log}.
 *
 * @author mprins
 */
public class VerminderenStukdelenIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(VerminderenStukdelenIntegrationTest.class);

    // bestand met "ontstaan" bericht
    private final String ontstaanBestand = "/verminderenstukdelen/MUTKX01-ASN00V2937-Bericht1.xml";
    private final String datumOntstaan = "2017-02-22";
    private final int brondocOntstaan = 57;

    // bestand met mutatie bericht
    private final String mutatieBestand = "/verminderenstukdelen/MUTKX01-ASN00V2937-Bericht2.xml";
    private final String datumMutatie = "2017-03-03";
    private final int brondocMutatie = 18;

    private BrmoFramework brmo;

    // dbunit
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;

    private final Lock sequential = new ReentrantLock(true);

    @Before
    @Override
    public void setUp() throws Exception {
        BasicDataSource dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.jdbc.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.passwd"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        BasicDataSource dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(params.getProperty("rsgb.jdbc.url"));
        dsRsgb.setUsername(params.getProperty("rsgb.user"));
        dsRsgb.setPassword(params.getProperty("rsgb.passwd"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);

        staging = new DatabaseDataSourceConnection(dsStaging);
        rsgb = new DatabaseDataSourceConnection(dsRsgb);

        if (this.isMsSQL) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), params.getProperty("rsgb.user").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            // we hebben alleen nog postgres over
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        brmo = new BrmoFramework(dsStaging, dsRsgb);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(BrkToStagingToRsgbIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        assumeTrue("Er zijn brk STAGING_OK berichten", 0l == brmo.getCountBerichten(null, null, BrmoFramework.BR_BRK, "STAGING_OK"));
        assumeTrue("Er zijn brk STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRK, "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging);
        staging.close();

        CleanUtil.cleanRSGB_BRK(rsgb, true);
        rsgb.close();

        sequential.unlock();
    }

    @Test
    public void testMinderStukdelenInMutatie() throws Exception {
        assumeNotNull("Het ontstaan test bestand moet er zijn.", BrkToStagingToRsgbIntegrationTest.class.getResource(ontstaanBestand));
        assumeNotNull("Het mutatie test bestand moet er zijn.", BrkToStagingToRsgbIntegrationTest.class.getResource(mutatieBestand));

        LOG.debug("laden van ontstaan bericht in staging DB.");
        brmo.loadFromFile(BrmoFramework.BR_BRK, BrkToStagingToRsgbIntegrationTest.class.getResource(ontstaanBestand).getFile());

        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull("De verzameling berichten bestaat niet.", berichten);
        assertEquals("Het aantal berichten is niet als verwacht.", 1, berichten.size());
        assertNotNull("De verzameling processen bestaat niet.", processen);
        assertEquals("Het aantal processen is niet als verwacht.", 1, processen.size());

        LOG.debug("Transformeren ontstaan bericht naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals("Niet alle berichten zijn OK getransformeerd", 1, brmo.getCountBerichten(null, null, BrmoFramework.BR_BRK, "RSGB_OK"));

        // test inhoud van rsgb tabellen na transformatie ontstaan bericht
        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Het aantal onroerende zaak records komt niet overeen", 1, kad_onrrnd_zk.getRowCount());
        assertEquals("Datum eerste record komt niet overeen", datumOntstaan, kad_onrrnd_zk.getValue(0, "dat_beg_geldh"));

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals("Het aantal brondocument records komt niet overeen", brondocOntstaan, brondocument.getRowCount());

        // mutatie laden
        brmo.loadFromFile(BrmoFramework.BR_BRK, BrkToStagingToRsgbIntegrationTest.class.getResource(mutatieBestand).getFile());
        LOG.debug("klaar met laden van mutatie bericht in staging DB.");

        LOG.debug("Transformeren mutatie bericht naar rsgb DB.");
        t = brmo.toRsgb();
        t.join();

        // test staging inhoud
        assertEquals("Het aantal berichten is niet als verwacht.", 2, brmo.listBerichten().size());
        assertEquals("Het aantal processen is niet als verwacht.", 2, brmo.listLaadProcessen().size());
        assertEquals("Niet alle berichten zijn OK getransformeerd", 2, brmo.getCountBerichten(null, null, BrmoFramework.BR_BRK, "RSGB_OK"));
        for (Bericht b : brmo.listBerichten()) {
            assertNotNull("Bericht is 'null'", b);
            assertNotNull("'db-xml' van bericht is 'null'", b.getDbXml());
        }

        // test inhoud van rsgb tabellen na transformatie mutatie bericht
        kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Het aantal onroerende zaak records komt niet overeen", 1, kad_onrrnd_zk.getRowCount());
        assertEquals("Datum eerste record komt niet overeen", datumMutatie, kad_onrrnd_zk.getValue(0, "dat_beg_geldh"));

        ITable kad_onrrnd_zk_archief = rsgb.createDataSet().getTable("kad_onrrnd_zk_archief");
        assertEquals("Het aantal onroerende zaak records komt niet overeen", 1, kad_onrrnd_zk_archief.getRowCount());
        assertEquals("Einddatum eerste record komt niet overeen", datumMutatie, kad_onrrnd_zk_archief.getValue(0, "datum_einde_geldh"));
        assertEquals("Begindatum eerste record komt niet overeen", datumOntstaan, kad_onrrnd_zk_archief.getValue(0, "dat_beg_geldh"));

        brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals("Het aantal brondocument records komt niet overeen", brondocMutatie, brondocument.getRowCount());
    }
}
