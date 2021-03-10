/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.RsgbProxy;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;


/**
 * testcases voor mantis 6098; incorrecte verwijdering van berichten. Draaien
 * met:
 * {@code mvn -Dit.test=Mantis6098IntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle.
 *
 * @author mprins
 */
@Tag("skip-windows-java11")
public class Mantis6098IntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Mantis6098IntegrationTest.class);
    private static final long stand = 5521;
    private static final long mutatie = 458403;
    private static final long verwijder = 458408;

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
            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), params.getProperty("rsgb.user").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            // we hebben alleen nog postgres over
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }
        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(Mantis6098IntegrationTest.class.getResource("/mantis6098/staging-flat.xml").toURI())));

        sequential.lock();

        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }

        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);

        assumeTrue(3L == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Er zijn 3 STAGING_OK berichten");
        assumeTrue(3L == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"),
                "Er zijn 3 STAGING_OK laadprocessen");
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanRSGB_BRK(rsgb, true);
        rsgb.close();

        CleanUtil.cleanSTAGING(staging, false);
        staging.close();

        sequential.unlock();
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

        assertEquals(2L, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Twee berichten zijn niet getransformeerd");
        assertEquals(1L, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Een bericht is OK getransformeerd");

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals(1, kad_perceel.getRowCount(), "Er is 1 perceel geladen");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals(1, kad_onrrnd_zk.getRowCount(), "Er is 1 onroerende zaak geladen");

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals(369, brondocument.getRowCount(), "Er zijn 369 brondocumenten geladen");
    }

    /**
     * transformeer stand bericht en eerste mutatie.
     *
     * @throws Exception if any
     */
    @Test
    public void testStandMutatie() throws Exception {
        Thread t = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, new long[]{stand, mutatie}, null);
        t.join();

        assertEquals(2L, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Twee berichten zijn OK getransformeerd");
        assertEquals(1L, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Een bericht is niet getransformeerd");

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals(1, kad_perceel.getRowCount(), "Er is 1 perceel geladen");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals(1, kad_onrrnd_zk.getRowCount(), "Er is 1 onroerende zaak geladen");

        ITable kad_perceel_archief = rsgb.createDataSet().getTable("kad_perceel_archief");
        assertEquals(1, kad_perceel_archief.getRowCount(), "Er is 1 perceel gearchiveerd");

        ITable kad_onrrnd_zk_archief = rsgb.createDataSet().getTable("kad_onrrnd_zk_archief");
        assertEquals(1, kad_onrrnd_zk_archief.getRowCount(), "Er is 1 onroerende zaak gearchiveerd");

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals(369, brondocument.getRowCount(), "Er zijn 369 brondocumenten geladen");
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

        Assertions.assertEquals(3L, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Alle berichten zijn OK getransformeerd");

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        Assertions.assertEquals(0, brondocument.getRowCount(), "Er zijn geen brondocumenten");

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        Assertions.assertEquals(0, kad_perceel.getRowCount(), "Er zijn geen actuele percelen");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        Assertions.assertEquals(0, kad_onrrnd_zk.getRowCount(), "Er zijn geen actuele onroerende zaken");
    }

    /**
     * transformeer stand bericht en verwijder bericht.
     *
     * @throws Exception if any
     */
    @Test
    public void testStandDelete() throws Exception {
        Thread t = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, new long[]{stand, verwijder}, null);
        t.join();

        Assertions.assertEquals(2L, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Twee berichten zijn OK getransformeerd");
        Assertions.assertEquals(1L, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Een bericht is niet getransformeerd");

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        Assertions.assertEquals(0, kad_perceel.getRowCount(), "Er zijn geen actuele percelen");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        Assertions.assertEquals(0, kad_onrrnd_zk.getRowCount(), "Er zijn geen actuele onroerende zaken");

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        Assertions.assertEquals(0, brondocument.getRowCount(), "Er zijn geen brondocumenten");
    }

    /**
     * transformeer stand bericht en verwijder bericht; daarna mutatie
     * transformeren.
     *
     * @throws Exception if any
     */
    @Test
    public void testStandDeleteMutatie() throws Exception {
        Thread t1 = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, new long[]{stand, verwijder}, null);
        t1.join();

        Assertions.assertEquals(2L, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Verwacht 2 berichten zijn OK getransformeerd");
        Assertions.assertEquals(1L, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Verwacht 1 bericht is niet getransformeerd");

        Thread t2 = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, new long[]{mutatie}, null);
        t2.join();

        Assertions.assertEquals(2L, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Twee berichten zijn OK getransformeerd");
        Assertions.assertEquals(1L, brmo.getCountBerichten(null, null, "brk", "RSGB_OUTDATED"),
                "Verwacht 1 bericht is outdated");

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        Assertions.assertEquals(0, kad_perceel.getRowCount(), "Er zijn geen actuele percelen");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        Assertions.assertEquals(0, kad_onrrnd_zk.getRowCount(), "Verwacht geen actuele onroerende zaken");

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        Assertions.assertEquals(0, brondocument.getRowCount(), "Verwacht geen brondocumenten");
    }

}
