package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author meine
 */
/**
 * Testcases voor creeeren van zak_recht_archief tabel

 * Draaien met:
 * {@code mvn -Dit.test=ZakRechtArchiefIntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=ZakRechtArchiefIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * of
 * {@code mvn -Dit.test=ZakRechtArchiefIntegrationTest -Dtest.onlyITs=true verify -Pmssql > target/mssql.log}.
 *
 * @author meine
 */

public class ZakRechtArchiefIntegrationTest extends AbstractDatabaseIntegrationTest{

    private static final Log LOG = LogFactory.getLog(ZakRechtArchiefIntegrationTest.class);

    private BrmoFramework brmo;

    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;

    private final Lock sequential = new ReentrantLock();

    @Override
    @Before
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
            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), params.getProperty("rsgb.user").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            // we hebben alleen nog postgres over
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }
        IDataSet stagingDataSet = new XmlDataSet(new FileInputStream(new File(Mantis6380IntegrationTest.class.getResource("/zak_recht_archief/staging.xml").toURI())));

        sequential.lock();

        if (this.isMsSQL) {
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }
        brmo = new BrmoFramework(dsStaging, dsRsgb);
        assumeTrue("Er zijn geen 6 STAGING_OK berichten", 6l == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));
        assumeTrue("Er zijn geen 1 STAGING_OK laadproces", 1l == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"));
    }

    
    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        
        CleanUtil.cleanRSGB_BRK(rsgb, true);
        rsgb.close();

        CleanUtil.cleanSTAGING(staging, false);
        staging.close();

        sequential.unlock();
    }

    /**
     * transformeer alle berichten en test of dat correct is gedaan.
     *
     * @throws Exception if any
     */
    @Test
    public void transformeerberichten() throws Exception {
        brmo.setOrderBerichten(true);
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals("Niet alle berichten zijn OK getransformeerd", 0l, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));
        assertEquals("Niet alle berichten zijn OK getransformeerd", 6l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assertEquals("Er zijn berichten met status RSGB_NOK", 0l, brmo.getCountBerichten(null, null, "brk", "RSGB_NOK"));

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertTrue("Er zijn geen brondocumenten", brondocument.getRowCount() > 0);

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Aantal actuele onroerende zaken is incorrect", 1, kad_onrrnd_zk.getRowCount());

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Aantal actuele percelen is incorrect", 1, kad_perceel.getRowCount());
        assertEquals("Perceel identif is incorrect", "20930170970000", kad_perceel.getValue(0, "sc_kad_identif").toString());

        // test of de records in de tabellen de juiste clazz hebben en volledig zijn
        String[] tables = {"prs", "nat_prs"};
        ITable subj = rsgb.createDataSet().getTable("subject");
        assertEquals("Aantal rijen klopt niet in tabel " + "subject", 5, subj.getRowCount());
        ITable prs = rsgb.createDataSet().getTable("prs");
        assertEquals("Aantal rijen klopt niet in tabel " + "prs", 5, prs.getRowCount());
        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals("Aantal rijen klopt niet in tabel " + "nat_prs", 4, nat_prs.getRowCount());
        // ingeschr_nat_prs
        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals("Aantal ingeschr_nat_prs klopt niet.", 4, ingeschr_nat_prs.getRowCount());
        for (int row=0;row < 4; row++) {
            assertNull("'clazz' is niet null voor rij "+ row + 1 +", tabel ingeschr_nat_prs", ingeschr_nat_prs.getValue(row, "clazz"));
        }
        // niet_ingezetene
        ITable niet_ingezetene = rsgb.createDataSet().getTable("niet_ingezetene");
        assertEquals("Aantal niet_ingezetene klopt niet", 0, niet_ingezetene.getRowCount());
        
        assertEquals("Aantal niet_ingezetene klopt niet", 0, niet_ingezetene.getRowCount());
        // zak_recht heeft ingangsdatum_recht
        ITable zak_recht = rsgb.createDataSet().getTable("zak_recht");
        assertEquals("Aantal zakelijk rechten klopt niet", 4, zak_recht.getRowCount());
        for (int i = 0; i < 4; i++) {
            assertNotNull("Ingangsdatum recht is niet gevuld", zak_recht.getValue(i, "ingangsdatum_recht"));
        }
        
        ITable zak_recht_archief = rsgb.createDataSet().getTable("zak_recht_archief");
        assertEquals("Aantal zakelijk rechten klopt niet", 19, zak_recht_archief.getRowCount());
        
        for (int i = 0; i < zak_recht_archief.getRowCount(); i++) {
            assertNotNull("Ingangsdatum recht is niet gevuld", zak_recht_archief.getValue(i, "ingangsdatum_recht"));
            assertNotNull("Eindd_recht is niet gevuld", zak_recht_archief.getValue(i, "eindd_recht"));
        }
    }
}
