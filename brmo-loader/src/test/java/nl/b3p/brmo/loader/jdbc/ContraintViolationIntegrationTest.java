/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.brmo.loader.jdbc;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.AbstractDatabaseIntegrationTest;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.ITable;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Draaien met:
 * {@code mvn -Dit.test=ContraintViolationIntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor Oracle of
 * {@code mvn -Dit.test=ContraintViolationIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * voor PostgreSQL.
 *
 * @author mprins
 */
public class ContraintViolationIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(ContraintViolationIntegrationTest.class);

    private final String bestandNaam = "/GH-275/OPR-1884300000000464.xml";
    private final String bestandType = "bag";
    private final int aantalProcessen = 1;
    private final int aantalBerichten = 1;

    private BrmoFramework brmo;
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;
    private final Lock sequential = new ReentrantLock();

    public ContraintViolationIntegrationTest() {
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
        brmo = new BrmoFramework(dsStaging, dsRsgb);

        if (this.isMsSQL) {
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), params.getProperty("rsgb.user").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegegeven.");
        }

        assumeNotNull("Het bestand met testdata zou moeten bestaan.", ContraintViolationIntegrationTest.class.getResource(bestandNaam));

        sequential.lock();
        CleanUtil.cleanSTAGING(staging);

        assumeTrue("Er zijn geen STAGING_OK berichten", 0l == brmo.getCountBerichten(null, null, "bag", "STAGING_OK"));
        assumeTrue("Er zijn geen STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, "bag", "STAGING_OK"));

    }

    @After
    public void cleanup() throws Exception {
        if (brmo != null) {
            brmo.closeBrmoFramework();
        }

        if (rsgb != null) {
            CleanUtil.cleanRSGB_BAG(rsgb, true);
            rsgb.close();
        }

        if (staging != null) {
            CleanUtil.cleanSTAGING(staging);
            DatabaseOperation.DELETE_ALL.execute(staging, new DefaultDataSet(new DefaultTable[]{
                new DefaultTable("job")}
            ));
            staging.close();
        }

        try {
            sequential.unlock();
        } catch (IllegalMonitorStateException e) {
            // in geval van niet waar gemaakte assumptions
            LOG.debug("unlock van thread is mislukt, mogelijk niet ge-lock-ed");
        }
    }

    /**
     * test voor foreign key constraint violation.
     *
     * @throws Exception if any
     */
    @Test
    public void testForeignKeyConstraintViolation() throws Exception {
        brmo.loadFromFile(bestandType, ContraintViolationIntegrationTest.class.getResource(bestandNaam).getFile());
        LOG.debug("klaar met laden van berichten in staging DB.");

        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull("De verzameling berichten bestaat niet.", berichten);
        assertEquals("Het aantal berichten is niet als verwacht.", aantalBerichten, berichten.size());
        assertNotNull("De verzameling processen bestaat niet.", processen);
        assertEquals("Het aantal processen is niet als verwacht.", aantalProcessen, processen.size());

        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("Het aantal berichten klopt niet", aantalBerichten, bericht.getRowCount());
        assertNull("DB xml is niet null (want nog niet getransformeerd)", bericht.getValue(0, "db_xml"));

        LOG.debug("Transformeren berichten naar rsgb DB.");
        brmo.setOrderBerichten(true);
        Thread t = brmo.toRsgb();
        t.join();
        LOG.debug("Klaar met transformeren berichten naar rsgb DB.");

        assertEquals("Niet alle berichten zijn naar RSGB_BAG_NOK getransformeerd", aantalBerichten, brmo.getCountBerichten(null, null, "bag", "RSGB_BAG_NOK"));

        ITable gem_openb_rmte = rsgb.createDataSet().getTable("gem_openb_rmte");
        assertEquals("Het aantal openbare ruimten klopt niet.", 0, gem_openb_rmte.getRowCount());

        // test voor de gerelateerde woonplaats
        ITable wnplts = rsgb.createQueryTable("wnplts", "select * from wnplts where identif='3042'");
        assertEquals("Het aantal openbare ruimten klopt niet.", 0, wnplts.getRowCount());

    }
}
