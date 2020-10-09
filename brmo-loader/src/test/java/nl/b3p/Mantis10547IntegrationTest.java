/*
 * Copyright (C) 2018 B3Partners B.V.
 */
package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.dbcp.BasicDataSource;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * testcases voor mantis 10547; datumwaardes in archieftabellen hebben
 * verschillende formatting (wat niet moet). Draaien met:
 * {@code mvn -Dit.test=Mantis10547IntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=Mantis10547IntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl brmo-loader > /tmp/postgresql.log}
 * voor postgresql.
 *
 * @author mprins
 */
public class Mantis10547IntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Mantis10547IntegrationTest.class);

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // {"bestandsNaam", aantalProcessen, aantalBerichten },
                arguments("/mantis6166/staging-69660669770000.xml", 4, 4),
                arguments("/mantis6166/staging-66860489870000.xml", 4, 4)
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
            Assertions.fail("Geen ondersteunde database aangegegeven.");
        }
        sequential.lock();

        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);
    }

    @AfterEach
    public void cleanup() throws Exception {
        if (brmo != null) {
            // in geval van niet waar gemaakte assumptions
            brmo.closeBrmoFramework();
        }
        CleanUtil.cleanRSGB_BRK(rsgb, true);
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
     * transformeer alle berichtenen ga dan de data testen.
     *
     * @throws Exception if any
     */
    @ParameterizedTest(name = "{index}: verwerken bestand: {0}")
    @MethodSource("argumentsProvider")
    public void testAll(final String bestandsNaam, final long aantalProcessen, final long aantalBerichten) throws Exception {
        assumeTrue(Mantis6166IntegrationTest.class.getResource(bestandsNaam) != null,
                "Het bestand met testdata zou moeten bestaan.");
        IDataSet stagingDataSet = new XmlDataSet(new FileInputStream(new File(Mantis6166IntegrationTest.class.getResource(bestandsNaam).toURI())));
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

        final Pattern pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        for (int i = 0; i < aantalBerichten - 1; i++) {
            LOG.debug("dat_beg_geldh/datum_einde_geldh voor rij: " + 1 + ": "
                    + kad_onrrnd_zk_archief.getValue(i, "dat_beg_geldh") + "/"
                    + kad_onrrnd_zk_archief.getValue(i, "datum_einde_geldh"));
            assertTrue((pattern.matcher("" + kad_onrrnd_zk_archief.getValue(i, "dat_beg_geldh"))).find(),
                    "'dat_beg_geldh' patroon klopt niet");
            assertTrue((pattern.matcher("" + kad_onrrnd_zk_archief.getValue(i, "datum_einde_geldh"))).find(),
                    "'datum_einde_geldh' patroon klopt niet");
        }
    }
}
