/*
 * Copyright (C) 2018 B3Partners B.V.
 */
package nl.b3p.brmo.service.stripes;

import net.sourceforge.stripes.action.ActionBeanContext;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.service.testutil.TestUtil;
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import static org.mockito.Mockito.*;

import javax.servlet.ServletContext;

/**
 * testcases voor GH 557 (overnemen comfort adresgegevens uit BRK in subject
 * tabel). Draaien met:
 * {@code mvn -Dit.test=BRKComfortAdresUpdatesIntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=BRKComfortAdresUpdatesIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * voor PostgreSQL.
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
public class BRKComfortAdresUpdatesIntegrationTest extends TestUtil {

    private static final Log LOG = LogFactory.getLog(BRKComfortAdresUpdatesIntegrationTest.class);

    @Parameterized.Parameters(name = "{index}: verwerken bestanden: {0} en {1}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {sStagingBestand, sRsgbBestand},
            {"/GH557-brk-comfort-adres/staging-flat.xml", "/GH557-brk-comfort-adres/rsgb-flat.xml"},
            {"/GH557-brk-comfort-adres/staging-flat-postbus.xml", "/GH557-brk-comfort-adres/rsgb-flat-postbus.xml"},});
    }

    private final Lock sequential = new ReentrantLock();
    private UpdatesActionBean bean;
    private BrmoFramework brmo;
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;

    /*
     * test parameters.
     */
    private final String sStagingBestand;
    private final String sRsgbBestand;

    public BRKComfortAdresUpdatesIntegrationTest(String sStagingBestand, String sRsgbBestand) {
        this.sRsgbBestand = sRsgbBestand;
        this.sStagingBestand = sStagingBestand;
    }

    @Before
    public void setUp() throws Exception {
        assumeTrue("Het bestand met staging testdata zou moeten bestaan.", BRKComfortAdresUpdatesIntegrationTest.class.getResource(sStagingBestand) != null);
        assumeTrue("Het bestand met rsgb testdata zou moeten bestaan.", BRKComfortAdresUpdatesIntegrationTest.class.getResource(sRsgbBestand) != null);

        // mock de context van de bean
        ServletContext sctx = mock(ServletContext.class);
        ActionBeanContext actx = mock(ActionBeanContext.class);
        bean = spy(UpdatesActionBean.class);
        when(bean.getContext()).thenReturn(actx);
        when(actx.getServletContext()).thenReturn(sctx);

        // set up database en brmo framework en laadt test data
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
        staging.getConfig().setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);
        rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);

        setupJNDI(dsRsgb, dsStaging);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(BRKComfortAdresUpdatesIntegrationTest.class.getResource(sStagingBestand).toURI())));
        IDataSet rsgbDataSet = fxdb.build(new FileInputStream(new File(BRKComfortAdresUpdatesIntegrationTest.class.getResource(sRsgbBestand).toURI())));

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

        assumeTrue("Er zijn x RSGB_OK berichten", 1 == brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assumeTrue("Er zijn x STAGING_OK laadprocessen", 1 == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        if (brmo != null) {
            // in geval van niet waar gemaakte assumptions
            brmo.closeBrmoFramework();
        }
        if (rsgb != null) {
            CleanUtil.cleanRSGB_BRK(rsgb, true);
            rsgb.close();
        }
        if (staging != null) {
            CleanUtil.cleanSTAGING(staging);
            staging.close();
        }

        try {
            sequential.unlock();
        } catch (IllegalMonitorStateException e) {
            // in geval van niet waar gemaakte assumptions
            LOG.debug("unlock van thread is mislukt, mogelijk niet ge-lock-ed of test overgeslagen.");
        }
    }

    /**
     * Test of de snelle update de kolom `adres_binnenland` bijwerkt met comfort
     * adres op basis van woonlocatie, eventueel wordt ook postbus adres gevuld
     * uit postlocatie.
     *
     * @throws Exception
     */
    @Test
    public void testSnelleUpdate() throws Exception {
        final IDataSet rds = rsgb.createDataSet();

        ITable subject = rds.getTable("subject");
        for (int i = 0; i < subject.getRowCount(); i++) {
            assertNull("'adres_binnenland' is niet 'null'", subject.getValue(i, "adres_binnenland"));

            if (sStagingBestand.contains("postbus")) {
                assertNull("'pa_postadres_postcode' is 'null'", subject.getValue(i, "pa_postadres_postcode"));
                assertNull("'pa_postbus__of_antwoordnummer' is niet 'null'", subject.getValue(i, "pa_postbus__of_antwoordnummer"));
                assertNull("'pa_postadrestype'  is niet 'null'", subject.getValue(i, "pa_postadrestype"));
            }
        }

        bean.populateUpdateProcesses();
        bean.setUpdateProcessName("Bijwerken subject adres comfort data");
        bean.update();

        assertEquals("niet alle berichten zijn 'RSGB_OK'", 1, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));

        // subject tabel opnieuw openen
        subject = rds.getTable("subject");
        for (int i = 0; i < subject.getRowCount(); i++) {
            LOG.debug("Rij: " + i + " adres: " + subject.getValue(i, "adres_binnenland"));
            assertNotNull("'adres_binnenland' is 'null'", subject.getValue(i, "adres_binnenland"));
            assertFalse("'adres_binnenland' is leeg", subject.getValue(i, "adres_binnenland").toString().isEmpty());

            if (sStagingBestand.contains("postbus")) {
                assertNotNull("'pa_postadres_postcode' is 'null'", subject.getValue(i, "pa_postadres_postcode"));
                assertNotNull("'pa_postbus__of_antwoordnummer' is 'null'", subject.getValue(i, "pa_postbus__of_antwoordnummer"));
                assertEquals("waarde moet 'P' zijn", "P", subject.getValue(i, "pa_postadrestype"));
            }
        }
    }

}
