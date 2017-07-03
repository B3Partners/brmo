package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * Draaien met:
 * {@code mvn -Dit.test=BrkToStagingToRsgbIntegrationTest -Dtest.onlyITs=true verify -Pmssql > target/mssql.log}
 * voor bijvoorbeeld MSSQL.
 *
 * @author Boy de Wit
 * @author mprins
 */
@RunWith(Parameterized.class)
public class BrkToStagingToRsgbIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BrkToStagingToRsgbIntegrationTest.class);

    @Parameterized.Parameters(name = "{index}: type: {0}, bestand: {1}, mutatiedatum: {4}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"type","filename", aantalBerichten, aantalProcessen, "datumEersteMutatie"},

            {"brk", "/nl/b3p/brmo/loader/xml/MUTBX01-ASN00T1660-20091119-1-singleline.xml", 1, 1, "2009-11-19"} /*
             * dit bestand zit in de DVD Proefbestanden BRK Levering oktober 2012 (Totaalstanden)
             * /mnt/v_b3p_projecten/BRMO/BRK/BRK_STUF_IMKAD/BRK/Levering(dvd)/Proefbestanden BRK Levering oktober 2012 (Totaalstanden)/20091130/
             * en staat op de git-ignore lijst omdat 't 18.5MB groot is, `grep -o KadastraalObjectSnapshot BURBX01.xml | wc -w`/2 geeft aantal berichten
             */, {"brk", "/nl/b3p/brmo/loader/xml/BURBX01-ASN00-20091130-6000015280-9100000039.zip", (63104 / 2), 1, "2009-10-31"}
        });
    }

    /**
     * test parameter.
     */
    private final String bestandNaam;
    /**
     * test parameter.
     */
    private final String bestandType;
    /**
     * test parameter.
     */
    private final long aantalBerichten;
    /**
     * test parameter.
     */
    private final long aantalProcessen;
    /**
     * test parameter.
     */
    private final String datumEersteMutatie;

    private BrmoFramework brmo;

    // dbunit
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;

    private final Lock sequential = new ReentrantLock(true);

    public BrkToStagingToRsgbIntegrationTest(String bestandType, String bestandNaam, long aantalBerichten, long aantalProcessen, String datumEersteMutatie) {
        this.bestandType = bestandType;
        this.bestandNaam = bestandNaam;
        this.aantalBerichten = aantalBerichten;
        this.aantalProcessen = aantalProcessen;
        this.datumEersteMutatie = datumEersteMutatie;
    }

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

        assumeTrue("Er zijn geen STAGING_OK berichten", 0l == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));
        assumeTrue("Er zijn geen STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"));
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
    public void testBrkXMLToStaging() throws Exception {
        assumeNotNull("Het test bestand moet er zijn.", BrkToStagingToRsgbIntegrationTest.class.getResource(bestandNaam));

        brmo.loadFromFile(bestandType, BrkToStagingToRsgbIntegrationTest.class.getResource(bestandNaam).getFile());
        LOG.debug("klaar met laden van berichten in staging DB.");

        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull("De verzameling berichten bestaat niet.", berichten);
        assertEquals("Het aantal berichten is niet als verwacht.", aantalBerichten, berichten.size());
        assertNotNull("De verzameling processen bestaat niet.", processen);
        assertEquals("Het aantal processen is niet als verwacht.", aantalProcessen, processen.size());

        LOG.debug("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals("Niet alle berichten zijn OK getransformeerd", aantalBerichten, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        berichten = brmo.listBerichten();
        for (Bericht b : berichten) {
            assertNotNull("Bericht is 'null'", b);
            assertNotNull("'db-xml' van bericht is 'null'", b.getDbXml());
        }

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Het aantal on", aantalBerichten, kad_onrrnd_zk.getRowCount());
        assertEquals("Datum eerste record komt niet overeen", datumEersteMutatie, kad_onrrnd_zk.getValue(0, "dat_beg_geldh"));
    }
}
