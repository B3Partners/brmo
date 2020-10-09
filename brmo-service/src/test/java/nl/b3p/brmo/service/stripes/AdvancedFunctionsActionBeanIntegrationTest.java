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
 * testcases voor GH issue 260; herhalen van brk verwijder berichten. Draaien
 * met:
 * {@code mvn -Dit.test=AdvancedFunctionsActionBeanIntegrationTest -Dtest.onlyITs=true verify -Poracle -pl brmo-service >
 * /tmp/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=AdvancedFunctionsActionBeanIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl brmo-service >
 * /tmp/postgresql.log} voor PostgreSQL.
 *
 * @author mprins
 */
public class AdvancedFunctionsActionBeanIntegrationTest extends TestUtil {

    private static final Log LOG = LogFactory.getLog(AdvancedFunctionsActionBeanIntegrationTest.class);
    private final Lock sequential = new ReentrantLock();
    private AdvancedFunctionsActionBean bean;

    private UpdatesActionBean updatesBean;
    private BrmoFramework brmo;
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                /*"sBestandsNaam", aantalProcessen, aantalBerichten, rBestandsNaam,*/
                arguments("/gh-issue-260/staging-flat.xml", 2, 2, "/gh-issue-260/rsgb-spook_kad_onrrnd_zk-flat.xml"),
                arguments("/gh-issue-260/staging-flat-4.xml", 4, 4, "/gh-issue-260/rsgb-spook_kad_onrrnd_zk-flat-4.xml")
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
            fail("Geen ondersteunde database aangegegeven");
        }

        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);
        sequential.lock();
    }

    private void loadData(String sBestandsNaam, long aantalBerichten, long aantalProcessen, String rBestandsNaam)
            throws Exception {
        Assumptions.assumeTrue(AdvancedFunctionsActionBeanIntegrationTest.class.getResource(sBestandsNaam) != null,
                "Het bestand met staging testdata zou moeten bestaan.");
        Assumptions.assumeTrue(AdvancedFunctionsActionBeanIntegrationTest.class.getResource(rBestandsNaam) != null,
                "Het bestand met rsgb testdata zou moeten bestaan.");
        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(
                new File(AdvancedFunctionsActionBeanIntegrationTest.class.getResource(sBestandsNaam).toURI())));
        IDataSet rsgbDataSet = fxdb.build(new FileInputStream(
                new File(AdvancedFunctionsActionBeanIntegrationTest.class.getResource(rBestandsNaam).toURI())));

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

    @DisplayName("Cleanup")
    @ParameterizedTest(name = "{index}: verwerken bestand: ''{0}''")
    @MethodSource("argumentsProvider")
    public void testReplayBRKVerwijderBerichten(String sBestandsNaam, long aantalBerichten, long aantalProcessen,
                                                String rBestandsNaam) throws Exception {
        loadData(sBestandsNaam, aantalBerichten, aantalProcessen, rBestandsNaam);
        final IDataSet rds = rsgb.createDataSet();

        assertEquals(1, rds.getTable("kad_onrrnd_zk").getRowCount(),
                "Er is een spook record in de kad_onrrnd_zk tabel");
        assertEquals(0, rds.getTable("kad_perceel").getRowCount(), "De perceel tabel is leeg");
        assertEquals(aantalBerichten - (1 + 1), rds.getTable("kad_onrrnd_zk_archief").getRowCount(),
                "De kad_onrrnd_zk_archief komt een record te kort");
        assertTrue(0 < rds.getTable("kad_perceel_archief").getRowCount(),
                "Er is minstens een perceel in kad_perceel_archief");

        bean.replayBRKVerwijderBerichten(BrmoFramework.BR_BRK, Bericht.STATUS.RSGB_OK.toString());

        assertEquals(0, rds.getTable("kad_onrrnd_zk").getRowCount(),
                "Er zijn geen spook records in de kad_onrrnd_zk tabel");
        assertTrue(0 < rds.getTable("kad_onrrnd_zk_archief").getRowCount(),
                "De kad_onrrnd_zk_archief tabel is niet leeg");
        assertEquals(
                aantalBerichten - 1,
                rds.getTable("kad_perceel_archief").getRowCount(),
                "Er zit voor ieder bericht met perceel in record in kad_perceel_archief");

        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals(aantalBerichten, bericht.getRowCount(), "Alle berichten hebben status RSGB_OK");
    }

    @DisplayName("Cleanup")
    @ParameterizedTest(name = "{index}: verwerken bestand: ''{0}''")
    @MethodSource("argumentsProvider")
    public void testFillbestandsNaamHersteld(String sBestandsNaam, long aantalBerichten, long aantalProcessen,
                                             String rBestandsNaam) throws Exception {
        loadData(sBestandsNaam, aantalBerichten, aantalProcessen, rBestandsNaam);
        bean.fillbestandsNaamHersteld(BrmoFramework.BR_BRK, "0");
        ITable laadproces = staging.createDataSet().getTable("laadproces");

        for (int i = 1; i < laadproces.getRowCount(); i++) {
            if (laadproces.getValue(i, "bestand_naam").toString().equals("stand")) {
                assertNull(laadproces.getValue(i, "bestand_naam_hersteld"),
                        "'bestand_naam_hersteld' moet leeg zijn voor stand");
            } else if (laadproces.getValue(i, "bestand_naam").toString().equals("verwijder")) {
                assertNotNull(laadproces.getValue(i, "bestand_naam_hersteld"),
                        "'bestand_naam_hersteld' mag niet leeg zijn");
                assertEquals(
                        "bestandsnaam kon niet worden hersteld", laadproces.getValue(i, "bestand_naam_hersteld"),
                        "inhoud mag niet herstelbaar zijn");
            } else {
                assertNotNull(laadproces.getValue(i, "bestand_naam_hersteld"),
                        "'bestand_naam_hersteld' mag niet leeg zijn");
                assertNotEquals(
                        "bestandsnaam kon niet worden hersteld", laadproces.getValue(i, "bestand_naam_hersteld"),
                        "inhoud moet herstelbaar zijn");
            }
        }
    }
}
