/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
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
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import org.junit.experimental.categories.Category;

/**
 * Testcases voor mantis-6235; incorrect parsen van VVE identificatie. Draaien
 * met:
 * {@code mvn -Dit.test=Mantis6235IntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle.
 *
 * <strong>NB.</strong> werkt niet op mssql, althans niet met de jTDS driver
 * omdat die geen JtdsPreparedStatement#setNull() methode heeft.
 *
 * @author mprins
 */
@Category(JTDSDriverBasedFailures.class)
public class Mantis6235IntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Mantis6235IntegrationTest.class);

    private BrmoFramework brmo;

    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;

    private final Lock sequential = new ReentrantLock();

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

        IDataSet stagingDataSet = new XmlDataSet(new FileInputStream(new File(Mantis6166IntegrationTest.class.getResource("/mantis6235/staging.xml").toURI())));

        sequential.lock();

        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }

        brmo = new BrmoFramework(dsStaging, dsRsgb);

        assumeTrue("Er zijn 2 STAGING_OK berichten", 2l == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"));
        assumeTrue("Er is 1 STAGING_OK laadproces", 1l == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanRSGB(rsgb);
        rsgb.close();

        CleanUtil.cleanSTAGING(staging);
        staging.close();

        sequential.unlock();
    }

    /**
     * transformeer alle berichten.
     *
     * @throws Exception if any
     */
    @Test
    public void testTransformStandberichten() throws Exception {
        brmo.setOrderBerichten(false);
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals("Niet alle berichten zijn OK getransformeerd", 2l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assertEquals("Niet alle berichten zijn OK getransformeerd", 0l, brmo.getCountBerichten(null, null, "brk", "RSGB_NOK"));

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertTrue("Er zijn geen brondocumenten", brondocument.getRowCount() > 0);

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Er zijn geen actuele onroerende zaken", 2, kad_onrrnd_zk.getRowCount());

        ITable app_re = rsgb.createDataSet().getTable("app_re");
        assertEquals("Er zijn geen actuele appartmentsrechten", 2, app_re.getRowCount());

        assertEquals("Eerste rij bevat niet verwachte VVE 'fk_2nnp_sc_identif'", "NL.KAD.Persoon.58193932", app_re.getValue(0, "fk_2nnp_sc_identif").toString());
        assertEquals("Tweede rij bevat niet verwachte VVE 'fk_2nnp_sc_identif'", "NL.KAD.Persoon.58201049", app_re.getValue(1, "fk_2nnp_sc_identif").toString());
    }
}
