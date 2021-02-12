package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.dbcp2.BasicDataSource;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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

    @BeforeEach
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

        assumeTrue(60l == brmo.getCountBerichten(null, null, "brk,bag,nhr", "STAGING_OK"),
                "Er zijn geen STAGING_OK berichten");
        assumeTrue(6l == brmo.getCountLaadProcessen(null, null, "bag", "STAGING_OK"),
                "Er zijn BAG geen STAGING_OK laadprocessen");
        assumeTrue(0l == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"),
                "Er zijn BRK STAGING_OK laadprocessen");
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        CleanUtil.cleanSTAGING(staging, false);
        staging.close();
        sequential.unlock();
    }

    @Test
    public void emptyStagingDb() throws BrmoException {
        brmo.emptyStagingDb();

        assertTrue(0l == brmo.getCountBerichten(null, null, "brk,bag,nhr", "STAGING_OK"),
                "Er zijn STAGING_OK berichten");
        assertTrue(0l == brmo.getCountLaadProcessen(null, null, "bag", "STAGING_OK"),
                "Er zijn STAGING_OK laadprocessen");
    }

    @Test
    public void testStatus() throws BrmoException {
        filterStatus = "STAGING_OK";
        sort = "status";

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir, filterSoort, filterStatus);

        assertNotNull(berichten, "Er moet een aantal bag berichten zijn.");
        assertTrue(berichten.size() > 0, "Het aantal bag berichten is groter dan 0");
    }

    @Test
    public void testSoort() throws BrmoException {
        filterSoort = "bag";
        sort = "soort";

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir, filterSoort, filterStatus);
        assertNotNull(berichten, "Er moet een aantal bag berichten zijn.");
    }

    @Test
    public void testOrderByDesc() throws BrmoException {
        sort = "id";
        dir = "DESC";

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir, filterSoort, filterStatus);
        long id1 = berichten.get(0).getId();
        long id2 = berichten.get(1).getId();

        assertNotNull(berichten, "Er moet een aantal berichten zijn.");
        assertTrue(id1 > id2, "De DESC sortering moet kloppen");
    }

    @Test
    public void testOrderByAsc() throws BrmoException {
        sort = "id";
        dir = "ASC";

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir, filterSoort, filterStatus);
        long id1 = berichten.get(0).getId();
        long id2 = berichten.get(1).getId();

        assertNotNull(berichten, "Er moet een aantal berichten zijn.");
        assertTrue(id1 < id2, "De ASC sortering moet kloppen");
    }

    @Test
    public void testPaging() throws BrmoException {
        page = 0;
        start = 0;
        limit = 3;

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir, filterSoort, filterStatus);
        assertNotNull(berichten, "Er moet een aantal berichten zijn.");
        assertEquals(limit, berichten.size(), "Het aantal in de selectie");
    }
}
