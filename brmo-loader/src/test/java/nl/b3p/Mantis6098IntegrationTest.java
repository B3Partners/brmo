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
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.dataset.DefaultDataSet;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * testcases voor mantis 6098; incorrecte verwijdering van berichten.
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
        dsRsgb.setDefaultCatalog(params.getProperty("rsgb.schema"));

        rsgb = new DatabaseDataSourceConnection(dsRsgb);
        staging = new DatabaseDataSourceConnection(dsStaging);
        // IDataSet stagingDataSet = new XmlDataSet(new FileInputStream(new File(Mantis6098IntegrationTest.class.getResource("/mantis6098/staging.xml").toURI())));
        // IDataSet stagingDataSet = new FlatXmlDataSet(new FileInputStream(new File(Mantis6098IntegrationTest.class.getResource("/mantis6098/staging-flat.xml").toURI())));
        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(Mantis6098IntegrationTest.class.getResource("/mantis6098/staging-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);

        assumeTrue("Er zijn 3 STAGING_OK berichten", 3l == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));
        assumeTrue("Er zijn 3 STAGING_OK laadprocessen", 3l == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        /* cleanup rsgb, doet:
                DELETE FROM brondocument;
                DELETE FROM herkomst_metadata;
                DELETE FROM subject;
                DELETE FROM prs;
                DELETE FROM kad_perceel_archief;
                DELETE FROM kad_perceel;
                DELETE FROM kad_onrrnd_zk_archief;
                DELETE FROM kad_onrrnd_zk;
         dus omgekeerde volgorde tov. array
         */
        DatabaseOperation.DELETE_ALL.execute(rsgb, new DefaultDataSet(new DefaultTable[]{
            new DefaultTable("kad_onrrnd_zk"),
            new DefaultTable("kad_onrrnd_zk_archief"),
            new DefaultTable("kad_perceel"),
            new DefaultTable("kad_perceel_archief"),
            new DefaultTable("prs"),
            new DefaultTable("subject"),
            new DefaultTable("herkomst_metadata"),
            new DefaultTable("brondocument")}
        ));
        rsgb.close();
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

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er zijn geen actuele percelen", 0, kad_perceel.getRowCount());

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Er zijn geen actuele onroerende zaken", 0, kad_onrrnd_zk.getRowCount());

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals("Er zijn geen brondocumenten", 0, brondocument.getRowCount());
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

        assertEquals("Twee berichten zijn OK getransformeerd", 2l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assertEquals("Een bericht is niet getransformeerd", 1l, brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));

        Thread t2 = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_IDS, new long[]{mutatie}, null);
        t2.join();

        assertEquals("Twee berichten zijn OK getransformeerd", 2l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assertEquals("Een bericht is outdated", 1l, brmo.getCountBerichten(null, null, "brk", "RSGB_OUTDATED"));

        ITable kad_perceel = rsgb.createDataSet().getTable("kad_perceel");
        assertEquals("Er zijn geen actuele percelen", 0, kad_perceel.getRowCount());

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Er zijn geen actuele onroerende zaken", 0, kad_onrrnd_zk.getRowCount());

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertEquals("Er zijn geen brondocumenten", 0, brondocument.getRowCount());
    }

}
