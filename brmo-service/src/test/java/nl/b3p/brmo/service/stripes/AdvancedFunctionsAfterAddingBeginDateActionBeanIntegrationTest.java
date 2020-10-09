/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.service.stripes;

import net.sourceforge.stripes.action.ActionBeanContext;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
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
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

/**
 * testcase voor snelle update van brk berichten en daarna een herhalen van brk verwijder berichten. Draaien
 * met:
 * {@code mvn -Dit.test=AdvancedFunctionsAfterAddingBeginDateActionBeanIntegrationTest -Dtest.onlyITs=true verify
 * -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=AdvancedFunctionsAfterAddingBeginDateActionBeanIntegrationTest -Dtest.onlyITs=true verify
 * -Ppostgresql -pl brmo-service > /tmp/postgresql.log}
 * voor PostgreSQL of
 * {@code mvn -Dit.test=AdvancedFunctionsAfterAddingBeginDateActionBeanIntegrationTest -Dtest.onlyITs=true verify
 * -Pmssql -pl brmo-service > target/mssql.log}
 * voor MS SQL.
 *
 * @author mprins
 * @see AdvancedFunctionsActionBeanIntegrationTest
 */
//@Category({ MSSqlServerDriverBasedFailures.class })
public class AdvancedFunctionsAfterAddingBeginDateActionBeanIntegrationTest extends TestUtil {

    private static final Log LOG = LogFactory.getLog(
            AdvancedFunctionsAfterAddingBeginDateActionBeanIntegrationTest.class);
    private final Lock sequential = new ReentrantLock();
    private AdvancedFunctionsActionBean bean;

    private UpdatesActionBean updatesBean;

    private BrmoFramework brmo;
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                /*"sBestandsNaam", aantalProcessen, aantalBerichten, "rBestandsNaam",*/
                arguments("/replayDeleteAfterUpdateBegindate/staging-flat.xml", 2, 2,
                        "/replayDeleteAfterUpdateBegindate/rsgb-spook_kad_onrrnd_zk-flat.xml"),
                arguments("/replayDeleteAfterUpdateBegindate/staging-flat-4.xml", 4, 4,
                        "/replayDeleteAfterUpdateBegindate/rsgb-spook_kad_onrrnd_zk-flat-4.xml")
        );
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        bean = new AdvancedFunctionsActionBean();
        updatesBean = spy(UpdatesActionBean.class);

        ServletContext sctx = mock(ServletContext.class);
        ActionBeanContext actx = mock(ActionBeanContext.class);
        updatesBean = spy(UpdatesActionBean.class);
        when(updatesBean.getContext()).thenReturn(actx);
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
            Assertions.fail("Geen ondersteunde database aangegegeven");
        }
        sequential.lock();
        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);
    }

    private void loadData(String sBestandsNaam, long aantalBerichten, long aantalProcessen, String rBestandsNaam)
            throws Exception {

        Assumptions.assumeTrue(
                AdvancedFunctionsAfterAddingBeginDateActionBeanIntegrationTest.class.getResource(sBestandsNaam) != null,
                "Het bestand met staging testdata zou moeten bestaan.");
        Assumptions.assumeTrue(
                AdvancedFunctionsAfterAddingBeginDateActionBeanIntegrationTest.class.getResource(rBestandsNaam) != null,
                "Het bestand met rsgb testdata zou moeten bestaan.");

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(
                AdvancedFunctionsAfterAddingBeginDateActionBeanIntegrationTest.class.getResource(
                        sBestandsNaam).toURI())));
        IDataSet rsgbDataSet = fxdb.build(new FileInputStream(new File(
                AdvancedFunctionsAfterAddingBeginDateActionBeanIntegrationTest.class.getResource(
                        rBestandsNaam).toURI())));

        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
            InsertIdentityOperation.CLEAN_INSERT.execute(rsgb, rsgbDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
            DatabaseOperation.CLEAN_INSERT.execute(rsgb, rsgbDataSet);
        }
        Assumptions.assumeTrue(aantalBerichten == brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Er zijn x RSGB_OK berichten");
        Assumptions.assumeTrue(aantalProcessen == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"),
                "Er zijn x STAGING_OK laadprocessen");
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

    @DisplayName("Replay BRK Verwijder berichten na update IngangsdatumRecht")
    @ParameterizedTest(name = "{index}: verwerken bestand: ''{0}''")
    @MethodSource("argumentsProvider")
    public void testReplayBRKVerwijderBerichtenNaUpdateIngangsdatumRecht(String sBestandsNaam, long aantalBerichten,
                                                                         long aantalProcessen, String rBestandsNaam)
            throws Exception {

        loadData(sBestandsNaam, aantalBerichten, aantalProcessen, rBestandsNaam);

        updatesBean.populateUpdateProcesses();
        updatesBean.setUpdateProcessName("Bijwerken ingangsdatum_recht zakelijk recht");
        updatesBean.update();
        Date now = new Date();
        long maxWait = 20000;
        while (!updatesBean.isComplete() && (new Date().getTime() - now.getTime() < maxWait)) {
            Thread.sleep(1000);
            LOG.info("waiting...");
        }
        LOG.info("Finished waiting");

        final IDataSet rds = rsgb.createDataSet();
        ITable zak_recht = rsgb.createDataSet().getTable("zak_recht");
        for (int i = 0; i < zak_recht.getRowCount(); i++) {
            Assertions.assertNotNull(zak_recht.getValue(i, "ingangsdatum_recht"), "Ingangsdatum recht is niet gevuld");
            Assertions.assertNotNull(zak_recht.getValue(i, "fk_3avr_aand"), "fk_3avr_aand recht is niet gevuld");
        }

        // deze gegevens komen uit de test data set
        Assertions.assertEquals(1, rds.getTable("kad_onrrnd_zk").getRowCount(),
                "Er is een spook record in de kad_onrrnd_zk tabel");
        Assertions.assertEquals(0, rds.getTable("kad_perceel").getRowCount(), "De perceel tabel is leeg");
        Assertions.assertEquals(aantalBerichten - (1 + 1), rds.getTable("kad_onrrnd_zk_archief").getRowCount(),
                "De kad_onrrnd_zk_archief komt een record te kort");
        Assertions.assertTrue(0 < rds.getTable("kad_perceel_archief").getRowCount(),
                "Er is minstens een perceel in kad_perceel_archief");

        bean.replayBRKVerwijderBerichten(BrmoFramework.BR_BRK, Bericht.STATUS.RSGB_OK.toString());

        Assertions.assertEquals(0, rds.getTable("kad_onrrnd_zk").getRowCount(),
                "Er zijn geen spook records in de kad_onrrnd_zk tabel");
        Assertions.assertTrue(0 < rds.getTable("kad_onrrnd_zk_archief").getRowCount(),
                "De kad_onrrnd_zk_archief tabel is niet leeg");
        Assertions.assertEquals(
                aantalBerichten - 1,
                rds.getTable("kad_perceel_archief").getRowCount(),
                "Er zit voor ieder bericht met perceel een record in kad_perceel_archief");

        Assertions.assertEquals(
                aantalBerichten,
                brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Alle berichten hebben status RSGB_OK");
    }
}
