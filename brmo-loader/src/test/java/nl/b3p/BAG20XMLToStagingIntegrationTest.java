package nl.b3p;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * Draaien met:
 * {@code mvn clean -pl :brmo-loader -Dit.test=BAG20XMLToStagingIntegrationTest -Dtest.onlyITs=true verify -Pmssql > /tmp/mssql.log}
 * voor bijvoorbeeld MSSQL of
 * {@code mvn clean -pl :brmo-loader -Dit.test=BAG20XMLToStagingIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > /tmp/pgsql.log}.
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
public class BAG20XMLToStagingIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BAG20XMLToStagingIntegrationTest.class);
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
    private final Lock sequential = new ReentrantLock();
    private IDatabaseConnection staging;
    private BrmoFramework brmo;

    public BAG20XMLToStagingIntegrationTest(String bestandType, String bestandNaam, long aantalBerichten, long aantalProcessen) {
        this.bestandType = bestandType;
        this.bestandNaam = bestandNaam;
        this.aantalBerichten = aantalBerichten;
        this.aantalProcessen = aantalProcessen;
    }

    @Parameterized.Parameters(name = "{index}: type: {0}, bestand: {1}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
                // {"type","filename", aantalBerichten, aantalLaadProcessen},
                {"bag20", "/bag-2.0/WPL.xml", 1, 1},
                {"bag20", "/bag-2.0/PND.xml", 1, 1},
                {"bag20", "/bag-2.0/OPR.xml", 1, 1},
                {"bag20", "/bag-2.0/NUM.xml", 1, 1},
                {"bag20", "/bag-2.0/LIG.xml", 1, 1},
                {"bag20", "/bag-2.0/STA.xml", 1, 1},
                {"bag20", "/bag-2.0/VBO.xml", 1, 1},
                //{/*bestand heeft 4 berichten voor object, maar we houden alleen de laatste mutatie in staging*/"bag", "/GH-275/OPR-1884300000000464.xml", 1, 1}
        });
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
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(BAG20XMLToStagingIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        assumeTrue("Er zijn geen STAGING_OK berichten", 0l == brmo.getCountBerichten(null, null, "bag", "STAGING_OK"));
        assumeTrue("Er zijn geen STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, "bag", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        CleanUtil.cleanSTAGING(staging, false);
        staging.close();
        sequential.unlock();
    }

    @Test
    public void testBagStandToStaging() throws BrmoException {
        try {
            brmo.loadFromFile(bestandType, BAG20XMLToStagingIntegrationTest.class.getResource(bestandNaam).getFile(), null);
        } catch (BrmoLeegBestandException blbe) {
            LOG.debug("Er is een bestand zonder berichten geladen (kan voorkomen).");
        }

        assertEquals("Verwacht aantal berichten", aantalBerichten, brmo.getCountBerichten(null, null, bestandType, "STAGING_OK"));
        assertEquals("Verwacht aantal laadprocessen", aantalProcessen, brmo.getCountLaadProcessen(null, null, bestandType, "STAGING_OK"));
    }
}
