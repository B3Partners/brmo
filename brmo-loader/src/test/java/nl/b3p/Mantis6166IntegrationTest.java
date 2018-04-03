/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.RsgbProxy;
import nl.b3p.brmo.test.util.database.JTDSDriverBasedFailures;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * testcases voor mantis 6166; incorrecte verwijdering van percelen. Draaien
 * met:
 * {@code mvn -Dit.test=Mantis6166IntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle.
 *
 * <strong>NB. werkt niet op mssql, althans niet met de jTDS driver omdat die
 * geen JtdsPreparedStatement#setNull() methode heeft.</strong>
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
@Category(JTDSDriverBasedFailures.class)
public class Mantis6166IntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Mantis6166IntegrationTest.class);

    @Parameterized.Parameters(name = "{index}: verwerken bestand: {0}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"bestandsNaam", aantalProcessen, aantalBerichten, standId, verwijderId, mutatieIds },
            {"/mantis6166/staging-69660669770000.xml", 4, 4, 722959, 946717, new long[]{47134330, 47134331}},
            {"/mantis6166/staging-66860489870000.xml", 4, 4, 488741, 948118, new long[]{47125275, 47125276}}
        });
    }

    private BrmoFramework brmo;
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;

    private final Lock sequential = new ReentrantLock();

    /*
     * test parameters.
     */
    private final String bestandsNaam;
    private final long aantalProcessen;
    private final long aantalBerichten;
    private final long stand;
    private final long[] mutaties;
    private final long verwijder;

    public Mantis6166IntegrationTest(final String bestandsNaam, final long aantalProcessen, final long aantalBerichten, final long stand, final long verwijder, final long[] mutaties) {
        this.bestandsNaam = bestandsNaam;
        this.aantalProcessen = aantalProcessen;
        this.aantalBerichten = aantalBerichten;
        this.stand = stand;
        this.verwijder = verwijder;
        this.mutaties = mutaties;
    }

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

        assumeTrue("Deze test werkt niet met de jTDS driver omdat die geen JtdsPreparedStatement#setNull() methode heeft.", !this.isMsSQL);

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
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegegeven.");
        }

        assumeTrue("Het bestand met testdata zou moeten bestaan.", Mantis6166IntegrationTest.class.getResource(bestandsNaam) != null);

        IDataSet stagingDataSet = new XmlDataSet(new FileInputStream(new File(Mantis6166IntegrationTest.class.getResource(bestandsNaam).toURI())));

        sequential.lock();

        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }

        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);

        assumeTrue("Er zijn meer dan 0 berichten in het bestand om te laden", aantalBerichten > 0);
        assumeTrue("Er zijn meer dan 0 laadprocessen in het bestand om te laden", aantalProcessen > 0);
        assumeTrue("Er zijn x STAGING_OK berichten om te verwerken", aantalBerichten == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));
        assumeTrue("Er zijn x STAGING_OK laadprocessen in de database", aantalProcessen == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        if (brmo != null) {
            // in geval van niet waar gemaakte assumptions
            brmo.closeBrmoFramework();
        }
        assumeTrue("Deze test werkt niet met de jTDS driver omdat die geen JtdsPreparedStatement#setNull() methode heeft.", !this.isMsSQL);

        CleanUtil.cleanRSGB(rsgb);
        rsgb.close();

        DatabaseOperation.DELETE_ALL.execute(staging, new DefaultDataSet(new DefaultTable[]{
            new DefaultTable("job")}
        ));
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
    @Test
    public void testStand() throws Exception {
        Thread t = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, new long[]{stand}, null);
        t.join();

        assertEquals("Mutatie berichten zijn niet getransformeerd", aantalBerichten - 1, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));
        assertEquals("Een bericht is OK getransformeerd", 1l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er is 1 perceel geladen", 1, kad_perceel.getRowCount());

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er is 1 onroerende zaak geladen", 1, kad_onrrnd_zk.getRowCount());
    }

    /**
     * transformeer stand bericht en mutaties.
     *
     * @throws Exception if any
     */
    @Test
    public void testStandMutatie() throws Exception {
        long[] transformIds = new long[mutaties.length + 1];
        transformIds[0] = stand;
        for (int i = 1; i < transformIds.length; i++) {
            transformIds[i] = mutaties[i - 1];
        }
        Thread t = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, transformIds, null);
        t.join();

        assertEquals("Alle berichten behalve verwijderen zijn OK getransformeerd", aantalBerichten - 1, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assertEquals("Een (verwijder) bericht is niet getransformeerd en heeft STAGING_OK status", 1l, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er is 1 perceel geladen", 1, kad_perceel.getRowCount());

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Er is 1 onroerende zaak geladen", 1, kad_onrrnd_zk.getRowCount());

        ITable kad_perceel_archief = rsgb.createDataSet().getTable("kad_perceel_archief");
        assertEquals("Er zijn (verwerkt aantal -1) percelen gearchiveerd", transformIds.length - 1, kad_perceel_archief.getRowCount());

        ITable kad_onrrnd_zk_archief = rsgb.createDataSet().getTable("kad_onrrnd_zk_archief");
        assertEquals("Er zijn (verwerkt aantal -1) onroerende zaken gearchiveerd", transformIds.length - 1, kad_onrrnd_zk_archief.getRowCount());
    }

    /**
     * transformeer alle berichten.
     *
     * @throws Exception if any
     */
    @Test
    public void testAll() throws Exception {
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals("Alle berichten zijn OK getransformeerd", aantalBerichten, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er zijn geen actuele percelen", 0, kad_perceel.getRowCount());

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Er zijn geen actuele onroerende zaken", 0, kad_onrrnd_zk.getRowCount());

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals("Er zijn geen brondocumenten", 0, brondocument.getRowCount());

        ITable kad_perceel_archief = rsgb.createDataSet().getTable("kad_perceel_archief");
        assertEquals("Alle percelen zijn  gearchiveerd", aantalBerichten - 1, kad_perceel_archief.getRowCount());

        ITable kad_onrrnd_zk_archief = rsgb.createDataSet().getTable("kad_onrrnd_zk_archief");
        assertEquals("Alle onroerende zaken zijn gearchiveerd", aantalBerichten - 1, kad_onrrnd_zk_archief.getRowCount());
    }

    /**
     * transformeer stand bericht en verwijder bericht.
     *
     * @throws Exception if any
     */
    @Test
    public void testStandDelete() throws Exception {
        long[] transformIds = new long[]{stand, verwijder};

        Thread t = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, transformIds, null);
        t.join();

        assertEquals("Twee berichten zijn OK getransformeerd", transformIds.length, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assertEquals("Mutatie berichten zijn niet getransformeerd", aantalBerichten - transformIds.length, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er zijn geen actuele percelen", 0, kad_perceel.getRowCount());

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Er zijn geen actuele onroerende zaken", 0, kad_onrrnd_zk.getRowCount());

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals("Er zijn geen brondocumenten", 0, brondocument.getRowCount());

        ITable kad_perceel_archief = rsgb.createDataSet().getTable("kad_perceel_archief");
        assertEquals("Er is 1 archief perceel", 1, kad_perceel_archief.getRowCount());

        ITable kad_onrrnd_zk_archief = rsgb.createDataSet().getTable("kad_onrrnd_zk_archief");
        assertEquals("Er is 1 archief onroerende zaak", 1, kad_onrrnd_zk_archief.getRowCount());
    }

    /**
     * transformeer stand bericht en verwijder bericht; daarna mutatie(s)
     * transformeren.
     *
     * @throws Exception if any
     */
    @Test
    public void testStandDeleteMutatie() throws Exception {
        long[] transformIds = new long[]{stand, verwijder};

        Thread t1 = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, transformIds, null);
        t1.join();

        assertEquals("Twee berichten zijn OK getransformeerd", transformIds.length, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assertEquals("Mutatie berichten zijn niet getransformeerd", mutaties.length, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));

        Thread t2 = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, mutaties, null);
        t2.join();

        assertEquals("Twee berichten zijn OK getransformeerd", transformIds.length, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assertEquals("Mutatie berichten zijn outdated", mutaties.length, brmo.getCountBerichten(null, null, "brk", "RSGB_OUTDATED"));

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er zijn geen actuele percelen", 0, kad_perceel.getRowCount());

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Er zijn geen actuele onroerende zaken", 0, kad_onrrnd_zk.getRowCount());

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals("Er zijn geen brondocumenten", 0, brondocument.getRowCount());
    }

}
