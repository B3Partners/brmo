package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
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
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
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
 * Draaien met:
 * {@code mvn clean -pl :brmo-loader -Dit.test=BAG20XMLToStagingIntegrationTest -Dtest.onlyITs=true verify -Pmssql > /tmp/mssql.log}
 * voor bijvoorbeeld MSSQL of
 * {@code mvn clean -pl :brmo-loader -Dit.test=BAG20XMLToStagingIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > /tmp/pgsql.log}.
 *
 * @author mprins
 */
public class BAG20XMLToStagingIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BAG20XMLToStagingIntegrationTest.class);
    private final Lock sequential = new ReentrantLock();
    private IDatabaseConnection staging;
    private BrmoFramework brmo;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // ("type","filename", aantalBerichten, aantalLaadProcessen),
                // STAND
                arguments("bag20", "/bag-2.0/WPL.xml", 1, 1),
                arguments("bag20", "/bag-2.0/PND.xml", 1, 1),
                arguments("bag20", "/bag-2.0/OPR.xml", 1, 1),
                arguments("bag20", "/bag-2.0/NUM.xml", 1, 1),
                arguments("bag20", "/bag-2.0/LIG.xml", 1, 1),
                arguments("bag20", "/bag-2.0/STA.xml", 1, 1),
                arguments("bag20", "/bag-2.0/VBO.xml", 1, 1),
                // leeg bestand
                arguments("bag20", "/bag-2.0/STA01042020_000001-leeg.xml", 0, 1),
                // grep -o "Objecten:Pand" 0106PND01012020_000001.xml | wc -w = 10000
                // er zijn 1477 berichten die een bestaand object/bericht bijwerken, dus "dubbel" of meer
                arguments("bag20", "/bag-2.0/0106PND01012020_000001.xml", 10000 / 2 - 1477, 1)
                // MUTATIES
        );
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        BasicDataSource dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.jdbc.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.passwd"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        brmo = new BrmoFramework(dsStaging, null, null);
        staging = new DatabaseDataSourceConnection(dsStaging);

        if (this.isMsSQL) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(BAG20XMLToStagingIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        Assumptions.assumeTrue(0L == brmo.getCountBerichten(null, null, "bag20", "STAGING_OK"),
                "Er zijn geen STAGING_OK berichten");
        Assumptions.assumeTrue(0L == brmo.getCountLaadProcessen(null, null, "bag20", "STAGING_OK"),
                "Er zijn geen STAGING_OK laadprocessen");
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        CleanUtil.cleanSTAGING(staging, false);
        staging.close();
        sequential.unlock();
    }

    @DisplayName("BAG 2.0 stand in staging")
    @ParameterizedTest(name = "{index}: type: {0}, bestand: {1}")
    @MethodSource("argumentsProvider")
    public void testBagStandToStaging(String bestandType, String bestandNaam, long aantalBerichten, long aantalProcessen) throws BrmoException {
        try {
            brmo.loadFromFile(bestandType, BAG20XMLToStagingIntegrationTest.class.getResource(bestandNaam).getFile(), null);
        } catch (BrmoLeegBestandException blbe) {
            LOG.info("Er is een bestand zonder berichten geladen (kan voorkomen). " + blbe.getLocalizedMessage());
        }

        Assertions.assertEquals(aantalBerichten, brmo.getCountBerichten(null, null, bestandType, "STAGING_OK"),
                "Verwacht aantal berichten");
        Assertions.assertEquals(aantalProcessen, brmo.getCountLaadProcessen(null, null, bestandType, "STAGING_OK"),
                "Verwacht aantal laadprocessen");
    }
}
