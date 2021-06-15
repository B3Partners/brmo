package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.jdbc.util.converter.OracleConnectionUnwrapper;
import org.apache.commons.dbcp2.BasicDataSource;
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
import org.junit.jupiter.api.AfterEach;
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
 * Draaien met:
 * {@code mvn -Dit.test=BAGXMLToStagingIntegrationTest -Dtest.onlyITs=true verify -pl brmo-loader -Pmssql >
 * target/mssql.log}
 * voor bijvoorbeeld MSSQL, of
 * {@code mvn -Dit.test=BAGXMLToStagingIntegrationTest -Dtest.onlyITs=true verify -pl brmo-loader -Ppostgresql >
 * /tmp/postgresql.log}
 *
 * @author Boy de Wit
 * @author mprins
 */
public class BAGXMLToStagingIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BAGXMLToStagingIntegrationTest.class);
    private final Lock sequential = new ReentrantLock();
    private IDatabaseConnection staging;
    private BrmoFramework brmo;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // {"type","filename", aantalBerichten, aantalLaadProcessen},
                arguments("bag", "/nl/b3p/brmo/loader/xml/0197LIG01072014-01072014-000001.xml", 0, 1),
                arguments("bag", "/nl/b3p/brmo/loader/xml/0197STA01072014-01072014-000001.xml", 2, 1),
                arguments("bag", "/nl/b3p/brmo/loader/xml/9999MUT02012015-03012015.zip", 25718, 6),
                /* bestand heeft 4 berichten voor object, maar we houden alleen de laatste mutatie in staging */
                arguments("bag", "/GH-275/OPR-1884300000000464.xml", 1, 1)
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
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()),
                    params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }
        sequential.lock();
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        CleanUtil.cleanSTAGING(staging, false);
        staging.close();
        sequential.unlock();
    }

    @DisplayName("Bag stand in staging")
    @ParameterizedTest(name = "{index}: type: {0}, bestand: {1}")
    @MethodSource("argumentsProvider")
    public void testBagStandToStaging(String bestandType, String bestandNaam, long aantalBerichten,
                                      long aantalProcessen) throws Exception {
        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(
                new File(BAGXMLToStagingIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        assumeTrue(0l == brmo.getCountBerichten(null, null, "bag", "STAGING_OK"),
                "Er zijn geen STAGING_OK berichten");
        assumeTrue(0l == brmo.getCountLaadProcessen(null, null, "bag", "STAGING_OK"),
                "Er zijn geen STAGING_OK laadprocessen");


        try {
            brmo.loadFromFile(bestandType, BAGXMLToStagingIntegrationTest.class.getResource(bestandNaam).getFile(),
                    null);
        } catch (BrmoLeegBestandException blbe) {
            LOG.debug("Er is een bestand zonder berichten geladen (kan voorkomen).");
        }

        assertEquals(aantalBerichten, brmo.getCountBerichten(null, null, bestandType, "STAGING_OK"),
                "Verwacht aantal berichten");
        assertEquals(aantalProcessen, brmo.getCountLaadProcessen(null, null, bestandType, "STAGING_OK"),
                "Verwacht aantal laadprocessen");
    }
}
