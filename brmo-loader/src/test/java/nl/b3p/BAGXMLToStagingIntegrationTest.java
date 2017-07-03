package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
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
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Draaien met:
 * {@code mvn -Dit.test=BAGXMLToStagingIntegrationTest -Dtest.onlyITs=true verify -Pmssql > target/mssql.log}
 * voor bijvoorbeeld MSSQL.
 *
 * @author Boy de Wit
 * @author mprins
 */
@RunWith(Parameterized.class)
public class BAGXMLToStagingIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Parameterized.Parameters(name = "{index}: type: {0}, bestand: {1}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"type","filename", aantalBerichten, aantalLaadProcessen},
            {"bag", "/nl/b3p/brmo/loader/xml/0197LIG01072014-01072014-000001.xml", 0, 1},
            {"bag", "/nl/b3p/brmo/loader/xml/0197STA01072014-01072014-000001.xml", 2, 1},
            {"bag", "/nl/b3p/brmo/loader/xml/9999MUT02012015-03012015.zip", 25718, 6},
            {/*bestand heeft 4 berichten voor object, maar we houden alleen de laatste mutatie in staging*/"bag", "/GH-275/OPR-1884300000000464.xml", 1, 1}
        });
    }

    private static final Log LOG = LogFactory.getLog(BAGXMLToStagingIntegrationTest.class);

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

    private IDatabaseConnection staging;
    private final Lock sequential = new ReentrantLock();
    private BrmoFramework brmo;

    public BAGXMLToStagingIntegrationTest(String bestandType, String bestandNaam, long aantalBerichten, long aantalProcessen) {
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

        brmo = new BrmoFramework(dsStaging, null, null);
        staging = new DatabaseDataSourceConnection(dsStaging);

        if (this.isMsSQL) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(BAGXMLToStagingIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        assumeTrue("Er zijn geen STAGING_OK berichten", 0l == brmo.getCountBerichten(null, null, "bag", "STAGING_OK"));
        assumeTrue("Er zijn geen STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, "bag", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        CleanUtil.cleanSTAGING(staging);
        staging.close();
        sequential.unlock();
    }

    @Test
    public void testBagStandToStaging() throws BrmoException {
        try {
            brmo.loadFromFile(bestandType, BAGXMLToStagingIntegrationTest.class.getResource(bestandNaam).getFile());
        } catch (BrmoLeegBestandException blbe) {
            LOG.debug("Er is een bestand zonder berichten geladen (kan voorkomen).");
        }

        assertEquals("Verwacht aantal berichten", aantalBerichten, brmo.getCountBerichten(null, null, bestandType, "STAGING_OK"));
        assertEquals("Verwacht aantal laadprocessen", aantalProcessen, brmo.getCountLaadProcessen(null, null, bestandType, "STAGING_OK"));
    }
}
