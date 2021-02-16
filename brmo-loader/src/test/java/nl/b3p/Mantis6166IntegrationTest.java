/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.RsgbProxy;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.dbcp2.BasicDataSource;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * testcases voor mantis 6166; incorrecte verwijdering van percelen. Draaien
 * met:
 * {@code mvn -Dit.test=Mantis6166IntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=Mantis6166IntegrationTest -Dtest.onlyITs=true verify -Ppostgresql  -pl brmo-loader > /tmp/postgresql.log} voor PostgreSQL
 *
 * @author mprins
 */
public class Mantis6166IntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Mantis6166IntegrationTest.class);

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // {"bestandsNaam", aantalProcessen, aantalBerichten, standId, verwijderId, mutatieIds },
                arguments("/mantis6166/staging-69660669770000.xml", 4, 4, 722959, 946717,
                        new long[]{47134330, 47134331}),
                arguments("/mantis6166/staging-66860489870000.xml", 4, 4, 488741, 948118,
                        new long[]{47125275, 47125276})
        );
    }

    private BrmoFramework brmo;
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;
    private final Lock sequential = new ReentrantLock();

    @Override
    @BeforeEach
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
            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()),
                    params.getProperty("rsgb.user").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()),
                    params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            Assertions.fail("Geen ondersteunde database aangegegeven.");
        }

        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);

        sequential.lock();
    }

    private void loadData(final String bestandsNaam, final long aantalProcessen, final long aantalBerichten)
            throws Exception {
        assumeTrue(Mantis6166IntegrationTest.class.getResource(bestandsNaam) != null,
                "Het bestand met testdata zou moeten bestaan.");

        IDataSet stagingDataSet = new XmlDataSet(
                new FileInputStream(new File(Mantis6166IntegrationTest.class.getResource(bestandsNaam).toURI())));
        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }

        assumeTrue(aantalBerichten > 0, "Er zijn meer dan 0 berichten in het bestand om te laden");
        assumeTrue(aantalProcessen > 0, "Er zijn meer dan 0 laadprocessen in het bestand om te laden");
        assumeTrue(aantalBerichten == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Er zijn x STAGING_OK berichten om te verwerken");
        assumeTrue(aantalProcessen == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"),
                "Er zijn x STAGING_OK laadprocessen in de database");
    }

    @AfterEach
    public void cleanup() throws Exception {
        if (brmo != null) {
            // in geval van niet waar gemaakte assumptions
            brmo.closeBrmoFramework();
        }

        CleanUtil.cleanRSGB_BRK(rsgb,true);
        rsgb.close();

        CleanUtil.cleanSTAGING(staging, false);
        staging.close();

        try {
            sequential.unlock();
        } catch (IllegalMonitorStateException e) {
            // in geval van niet waar gemaakte assumptions
            LOG.debug("unlock van thread is mislukt, mogelijk niet ge-lock-ed");
        }
    }

    /**
     * transformeer stand bericht.
     *
     * @throws Exception if any
     */
    @DisplayName("Stand")
    @ParameterizedTest(name = "{index}: verwerken bestand: {0}")
    @MethodSource("argumentsProvider")
    public void testStand(final String bestandsNaam, final long aantalProcessen, final long aantalBerichten, final long stand, final long verwijder, final long[] mutaties) throws Exception {
        loadData( bestandsNaam,   aantalProcessen,   aantalBerichten);
        Thread t = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, new long[]{stand}, null);
        t.join();

        assertEquals(aantalBerichten - 1, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Mutatie berichten zijn niet getransformeerd");
        assertEquals(1l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Een bericht is OK getransformeerd");

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals(1, kad_perceel.getRowCount(), "Er is 1 perceel geladen");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals(1, kad_onrrnd_zk.getRowCount(), "Er is 1 onroerende zaak geladen");
    }

    /**
     * transformeer stand bericht en mutaties.
     *
     * @throws Exception if any
     */
    @DisplayName("Stand en mutatie")
    @ParameterizedTest(name = "{index}: verwerken bestand: {0}")
    @MethodSource("argumentsProvider")
    public void testStandMutatie(final String bestandsNaam, final long aantalProcessen, final long aantalBerichten, final long stand, final long verwijder, final long[] mutaties) throws Exception {
        loadData( bestandsNaam,   aantalProcessen,   aantalBerichten);
        long[] transformIds = new long[mutaties.length + 1];
        transformIds[0] = stand;
        for (int i = 1; i < transformIds.length; i++) {
            transformIds[i] = mutaties[i - 1];
        }
        Thread t = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, transformIds, null);
        t.join();

        assertEquals(aantalBerichten - 1, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Alle berichten behalve verwijderen zijn OK getransformeerd");
        assertEquals(1l, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Een (verwijder) bericht is niet getransformeerd en heeft STAGING_OK status");

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals(1, kad_perceel.getRowCount(), "Er is 1 perceel geladen");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals(1, kad_onrrnd_zk.getRowCount(), "Er is 1 onroerende zaak geladen");

        ITable kad_perceel_archief = rsgb.createDataSet().getTable("kad_perceel_archief");
        assertEquals(transformIds.length - 1, kad_perceel_archief.getRowCount(),
                "Er zijn (verwerkt aantal -1) percelen gearchiveerd");

        ITable kad_onrrnd_zk_archief = rsgb.createDataSet().getTable("kad_onrrnd_zk_archief");
        assertEquals(transformIds.length - 1, kad_onrrnd_zk_archief.getRowCount(),
                "Er zijn (verwerkt aantal -1) onroerende zaken gearchiveerd");
    }

    /**
     * transformeer alle berichten.
     *
     * @throws Exception if any
     */
    @DisplayName("All")
    @ParameterizedTest(name = "{index}: verwerken bestand: {0}")
    @MethodSource("argumentsProvider")
    public void testAll(final String bestandsNaam, final long aantalProcessen, final long aantalBerichten, final long stand, final long verwijder, final long[] mutaties) throws Exception {
        loadData( bestandsNaam,   aantalProcessen,   aantalBerichten);
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals(aantalBerichten, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Alle berichten zijn OK getransformeerd");

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals(0, kad_perceel.getRowCount(), "Er zijn geen actuele percelen");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals(0, kad_onrrnd_zk.getRowCount(), "Er zijn geen actuele onroerende zaken");

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals(0, brondocument.getRowCount(), "Er zijn geen brondocumenten");

        ITable kad_perceel_archief = rsgb.createDataSet().getTable("kad_perceel_archief");
        assertEquals(aantalBerichten - 1, kad_perceel_archief.getRowCount(),
                "Alle percelen zijn  gearchiveerd");

        ITable kad_onrrnd_zk_archief = rsgb.createDataSet().getTable("kad_onrrnd_zk_archief");
        assertEquals(aantalBerichten - 1, kad_onrrnd_zk_archief.getRowCount(),
                "Alle onroerende zaken zijn gearchiveerd");
    }

    /**
     * transformeer stand bericht en verwijder bericht.
     *
     * @throws Exception if any
     */
    @DisplayName("Stand en delete")
    @ParameterizedTest(name = "{index}: verwerken bestand: {0}")
    @MethodSource("argumentsProvider")
    public void testStandDelete(final String bestandsNaam, final long aantalProcessen, final long aantalBerichten, final long stand, final long verwijder, final long[] mutaties) throws Exception {
        loadData( bestandsNaam,   aantalProcessen,   aantalBerichten);
        long[] transformIds = new long[]{stand, verwijder};

        Thread t = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, transformIds, null);
        t.join();

        assertEquals(transformIds.length, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Twee berichten zijn OK getransformeerd");
        assertEquals(
                aantalBerichten - transformIds.length, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Mutatie berichten zijn niet getransformeerd");

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals(0, kad_perceel.getRowCount(), "Er zijn geen actuele percelen");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals(0, kad_onrrnd_zk.getRowCount(), "Er zijn geen actuele onroerende zaken");

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals(0, brondocument.getRowCount(), "Er zijn geen brondocumenten");

        ITable kad_perceel_archief = rsgb.createDataSet().getTable("kad_perceel_archief");
        assertEquals(1, kad_perceel_archief.getRowCount(), "Er is 1 archief perceel");

        ITable kad_onrrnd_zk_archief = rsgb.createDataSet().getTable("kad_onrrnd_zk_archief");
        assertEquals(1, kad_onrrnd_zk_archief.getRowCount(), "Er is 1 archief onroerende zaak");
    }

    /**
     * transformeer stand bericht en verwijder bericht; daarna mutatie(s)
     * transformeren.
     *
     * @throws Exception if any
     */
    @DisplayName("Stand en delete en mutatie")
    @ParameterizedTest(name = "{index}: verwerken bestand: {0}")
    @MethodSource("argumentsProvider")
    public void testStandDeleteMutatie(final String bestandsNaam, final long aantalProcessen, final long aantalBerichten, final long stand, final long verwijder, final long[] mutaties) throws Exception {
        loadData( bestandsNaam,   aantalProcessen,   aantalBerichten);
        long[] transformIds = new long[]{stand, verwijder};

        Thread t1 = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, transformIds, null);
        t1.join();

        assertEquals(transformIds.length, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Twee berichten zijn OK getransformeerd");
        assertEquals(mutaties.length, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Mutatie berichten zijn niet getransformeerd");

        Thread t2 = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, mutaties, null);
        t2.join();

        assertEquals(transformIds.length, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Twee berichten zijn OK getransformeerd");
        assertEquals(mutaties.length, brmo.getCountBerichten(null, null, "brk", "RSGB_OUTDATED"),
                "Mutatie berichten zijn outdated");

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals(0, kad_perceel.getRowCount(), "Er zijn geen actuele percelen");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals(0, kad_onrrnd_zk.getRowCount(), "Er zijn geen actuele onroerende zaken");

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals(0, brondocument.getRowCount(), "Er zijn geen brondocumenten");
    }

}
