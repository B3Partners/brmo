package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.test.util.database.JTDSDriverBasedFailures;
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
import org.junit.experimental.categories.Category;

/**
 * Testcases voor mantis-10315; ontbrekende rechthebbende bij KAD_RECHT. <br>
 * Draaien met:
 * {@code mvn -Dit.test=Mantis10315IntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=Mantis10315IntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}.
 * <strong>NB.</strong> werkt niet op mssql, althans niet met de jTDS driver
 * omdat die geen JtdsPreparedStatement#setNull() methode heeft.
 *
 * @author mprins
 */
@Category(JTDSDriverBasedFailures.class)
public class Mantis10315IntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Mantis10315IntegrationTest.class);

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

        IDataSet stagingDataSet = new XmlDataSet(new FileInputStream(new File(Mantis6380IntegrationTest.class.getResource("/mantis10315/staging.xml").toURI())));

        sequential.lock();

        if (this.isMsSQL) {
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }
        brmo = new BrmoFramework(dsStaging, dsRsgb);

        // skip als de bron data er niet is
        assumeTrue("Er zijn geen 1 STAGING_OK berichten", 1l == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));
        assumeTrue("Er zijn geen 1 STAGING_OK laadproces", 1l == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanRSGB_BRK(rsgb, true);
        rsgb.close();

        CleanUtil.cleanSTAGING(staging);
        staging.close();

        sequential.unlock();
    }

    /**
     * transformeer alle berichten en test of dat correct is gedaan.
     *
     * @throws Exception if any
     */
    @Test
    public void testTransformBerichten() throws Exception {
        brmo.setOrderBerichten(true);
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals("Niet alle berichten zijn OK getransformeerd", 0l, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));
        assertEquals("Niet alle berichten zijn OK getransformeerd", 1l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assertEquals("Er zijn berichten met status RSGB_NOK", 0l, brmo.getCountBerichten(null, null, "brk", "RSGB_NOK"));

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertTrue("Er zijn geen brondocumenten", brondocument.getRowCount() > 0);

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Aantal actuele onroerende zaken is incorrect", 1, kad_onrrnd_zk.getRowCount());

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Aantal actuele percelen is incorrect", 1, kad_perceel.getRowCount());
        assertEquals("Perceel identif is incorrect", "400376170000", kad_perceel.getValue(0, "sc_kad_identif").toString());

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals("Aantal subjecten klopt niet", 1, subject.getRowCount());

        ITable ingeschr_niet_nat_prs = rsgb.createDataSet().getTable("ingeschr_niet_nat_prs");
        assertEquals("Aantal ingeschr_niet_nat_prs klopt niet", 1, ingeschr_niet_nat_prs.getRowCount());

        ITable zak_recht = rsgb.createDataSet().getTable("zak_recht");
        assertEquals("Aantal zakelijke rechten klopt niet", 1, zak_recht.getRowCount());
        // test data van rij 1
        assertEquals("NL.KAD.ZakelijkRecht.AKR1.487436", zak_recht.getValue(0, "kadaster_identif").toString());
        assertNull("Teller moet 'null' zijn", zak_recht.getValue(0, "ar_teller"));
        assertNull("Noemer moet 'null' zijn", zak_recht.getValue(0, "ar_noemer"));
        assertNotNull("Rechthebbende is 'null'", zak_recht.getValue(0, "fk_8pes_sc_identif"));
        assertEquals("Rechthebbende is niet correct", "NL.KAD.Persoon.133947417", zak_recht.getValue(0, "fk_8pes_sc_identif").toString());
    }

}
