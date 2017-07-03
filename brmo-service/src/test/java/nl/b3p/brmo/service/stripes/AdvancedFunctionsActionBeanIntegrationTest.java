/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.service.stripes;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.service.testutil.TestUtil;
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
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * testcases voor GH issue 260; herhalen van brk verwijder berichten. Draaien
 * met:
 * {@code mvn -Dit.test=AdvancedFunctionsActionBeanIntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=AdvancedFunctionsActionBeanIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * voor PostgreSQL.
 *
 * <strong>Deze test werkt niet met de jTDS driver omdat die geen
 * {@code PreparedStatement.setNull(int, int, String)} methode heeft
 * geimplementeerd.</strong>
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
@Category(JTDSDriverBasedFailures.class)
public class AdvancedFunctionsActionBeanIntegrationTest extends TestUtil {

    private static final Log LOG = LogFactory.getLog(AdvancedFunctionsActionBeanIntegrationTest.class);

    @Parameterized.Parameters(name = "{index}: verwerken bestand: {0}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"sBestandsNaam", aantalProcessen, aantalBerichten, rBestandsNaam},
            {"/gh-issue-260/staging-flat.xml", 2, 2, "/gh-issue-260/rsgb-spook_kad_onrrnd_zk-flat.xml"},
            {"/gh-issue-260/staging-flat-4.xml", 4, 4, "/gh-issue-260/rsgb-spook_kad_onrrnd_zk-flat-4.xml"}
        });
    }

    private AdvancedFunctionsActionBean bean;
    private BrmoFramework brmo;
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;

    private final Lock sequential = new ReentrantLock();

    /*
     * test parameters.
     */
    private final String sBestandsNaam;
    private final String rBestandsNaam;
    private final long aantalBerichten;
    private final long aantalProcessen;

    public AdvancedFunctionsActionBeanIntegrationTest(String sBestandsNaam, long aantalBerichten, long aantalProcessen, String rBestandsNaam) {
        this.sBestandsNaam = sBestandsNaam;
        this.rBestandsNaam = rBestandsNaam;
        this.aantalBerichten = aantalBerichten;
        this.aantalProcessen = aantalProcessen;
    }

    @Before
    public void setUp() throws Exception {
        assumeTrue("Het bestand met staging testdata zou moeten bestaan.", AdvancedFunctionsActionBeanIntegrationTest.class.getResource(sBestandsNaam) != null);
        assumeTrue("Het bestand met rsgb testdata zou moeten bestaan.", AdvancedFunctionsActionBeanIntegrationTest.class.getResource(rBestandsNaam) != null);
        assumeTrue("Deze test werkt niet met de jTDS driver omdat die geen PreparedStatement.setNull(int, int, String) methode heeft geimplementeerd.",
                !this.isMsSQL);

        bean = new AdvancedFunctionsActionBean();

        BasicDataSource dsStaging = new BasicDataSource();
        dsStaging.setUrl(DBPROPS.getProperty("staging.url"));
        dsStaging.setUsername(DBPROPS.getProperty("staging.username"));
        dsStaging.setPassword(DBPROPS.getProperty("staging.password"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        BasicDataSource dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(DBPROPS.getProperty("rsgb.url"));
        dsRsgb.setUsername(DBPROPS.getProperty("rsgb.username"));
        dsRsgb.setPassword(DBPROPS.getProperty("rsgb.password"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);

        rsgb = new DatabaseDataSourceConnection(dsRsgb);
        staging = new DatabaseDataSourceConnection(dsStaging);

        if (this.isMsSQL) {
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), DBPROPS.getProperty("rsgb.username").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), DBPROPS.getProperty("staging.username").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegegeven");
        }
        setupJNDI(dsRsgb, dsStaging);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(AdvancedFunctionsActionBeanIntegrationTest.class.getResource(sBestandsNaam).toURI())));
        IDataSet rsgbDataSet = fxdb.build(new FileInputStream(new File(AdvancedFunctionsActionBeanIntegrationTest.class.getResource(rBestandsNaam).toURI())));

        sequential.lock();

        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
            InsertIdentityOperation.CLEAN_INSERT.execute(rsgb, rsgbDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
            DatabaseOperation.CLEAN_INSERT.execute(rsgb, rsgbDataSet);
        }

        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);

        assumeTrue("Er zijn x RSGB_OK berichten", aantalBerichten == brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assumeTrue("Er zijn x STAGING_OK laadprocessen", aantalProcessen == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        if (brmo != null) {
            // in geval van niet waar gemaakte assumptions
            brmo.closeBrmoFramework();
        }
        if (rsgb != null) {
            CleanUtil.cleanRSGB(rsgb);
            rsgb.close();
        }
        if (staging != null) {
            staging.close();
        }

        try {
            sequential.unlock();
        } catch (IllegalMonitorStateException e) {
            // in geval van niet waar gemaakte assumptions
            LOG.debug("unlock van thread is mislukt, mogelijk niet ge-lock-ed of test overgeslagen.");
        }
    }

    @Test
    public void testReplayBRKVerwijderBerichten() throws Exception {
        final IDataSet rds = rsgb.createDataSet();

        assertEquals("Er is een spook record in de kad_onrrnd_zk tabel", 1, rds.getTable("kad_onrrnd_zk").getRowCount());
        assertEquals("De perceel tabel is leeg", 0, rds.getTable("kad_perceel").getRowCount());
        assertEquals("De kad_onrrnd_zk_archief komt een record te kort", aantalBerichten - (1 + 1), rds.getTable("kad_onrrnd_zk_archief").getRowCount());
        assertTrue("Er is minstens een perceel in kad_perceel_archief", 0 < rds.getTable("kad_perceel_archief").getRowCount());

        bean.replayBRKVerwijderBerichten(BrmoFramework.BR_BRK, Bericht.STATUS.RSGB_OK.toString());

        assertEquals("Er zijn geen spook records in de kad_onrrnd_zk tabel", 0, rds.getTable("kad_onrrnd_zk").getRowCount());
        assertTrue("De kad_onrrnd_zk_archief tabel is niet leeg", 0 < rds.getTable("kad_onrrnd_zk_archief").getRowCount());
        assertEquals("Er zit voor ieder bericht met perceel in record in kad_perceel_archief",
                aantalBerichten - 1,
                rds.getTable("kad_perceel_archief").getRowCount()
        );

        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("Alle berichten hebben status RSGB_OK", aantalBerichten, bericht.getRowCount());
    }
}
