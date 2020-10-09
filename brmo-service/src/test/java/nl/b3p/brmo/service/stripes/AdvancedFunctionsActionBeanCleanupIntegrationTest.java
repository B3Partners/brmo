/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.service.stripes;

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

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * testcases voor GH issue 287; opschonen en archiveren van berichten ouder dan
 * 3 maanden. Draaien met:
 * {@code mvn -Dit.test=AdvancedFunctionsActionBeanCleanupIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl
 * brmo-service > /tmp/postgresql.log}
 * voor bijvoorbeeld PostgreSQL en
 * {@code mvn -Dit.test=AdvancedFunctionsActionBeanCleanupIntegrationTest -Dtest.onlyITs=true verify -Poracle  -pl
 * brmo-service > /tmp/oracle.log}
 * voor Oracle.
 *
 * @author Mark Prins
 */
public class AdvancedFunctionsActionBeanCleanupIntegrationTest extends TestUtil {

    private static final Log LOG = LogFactory.getLog(AdvancedFunctionsActionBeanCleanupIntegrationTest.class);
    private final Lock sequential = new ReentrantLock();
    private AdvancedFunctionsActionBean bean;
    private BrmoFramework brmo;
    private IDatabaseConnection staging;

    /**
     * @return test data met daarin
     * {@code {"sBestandsNaam", aantalBerichtenRsgbOk, aantalBerichtenToArchive, aantalBerichtenArchive,
     * aantalBerichtenRsgbNok}}
     */
    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                /* ("sBestandsNaam", aantalBerichtenRsgbOk, aantalBerichtenToArchive, aantalBerichtenArchive,
                aantalBerichtenRsgbNok), */
                arguments("/GH-287/staging-flat.xml", 43, 42, 6, 1),
                arguments("/GH-287/gh-292-staging-flat.xml", 2043, 2043, 0, 0),
                /* onderstaande staat op de ignore lijst omdat het op de build servers voor een OOM error zorgt, er
                zitten teveel berichten in.*/
                arguments("/GH-287/gh-292-staging-flat-4242.xml", 4242, 4242, 0, 0)
        );
    }

    @BeforeEach
    public void setUp() throws Exception {
        bean = new AdvancedFunctionsActionBean();
        staging = new DatabaseDataSourceConnection(dsStaging);

        if (this.isMsSQL) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            dsStaging.getConnection().setAutoCommit(true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()),
                    DBPROPS.getProperty("staging.username").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            Assertions.fail("Geen ondersteunde database aangegegeven");
        }
        brmo = new BrmoFramework(dsStaging, null);
        sequential.lock();
    }

    private void loadData(String sBestandsNaam, long aantalBerichtenRsgbOk, long aantalBerichtenArchive)
            throws Exception {
        Assumptions.assumeTrue(
                AdvancedFunctionsActionBeanCleanupIntegrationTest.class.getResource(sBestandsNaam) != null,
                "Het bestand met staging testdata zou moeten bestaan."
        );
        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(
                new FileInputStream(
                        new File(AdvancedFunctionsActionBeanCleanupIntegrationTest.class.getResource(
                                sBestandsNaam).toURI())
                )
        );

        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }

        Assumptions.assumeTrue(
                aantalBerichtenRsgbOk == brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, "RSGB_OK"),
                "Er zijn anders dan verwacht aantal RSGB_OK berichten"
        );
        Assumptions.assumeTrue(
                aantalBerichtenArchive == brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, "ARCHIVE"),
                "Er zijn anders dan verwacht aantal ARCHIVE berichten"
        );
    }

    @AfterEach
    public void cleanup() throws Exception {
        // in geval van niet waar gemaakte assumptions zijn sommige objecten null
        if (brmo != null) {
            brmo.closeBrmoFramework();
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

    @DisplayName("Cleanup Berichten")
    @ParameterizedTest(name = "{index}: bestand: {0}, aantal berichten {1}")
    @MethodSource("argumentsProvider")
    public void testCleanupBerichten(String sBestandsNaam, long aantalBerichtenRsgbOk, long aantalBerichtenToArchive,
                                     long aantalBerichtenArchive, long aantalBerichtenRsgbNok) throws Exception {

        loadData(sBestandsNaam, aantalBerichtenRsgbOk, aantalBerichtenArchive);
        bean.cleanupBerichten(Bericht.STATUS.RSGB_OK.toString(), BrmoFramework.BR_BAG);
        Assertions.assertEquals(
                aantalBerichtenRsgbOk - aantalBerichtenToArchive,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, "RSGB_OK"),
                "Er zijn anders dan verwacht aantal RSGB_OK berichten"
        );
        Assertions.assertEquals(
                aantalBerichtenArchive + aantalBerichtenToArchive,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, "ARCHIVE"),
                "Er zijn anders dan verwacht aantal ARCHIVE berichten"
        );
    }

    @DisplayName("Delete Berichten")
    @ParameterizedTest(name = "{index}: bestand: {0}, aantal berichten {1}")
    @MethodSource("argumentsProvider")
    public void testDeleteBerichten(String sBestandsNaam, long aantalBerichtenRsgbOk, long aantalBerichtenToArchive,
                                    long aantalBerichtenArchive, long aantalBerichtenRsgbNok) throws Exception {

        loadData(sBestandsNaam, aantalBerichtenRsgbOk, aantalBerichtenArchive);
        bean.deleteBerichten(Bericht.STATUS.ARCHIVE.toString(), BrmoFramework.BR_BAG);
        Assertions.assertEquals(
                0,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, "ARCHIVE"),
                "Er zijn nog ARCHIVE berichten"
        );
    }

    @DisplayName("Cleanup en Delete Berichten")
    @ParameterizedTest(name = "{index}: bestand: {0}, aantal berichten {1}")
    @MethodSource("argumentsProvider")
    //@Disabled("Deze programma flow komt normaal niet voor; de GUI staat slechts 1 keuze per run toe.")
    public void testCleanupAndDeleteBerichten(String sBestandsNaam, long aantalBerichtenRsgbOk,
                                              long aantalBerichtenToArchive, long aantalBerichtenArchive,
                                              long aantalBerichtenRsgbNok) throws Exception {

        loadData(sBestandsNaam, aantalBerichtenRsgbOk, aantalBerichtenArchive);
        LOG.debug("Archiveren 'ok' berichten");
        bean.cleanupBerichten(Bericht.STATUS.RSGB_OK.toString(), BrmoFramework.BR_BAG);

        LOG.debug("Verwijderen 'archief' berichten");
        bean.deleteBerichten(Bericht.STATUS.ARCHIVE.toString(), BrmoFramework.BR_BAG);

        Assertions.assertEquals(
                aantalBerichtenRsgbOk - aantalBerichtenToArchive,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, Bericht.STATUS.RSGB_OK.toString()),
                "Er zijn nog RSGB_OK berichten");
        Assertions.assertEquals(
                0,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, Bericht.STATUS.ARCHIVE.toString()),
                "Er zijn nog ARCHIVE berichten over");
        Assertions.assertEquals(
                aantalBerichtenRsgbNok,
                brmo.getCountBerichten(null, null, BrmoFramework.BR_BAG, Bericht.STATUS.RSGB_NOK.toString()),
                "Er zijn nog RSGB_NOK berichten over");
    }
}
