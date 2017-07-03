package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.dbcp.BasicDataSource;
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
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Draaien met:
 * {@code mvn -Dit.test=BerichtenFilterSqlIntegrationTest -Dtest.onlyITs=true verify -Pmssql > target/mssql.log}
 * voor bijvoorbeeld MSSQL.
 *
 * @author Boy de Wit
 * @author mprins
 */
public class BerichtenFilterSqlIntegrationTest extends AbstractDatabaseIntegrationTest {

    private String filterSoort;
    private String filterStatus;

    private int page = 0;
    private int start = 0;
    private int limit = 10;
    private String sort = null;
    private String dir = null;

    private IDatabaseConnection staging;
    private final Lock sequential = new ReentrantLock();
    private BrmoFramework brmo;

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
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(BerichtenFilterSqlIntegrationTest.class.getResource("/staging-6_laadprocessen_met_elk_10_bag_berichten-flat.xml").toURI())));

        sequential.lock();

        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }

        assumeTrue("Er zijn geen STAGING_OK berichten", 60l == brmo.getCountBerichten(null, null, "brk,bag,nhr", "STAGING_OK"));
        assumeTrue("Er zijn BAG geen STAGING_OK laadprocessen", 6l == brmo.getCountLaadProcessen(null, null, "bag", "STAGING_OK"));
        assumeTrue("Er zijn BRK STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        DatabaseOperation.DELETE_ALL.execute(staging, new DefaultDataSet(new DefaultTable[]{
            new DefaultTable("laadproces"),
            new DefaultTable("bericht")
        }));
        staging.close();

        sequential.unlock();
    }

    @Test
    public void emptyStagingDb() throws BrmoException {
        brmo.emptyStagingDb();

        assertTrue("Er zijn STAGING_OK berichten", 0l == brmo.getCountBerichten(null, null, "brk,bag,nhr", "STAGING_OK"));
        assertTrue("Er zijn STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, "bag", "STAGING_OK"));
    }

    @Test
    public void testStatus() throws BrmoException {
        filterStatus = "STAGING_OK";
        sort = "status";

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir, filterSoort, filterStatus);

        assertNotNull("Er moet een aantal bag berichten zijn.", berichten);
        assertTrue("Het aantal bag berichten is groter dan 0", berichten.size() > 0);
    }

    @Test
    public void testSoort() throws BrmoException {
        filterSoort = "bag";
        sort = "soort";

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir, filterSoort, filterStatus);
        assertNotNull("Er moet een aantal bag berichten zijn.", berichten);
    }

    @Test
    public void testOrderByDesc() throws BrmoException {
        sort = "id";
        dir = "DESC";

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir, filterSoort, filterStatus);
        long id1 = berichten.get(0).getId();
        long id2 = berichten.get(1).getId();

        assertNotNull("Er moet een aantal berichten zijn.", berichten);
        assertTrue("De DESC sortering moet kloppen", id1 > id2);
    }

    @Test
    public void testOrderByAsc() throws BrmoException {
        sort = "id";
        dir = "ASC";

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir, filterSoort, filterStatus);
        long id1 = berichten.get(0).getId();
        long id2 = berichten.get(1).getId();

        assertNotNull("Er moet een aantal berichten zijn.", berichten);
        assertTrue("De ASC sortering moet kloppen", id1 < id2);
    }

    @Test
    public void testPaging() throws BrmoException {
        page = 0;
        start = 0;
        limit = 3;

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir, filterSoort, filterStatus);
        assertNotNull("Er moet een aantal berichten zijn.", berichten);
        assertEquals("Het aantal in de selectie", limit, berichten.size());
    }
}
