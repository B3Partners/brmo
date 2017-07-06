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
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * testcases voor mantis 6098; incorrecte verwijdering van berichten. Draaien
 * met:
 * {@code mvn -Dit.test=Mantis6098IntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle.
 *
 * @author mprins
 */
public class Mantis6098IntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Mantis6098IntegrationTest.class);

    private BrmoFramework brmo;

    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;

    private final Lock sequential = new ReentrantLock();

    private static final long stand = 5521;
    private static final long mutatie = 458403;
    private static final long verwijder = 458408;

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

        assumeTrue("Er zijn 3 STAGING_OK berichten", 3l == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));
        assumeTrue("Er zijn 3 STAGING_OK laadprocessen", 3l == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanRSGB(rsgb);
        rsgb.close();

        DatabaseOperation.DELETE_ALL.execute(staging, new DefaultDataSet(new DefaultTable[]{
            new DefaultTable("job")}
        ));
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

        assertEquals("Twee berichten zijn niet getransformeerd", 2l, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));
        assertEquals("Een bericht is OK getransformeerd", 1l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er is 1 perceel geladen", 1, kad_perceel.getRowCount());

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er is 1 onroerende zaak geladen", 1, kad_onrrnd_zk.getRowCount());

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals("Er zijn 369 brondocumenten geladen", 369, brondocument.getRowCount());
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

        assertEquals("Twee berichten zijn OK getransformeerd", 2l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assertEquals("Een bericht is niet getransformeerd", 1l, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er is 1 perceel geladen", 1, kad_perceel.getRowCount());

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Er is 1 onroerende zaak geladen", 1, kad_onrrnd_zk.getRowCount());

        ITable kad_perceel_archief = rsgb.createDataSet().getTable("kad_perceel_archief");
        assertEquals("Er is 1 perceel gearchiveerd", 1, kad_perceel_archief.getRowCount());

        ITable kad_onrrnd_zk_archief = rsgb.createDataSet().getTable("kad_onrrnd_zk_archief");
        assertEquals("Er is 1 onroerende zaak gearchiveerd", 1, kad_onrrnd_zk_archief.getRowCount());

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals("Er zijn 369 brondocumenten geladen", 369, brondocument.getRowCount());
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

        assertEquals("Alle berichten zijn OK getransformeerd", 3l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals("Er zijn geen brondocumenten", 0, brondocument.getRowCount());

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er zijn geen actuele percelen", 0, kad_perceel.getRowCount());

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Er zijn geen actuele onroerende zaken", 0, kad_onrrnd_zk.getRowCount());
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

        assertEquals("Twee berichten zijn OK getransformeerd", 2l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assertEquals("Een bericht is niet getransformeerd", 1l, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er zijn geen actuele percelen", 0, kad_perceel.getRowCount());

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Er zijn geen actuele onroerende zaken", 0, kad_onrrnd_zk.getRowCount());

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals("Er zijn geen brondocumenten", 0, brondocument.getRowCount());
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

        assertEquals("Verwacht 2 berichten zijn OK getransformeerd", 2l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assertEquals("Verwacht 1 bericht is niet getransformeerd", 1l, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));

        Thread t2 = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, new long[]{mutatie}, null);
        t2.join();

        assertEquals("Twee berichten zijn OK getransformeerd", 2l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assertEquals("Verwacht 1 bericht is outdated", 1l, brmo.getCountBerichten(null, null, "brk", "RSGB_OUTDATED"));

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er zijn geen actuele percelen", 0, kad_perceel.getRowCount());

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Verwacht geen actuele onroerende zaken", 0, kad_onrrnd_zk.getRowCount());

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals("Verwacht geen brondocumenten", 0, brondocument.getRowCount());
    }

}
