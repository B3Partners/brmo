/*
 * Copyright (C) 2018 B3Partners B.V.
 */
package nl.b3p.brmo.service.stripes;

import net.sourceforge.stripes.action.ActionBeanContext;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.service.testutil.TestUtil;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

/**
 * testcases voor GH 557 (overnemen comfort adresgegevens uit BRK in subject
 * tabel). Draaien met:
 * {@code mvn -Dit.test=BRKComfortAdresUpdatesIntegrationTest -Dtest.onlyITs=true verify -Poracle -pl brmo-service >
 * /tmp/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=BRKComfortAdresUpdatesIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl
 * brmo-service > /tmp/postgresql.log}
 * voor PostgreSQL.
 *
 * @author mprins
 */
public class BRKComfortAdresUpdatesIntegrationTest extends TestUtil {

    private static final Log LOG = LogFactory.getLog(BRKComfortAdresUpdatesIntegrationTest.class);
    private final Lock sequential = new ReentrantLock();
    private UpdatesActionBean bean;
    private BrmoFramework brmo;
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // sStagingBestand, sRsgbBestand,
                arguments("/GH557-brk-comfort-adres/staging-flat.xml", "/GH557-brk-comfort-adres/rsgb-flat.xml"),
                arguments("/GH557-brk-comfort-adres/staging-flat-postbus.xml",
                        "/GH557-brk-comfort-adres/rsgb-flat-postbus.xml")
        );
    }

    @BeforeEach
    public void setUp() throws Exception {
        // mock de context van de bean
        ServletContext sctx = mock(ServletContext.class);
        ActionBeanContext actx = mock(ActionBeanContext.class);
        bean = spy(UpdatesActionBean.class);
        when(bean.getContext()).thenReturn(actx);
        when(actx.getServletContext()).thenReturn(sctx);

        rsgb = new DatabaseDataSourceConnection(dsRsgb);
        staging = new DatabaseDataSourceConnection(dsStaging);

        if (this.isMsSQL) {
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()),
                    DBPROPS.getProperty("rsgb.username").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()),
                    DBPROPS.getProperty("staging.username").toUpperCase());
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
        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);
        sequential.lock();
    }

    private void loadData(String sStagingBestand, String sRsgbBestand) throws Exception {
        Assumptions.assumeTrue(BRKComfortAdresUpdatesIntegrationTest.class.getResource(sStagingBestand) != null,
                "Het bestand met staging testdata zou moeten bestaan.");
        Assumptions.assumeTrue(BRKComfortAdresUpdatesIntegrationTest.class.getResource(sRsgbBestand) != null,
                "Het bestand met rsgb testdata zou moeten bestaan.");
        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(
                new File(BRKComfortAdresUpdatesIntegrationTest.class.getResource(sStagingBestand).toURI())));
        IDataSet rsgbDataSet = fxdb.build(new FileInputStream(
                new File(BRKComfortAdresUpdatesIntegrationTest.class.getResource(sRsgbBestand).toURI())));

        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
            InsertIdentityOperation.CLEAN_INSERT.execute(rsgb, rsgbDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
            DatabaseOperation.CLEAN_INSERT.execute(rsgb, rsgbDataSet);
        }

        Assumptions.assumeTrue(
                1 == brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Er zijn x RSGB_OK berichten"
        );
        Assumptions.assumeTrue(
                1 == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"),
                "Er zijn x STAGING_OK laadprocessen"
        );
    }

    @AfterEach
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
            CleanUtil.cleanSTAGING(staging, true);
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
     * @throws Exception if any
     */
    @DisplayName("Snelle Update")
    @ParameterizedTest(name =  "{index}: verwerken bestanden: ''{0}'' en ''{1}''")
    @MethodSource("argumentsProvider")
    public void testSnelleUpdate(String sStagingBestand, String sRsgbBestand) throws Exception {
        loadData(sStagingBestand, sRsgbBestand);

        final IDataSet rds = rsgb.createDataSet();
        ITable subject = rds.getTable("subject");
        for (int i = 0; i < subject.getRowCount(); i++) {
            assertNull(subject.getValue(i, "adres_binnenland"), "'adres_binnenland' is niet 'null'");

            if (sStagingBestand.contains("postbus")) {
                assertNull(subject.getValue(i, "pa_postadres_postcode"), "'pa_postadres_postcode' is 'null'");
                assertNull(subject.getValue(i, "pa_postbus__of_antwoordnummer"),
                        "'pa_postbus__of_antwoordnummer' is niet 'null'");
                assertNull(subject.getValue(i, "pa_postadrestype"), "'pa_postadrestype'  is niet 'null'");
            }
        }

        bean.populateUpdateProcesses();
        bean.setUpdateProcessName("Bijwerken subject adres comfort data");
        bean.update();

        assertEquals(1, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "niet alle berichten zijn 'RSGB_OK'");

        // subject tabel opnieuw openen
        subject = rds.getTable("subject");
        for (int i = 0; i < subject.getRowCount(); i++) {
            LOG.debug("Rij: " + i + " adres: " + subject.getValue(i, "adres_binnenland"));
            assertNotNull(subject.getValue(i, "adres_binnenland"), "'adres_binnenland' is 'null'");
            assertFalse(subject.getValue(i, "adres_binnenland").toString().isEmpty(),
                    "'adres_binnenland' is leeg");

            if (sStagingBestand.contains("postbus")) {
                assertNotNull(subject.getValue(i, "pa_postadres_postcode"),
                        "'pa_postadres_postcode' is 'null'");
                assertNotNull(subject.getValue(i, "pa_postbus__of_antwoordnummer"),
                        "'pa_postbus__of_antwoordnummer' is 'null'");
                assertEquals("P", subject.getValue(i, "pa_postadrestype"), "waarde moet 'P' zijn");
            }
        }
    }
}
