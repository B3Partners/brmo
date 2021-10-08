/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.jdbc.util.converter.OracleConnectionUnwrapper;
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
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;


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
public class Mantis6235IntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Mantis6235IntegrationTest.class);

    private BrmoFramework brmo;
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;
    private final Lock sequential = new ReentrantLock();
    private BasicDataSource dsStaging;
    private BasicDataSource dsRsgb;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.jdbc.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.passwd"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        dsRsgb = new BasicDataSource();
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

        IDataSet stagingDataSet = new XmlDataSet(new FileInputStream(new File(Mantis6235IntegrationTest.class.getResource("/mantis6235/staging.xml").toURI())));

        sequential.lock();

        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }

        brmo = new BrmoFramework(dsStaging, dsRsgb);

        assumeTrue(2l == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Er zijn 2 STAGING_OK berichten");
        assumeTrue(1l == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"),
                "Er is 1 STAGING_OK laadproces");
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanRSGB_BRK(rsgb, true);
        CleanUtil.cleanSTAGING(staging, false);
        rsgb.close();
        dsRsgb.close();
        staging.close();
        dsStaging.close();

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

        assertEquals(2l, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Niet alle berichten zijn OK getransformeerd");
        assertEquals(0l, brmo.getCountBerichten(null, null, "brk", "RSGB_NOK"),
                "Niet alle berichten zijn OK getransformeerd");

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");
        assertTrue(brondocument.getRowCount() > 0, "Er zijn geen brondocumenten");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals(2, kad_onrrnd_zk.getRowCount(), "Er zijn geen actuele onroerende zaken");

        ITable app_re = rsgb.createDataSet().getTable("app_re");
        assertEquals(2, app_re.getRowCount(), "Er zijn geen actuele appartmentsrechten");

        assertEquals("NL.KAD.Persoon.58193932", app_re.getValue(0, "fk_2nnp_sc_identif").toString(),
                "Eerste rij bevat niet verwachte VVE 'fk_2nnp_sc_identif'");
        assertEquals("NL.KAD.Persoon.58201049", app_re.getValue(1, "fk_2nnp_sc_identif").toString(),
                "Tweede rij bevat niet verwachte VVE 'fk_2nnp_sc_identif'");
    }
}
