/*
 * Copyright (C) 2017 B3Partners B.V.
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
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import org.junit.Ignore;

/**
 *
 * testcases voor GH issue 287; opschonen en archiveren van berichten ouder dan
 * 3 maanden. Draaien met:
 * {@code mvn -Dit.test=AdvancedFunctionsActionBeanCleanupIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * voor bijvoorbeeld PostgreSQL en
 * {@code mvn -Dit.test=AdvancedFunctionsActionBeanCleanupIntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor Oracle.
 *
 * <strong>Deze test werkt niet met de jTDS driver omdat die geen
 * {@code PreparedStatement.setNull(int, int, String)} methode heeft
 * geimplementeerd.</strong>
 *
 * @author Mark Prins
 */
@RunWith(Parameterized.class)
@Category(JTDSDriverBasedFailures.class)
public class AdvancedFunctionsActionBeanCleanupIntegrationTest extends TestUtil {

    private static final Log LOG = LogFactory.getLog(AdvancedFunctionsActionBeanCleanupIntegrationTest.class);


    /**
     *
     * @return een test data {@code Object[]} met daarin
     * {@code {"sBestandsNaam", aantalBerichtenRsgbOk, aantalBerichtenToArchive, aantalBerichtenArchive, aantalBerichtenRsgbNok}}
     */
    @Parameterized.Parameters(name = "{index}: bestand: {0}, aantal berichten {1}")
    public static Collection testdata() {
        return Arrays.asList(new Object[][]{
            // {"sBestandsNaam", aantalBerichtenRsgbOk, aantalBerichtenToArchive, aantalBerichtenArchive, aantalBerichtenRsgbNok},
            {"/GH-287/staging-flat.xml", 43, 42, 6, 1},
            {"/GH-287/gh-292-staging-flat.xml", 2042, 2042, 0, 0},
            /* onderstaand staat op de ignore lijst omdat het op de build servers voor oom error zorgt, er zitten teveel berichten in.*/
            {"/GH-287/gh-292-staging-flat-4242.xml", 4242, 4242, 0, 0}
        });
    }

    private AdvancedFunctionsActionBean bean;
    private BrmoFramework brmo;
    private IDatabaseConnection staging;
    private final Lock sequential = new ReentrantLock();

    /*
     * test parameters.
     */
    private final String sBestandsNaam;
    private final long aantalBerichtenRsgbOk;
    private final long aantalBerichtenToArchive;
    private final long aantalBerichtenArchive;
    private final long aantalBerichtenRsgbNok;

    public AdvancedFunctionsActionBeanCleanupIntegrationTest(String sBestandsNaam, long aantalBerichtenRsgbOk, long aantalBerichtenToArchive, long aantalBerichtenArchive, long aantalBerichtenRsgbNok) {
        this.sBestandsNaam = sBestandsNaam;
        this.aantalBerichtenRsgbOk = aantalBerichtenRsgbOk;
        this.aantalBerichtenToArchive = aantalBerichtenToArchive;
        this.aantalBerichtenArchive = aantalBerichtenArchive;
        this.aantalBerichtenRsgbNok = aantalBerichtenRsgbNok;
    }

    @Before
    public void setUp() throws Exception {
        assumeTrue("Het bestand met staging testdata zou moeten bestaan.", AdvancedFunctionsActionBeanCleanupIntegrationTest.class.getResource(sBestandsNaam) != null);
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

        staging = new DatabaseDataSourceConnection(dsStaging);

        if (this.isMsSQL) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            dsStaging.getConnection().setAutoCommit(true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), DBPROPS.getProperty("staging.username").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegegeven");
        }
        setupJNDI(dsRsgb, dsStaging);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(AdvancedFunctionsActionBeanCleanupIntegrationTest.class.getResource(sBestandsNaam).toURI())));

        sequential.lock();

        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }

        brmo = new BrmoFramework(dsStaging, null);

        assumeTrue("Er zijn anders dan verwacht aantal RSGB_OK berichten", aantalBerichtenRsgbOk == brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, "RSGB_OK"));
        assumeTrue("Er zijn anders dan verwacht aantal ARCHIVE berichten", aantalBerichtenArchive == brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, "ARCHIVE"));
    }

    @After
    public void cleanup() throws Exception {
        // in geval van niet waar gemaakte assumptions zijn sommige objecten null
        if (brmo != null) {
            brmo.closeBrmoFramework();
        }

        if (staging != null) {
        CleanUtil.cleanSTAGING(staging);
        if (staging != null) {
            staging.close();
            }
        }
        try {
            sequential.unlock();
        } catch (IllegalMonitorStateException e) {
            // in geval van niet waar gemaakte assumptions
            LOG.debug("unlock van thread is mislukt, mogelijk niet ge-lock-ed of test overgeslagen.");
        }
    }

    @Test
    public void testCleanupBerichten() throws Exception {
        bean.cleanupBerichten(Bericht.STATUS.RSGB_OK.toString(), BrmoFramework.BR_BAG);
        assertEquals("Er zijn anders dan verwacht aantal RSGB_OK berichten",
                aantalBerichtenRsgbOk - aantalBerichtenToArchive,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, "RSGB_OK")
        );
        assertEquals("Er zijn anders dan verwacht aantal ARCHIVE berichten",
                aantalBerichtenArchive + aantalBerichtenToArchive,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, "ARCHIVE")
        );
    }

    @Test
    public void testDeleteBerichten() throws Exception {
        bean.deleteBerichten(Bericht.STATUS.ARCHIVE.toString(), BrmoFramework.BR_BAG);
        assertEquals("Er zijn nog ARCHIVE berichten",
                0, brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, "ARCHIVE"));
    }

    @Test
    @Ignore("Deze programma flow komt normaal niet voor; de GUI staat slechts 1 keuze per run toe.")
    public void testCleanupAndDeleteBerichten() throws Exception {
        LOG.debug("Archiveren 'ok' berichten");
        bean.cleanupBerichten(Bericht.STATUS.RSGB_OK.toString(), BrmoFramework.BR_BAG);

        LOG.debug("Verwijderen 'archief' berichten");
        bean.deleteBerichten(Bericht.STATUS.ARCHIVE.toString(), BrmoFramework.BR_BAG);

        assertEquals("Er zijn nog RSGB_OK berichten",
                aantalBerichtenRsgbOk - aantalBerichtenToArchive,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, Bericht.STATUS.RSGB_OK.toString())
        );
        assertEquals("Er zijn nog ARCHIVE berichten over",
                0,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, Bericht.STATUS.ARCHIVE.toString())
        );
        assertEquals("Er zijn nog RSGB_NOK berichten over",
                aantalBerichtenRsgbNok,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, Bericht.STATUS.RSGB_NOK.toString())
        );
    }
}
