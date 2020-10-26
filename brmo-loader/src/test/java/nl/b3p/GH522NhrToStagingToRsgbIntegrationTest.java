package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Integratie test om een nHR dataservice soap bericht te laden en te
 * transformeren, bevat een testcase voor laden van nevenvestigingen.
 * <br>Draaien met:
 * {@code mvn -Dit.test=GH522NhrToStagingToRsgbIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl :brmo-loader > /tmp/postgresql.log}
 * voor bijvoorbeeld PostgreSQL
 */
public class GH522NhrToStagingToRsgbIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(GH522NhrToStagingToRsgbIntegrationTest.class);
    private final Lock sequential = new ReentrantLock(true);
    private BrmoFramework brmo;
    // dbunit
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // #1 A.S. Watson (Health & Beauty Continental Europe) B.V.
                arguments("/nhr-v3/2020-04-30-120558-31035585.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 1188},
                        // maar 1 hoofdvestiging
                        {"select sc_identif as hoofdvestiging from vestg where fk_19mac_kvk_nummer is not null", 1},
                        {"select sc_identif as hoofdvestiging from vestg where hoofdvestiging is null", 0},
                        {"select sc_identif as hoofdvestiging from vestg where hoofdvestiging = 'Ja'", 1},
                        {"select sc_identif as hoofdvestiging from vestg where hoofdvestiging = 'Nee'", 1188 - 1},
                        {"functionaris", 1 + 7},
                        {"select * from subject where identif='nhr.comVestg.000017320496' and pa_postadres_postcode='3927ZL' and pa_postadrestype='P' and pa_postbus__of_antwoordnummer='34'", 1},
                        {"select * from subject where identif='nhr.comVestg.000019946252' and fk_15aoa_identif='0482010001255998'", 1},
                })),
                // #2 HEMA B.V.
                arguments("/nhr-v3/2020-04-30-121846-34215639.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"vestg", 281},
                })),
                // #3 Boekenvoordeel B.V.
                arguments("/nhr-v3/2020-04-30-121909-39082874.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"vestg", 87},
                        {"select * from subject where identif='nhr.comVestg.000016834623' and fk_15aoa_identif='0034010000049149'", 1},
                        {"select * from subject where identif='nhr.comVestg.000016833503' and fk_15aoa_identif='0758010000023411'", 1},
                })),
                // #4 FrieslandCampina Nederland B.V.
                arguments("/nhr-v3/2020-04-30-121929-01070163.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"vestg", 37},
                        {"select * from subject where identif='nhr.comVestg.000019021232' and pa_postadres_postcode='1850AB' and pa_postadrestype='P' and pa_postbus__of_antwoordnummer='53'", 1},
                })),
                // #5 Prysmian Netherlands B.V.
                arguments("/nhr-v3/2020-04-30-121952-58087850.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"vestg", 3},
                })),
                // #6 Chubb
                arguments("/nhr-v3/33257455,23052007.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 9},
                        // maar 1 hoofdvestiging
                        {"select sc_identif as hoofdvestiging from vestg where fk_19mac_kvk_nummer is not null", 1},
                        {"select sc_identif as hoofdvestiging from vestg where hoofdvestiging = 'Ja'", 1},
                        {"select sc_identif as hoofdvestiging from vestg where hoofdvestiging = 'Nee'", 9 - 1},
                        {"functionaris", 9 /*unieke BSN */ + 1/*rsin*/},
                })),
                // #7 B3Partners
                arguments("/nhr-v3/34122633,32076598.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 1},
                        // maar 1 hoofdvestiging
                        {"select sc_identif as hoofdvestiging from vestg where fk_19mac_kvk_nummer is not null", 1},
                        {"functionaris", 1/*rsin*/},
                })),
                // #8 min EZ. (nietCommercieleVestiging)
                arguments("/nhr-v3/52813150.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 1 + 7},
                        // maar 1 hoofdvestiging
                        {"select sc_identif as hoofdvestiging from vestg where fk_19mac_kvk_nummer is not null", 1},
                        {"select sc_identif as hoofdvestiging from vestg where hoofdvestiging is null", 0},
                        {"select sc_identif as hoofdvestiging from vestg where hoofdvestiging = 'Ja'", 1},
                        {"select sc_identif as hoofdvestiging from vestg where hoofdvestiging = 'Nee'", 7},
                        {"functionaris", 0},
                }))
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
        dsStaging.setConnectionProperties(params.getProperty("staging.options", ""));

        BasicDataSource dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(params.getProperty("rsgb.jdbc.url"));
        dsRsgb.setUsername(params.getProperty("rsgb.user"));
        dsRsgb.setPassword(params.getProperty("rsgb.passwd"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);
        dsRsgb.setConnectionProperties(params.getProperty("rsgb.options", ""));

        staging = new DatabaseDataSourceConnection(dsStaging);
        rsgb = new DatabaseDataSourceConnection(dsRsgb);

        if (this.isMsSQL) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), params.getProperty("rsgb.user").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            // we hebben alleen nog postgres over
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(NhrToStagingToRsgbIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        assumeTrue(0L == brmo.getCountBerichten(null, null, "nhr", "STAGING_OK"), "Er zijn geen STAGING_OK berichten");
        assumeTrue(0L == brmo.getCountLaadProcessen(null, null, "nhr", "STAGING_OK"), "Er zijn geen STAGING_OK laadprocessen");
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        CleanUtil.cleanSTAGING(staging, false);
        staging.close();
        CleanUtil.cleanRSGB_NHR(rsgb);
        rsgb.close();
        sequential.unlock();
    }

    @DisplayName("NHR to STAGING to RSGB")
    @ParameterizedTest(name = "case #{index}: bestand: {0}")
    @MethodSource("argumentsProvider")
    public void testNhrXMLToStagingToRsgb(String bestandNaam, Map<String, Integer> rowCounts) throws Exception {

        assumeFalse(null == GH522NhrToStagingToRsgbIntegrationTest.class.getResource(bestandNaam), "Het test bestand moet er zijn.");

        brmo.loadFromFile("nhr", GH522NhrToStagingToRsgbIntegrationTest.class.getResource(bestandNaam).getFile(), null);
        LOG.info("klaar met laden van berichten in staging DB.");

        // alleen het eerste bericht heeft br_orgineel_xml, de rest niet
        ITable bericht = staging.createQueryTable("bericht", "select * from bericht where volgordenummer=0");
        assertEquals(1, bericht.getRowCount(), "Er zijn meer of minder dan 1 rij");
        LOG.debug("\n\n" + bericht.getValue(0, "br_orgineel_xml") + "\n\n");
        assertNotNull(bericht.getValue(0, "br_orgineel_xml"), "BR origineel xml is null");
        Object berichtId = bericht.getValue(0, "id");

        bericht = staging.createQueryTable("bericht", "select * from bericht where object_ref like 'nhr.%Vestg%'");
        assertEquals(rowCounts.get("vestg"), Integer.valueOf(bericht.getRowCount()), "aantal (niet)commerciele vestiging berichten onjuist");

        LOG.info("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        // na de verwerking moet soap payload er ook nog zijn
        bericht = staging.createQueryTable("bericht", "select * from bericht where br_orgineel_xml is not null");
        assertEquals(1, bericht.getRowCount(), "Er zijn meer of minder dan 1 rij");
        assertNotNull(bericht.getValue(0, "br_orgineel_xml"), "BR origineel xml is null na transformatie");
        assertEquals(berichtId, bericht.getValue(0, "id"), "bericht met br_orgineel_xml moet hetzelfde id hebben na transformatie");

        // check RSGB tabellen voor aantallen
        for (String table : rowCounts.keySet()) {
            checkRowCount(table, rowCounts.get(table));
        }
    }

    private void checkRowCount(String table, int aantal) throws SQLException, DataSetException {
        ITable _tbl;
        if (table.startsWith("select ")) {
            List<String> words = Arrays.asList(table.split(" "));
            _tbl = rsgb.createQueryTable(words.get(words.indexOf(table) + 1), table);
        } else {
            _tbl = rsgb.createTable(table);
        }

        assertEquals(aantal, _tbl.getRowCount(), "aantal '" + table + "' records klopt niet");
    }
}
