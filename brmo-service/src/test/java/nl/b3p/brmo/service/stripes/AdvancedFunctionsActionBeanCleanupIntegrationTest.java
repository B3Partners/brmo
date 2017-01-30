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
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.jdbc.OracleConnectionUnwrapper;
import nl.b3p.brmo.service.testutil.TestUtil;
import nl.b3p.brmo.test.util.database.JTDSDriverBasedFailures;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
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

/**
 *
 * testcases voor GH issue 287; opschonen en archiveren van berichten ouder dan
 * 3 maanden. Draaien met:
 * {@code mvn -Dit.test=AdvancedFunctionsActionBeanCleanupIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * voor bijvoorbeeld PostgreSQL.
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

    private static boolean haveSetupJNDI = false;

    /**
     *
     * @return een test data {@code Object[]} met daarin
     * {@code {"sBestandsNaam", aantalBerichtenRsgbOk, aantalBerichtenToArchive, aantalBerichtenArchive}}
     */
    @Parameterized.Parameters(name = "{index}: bestand: {0}")
    public static Collection testdata() {
        return Arrays.asList(new Object[][]{
            // {"sBestandsNaam", aantalBerichtenRsgbOk, aantalBerichtenToArchive, aantalBerichtenArchive, aantalBerichtenRsgbNok},
            {"/GH-287/staging-flat.xml", 43, 42, 6, 1}
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
        assumeTrue("Het bestand met staging testdata zou moeten bestaan.", AdvancedFunctionsActionBeanIntegrationTest.class.getResource(sBestandsNaam) != null);
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
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), DBPROPS.getProperty("staging.username").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegegeven");
        }

        if (!haveSetupJNDI) {
            try {
                System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
                System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
                InitialContext ic = new InitialContext();
                ic.createSubcontext("java:");
                ic.createSubcontext("java:comp");
                ic.createSubcontext("java:comp/env");
                ic.createSubcontext("java:comp/env/jdbc");
                ic.createSubcontext("java:comp/env/jdbc/brmo");
                ic.bind("java:comp/env/jdbc/brmo/rsgb", dsRsgb);
                ic.bind("java:comp/env/jdbc/brmo/staging", dsStaging);
                haveSetupJNDI = true;
            } catch (NamingException ex) {
                LOG.error("Opzetten van datasource jndi is mislukt", ex);
            }
        }

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(AdvancedFunctionsActionBeanIntegrationTest.class.getResource(sBestandsNaam).toURI())));

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
        if (brmo != null) {
            // in geval van niet waar gemaakte assumptions
            brmo.closeBrmoFramework();
        }

        CleanUtil.cleanSTAGING(staging);
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
    public void testCleanupAndDeleteBerichten() throws Exception {
        bean.cleanupBerichten(Bericht.STATUS.RSGB_OK.toString(), BrmoFramework.BR_BAG);
        bean.deleteBerichten(Bericht.STATUS.ARCHIVE.toString(), BrmoFramework.BR_BAG);
        assertEquals("Er zijn nog RSGB_OK berichten",
                aantalBerichtenRsgbOk - aantalBerichtenToArchive,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, "RSGB_OK")
        );
        assertEquals("Er zijn nog ARCHIVE berichten",
                0,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, "ARCHIVE")
        );
        assertEquals("Er zijn geen RSGB_NOK berichten",
                1,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, "RSGB_NOK")
        );
    }
}
