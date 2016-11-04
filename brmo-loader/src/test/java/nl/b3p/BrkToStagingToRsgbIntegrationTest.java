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
import nl.b3p.brmo.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.OracleDataTypeFactory;
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
 * @autor mprins
 */
@RunWith(Parameterized.class)
public class BrkToStagingToRsgbIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BrkToStagingToRsgbIntegrationTest.class);

    @Parameterized.Parameters(name = "{index}: type: {0}, bestand: {1}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"type","filename", aantalBerichten, aantalLaadProcessen},

            // dit bestand zit in de DVD Proefbestanden BRK Levering oktober 2012 (Totaalstanden)
            // /mnt/v_b3p_projecten/BRMO/BRK/BRK_STUF_IMKAD/BRK/Levering(dvd)/Proefbestanden BRK Levering oktober 2012 (Totaalstanden)/20091130/
            // en staat op de ignore lijst omdat 't 18.5MB groot is, grep -o KadastraalObjectSnapshot BURBX01.xml | wc -w geeft aantal berichten
            // {"brk", "/nl/b3p/brmo/loader/xml/BURBX01-ASN00-20091130-6000015280-9100000039.zip", 63104 / 2, 1},
            {"brk", "/nl/b3p/brmo/loader/xml/MUTBX01-ASN00T1660-20091119-1-singleline.xml", 1, 1}
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

    private BrmoFramework brmo;
    // dbunit
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;

    private final Lock sequential = new ReentrantLock();

    public BrkToStagingToRsgbIntegrationTest(String bestandType, String bestandNaam, long aantalBerichten, long aantalProcessen) {
        this.bestandType = bestandType;
        this.bestandNaam = bestandNaam;
        this.aantalBerichten = aantalBerichten;
        this.aantalProcessen = aantalProcessen;
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

        rsgb = new DatabaseDataSourceConnection(dsRsgb);
        staging = new DatabaseDataSourceConnection(dsStaging);
        if (this.isMsSQL) {
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()));
            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()));
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new OracleDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new OracleDataTypeFactory());
        } else if (this.isPostgis) {
            // we hebben alleen nog postgres over
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        brmo = new BrmoFramework(dsStaging, dsRsgb);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(BAGXMLToStagingIntegrationTest.class.getResource("/staging-6_laadprocessen_met_elk_10_bag_berichten-flat.xml").toURI())));

        sequential.lock();

        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }

        assumeTrue("Er zijn geen STAGING_OK berichten", 0l == brmo.getCountBerichten(null, null, "brk,bag,nhr", "STAGING_OK"));
        assumeTrue("Er zijn geen STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, "brk,bag,nhr", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        DatabaseOperation.DELETE_ALL.execute(staging, new DefaultDataSet(new DefaultTable[]{
            new DefaultTable("laadproces"),
            new DefaultTable("bericht"),
            new DefaultTable("job")
        }));
        rsgb.close();
        staging.close();

        sequential.unlock();
    }

    @Test
    public void testBrkXMLToStaging() throws Exception {
        assumeNotNull("Het bestand moet er zijn.", BrkToStagingToRsgbIntegrationTest.class.getResource(bestandNaam));

        brmo.loadFromFile(bestandType, BrkToStagingToRsgbIntegrationTest.class.getResource(bestandNaam).getFile());

        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();

        assertNotNull("verzameling berichten bestaat", berichten);
        assertEquals(aantalBerichten, processen.size());
        assertNotNull("verzameling processen bestaat", processen);
        assertEquals(aantalProcessen, processen.size());

        Thread t = brmo.toRsgb();
        t.join();

        assertEquals("Alle berichten zijn OK getransformeerd", aantalBerichten, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));

        berichten = brmo.listBerichten();
        for (Bericht b : berichten) {
            assertNotNull("bericht is niet 'null'", b);
            assertNotNull("db-xml van bericht is niet 'null'", b.getDbXml());
        }
    }
}
