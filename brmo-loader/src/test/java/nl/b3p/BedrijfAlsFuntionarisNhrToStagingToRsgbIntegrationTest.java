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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;

/**
 * Integratie test om een nHR dataservice soap bericht te laden en te
 * transformeren, bevat testcases voor bedrijven met bedrijf als functionaris.
 * <br>Draaien met:
 * {@code mvn -Dit.test=BedrijfAlsFuntionarisNhrToStagingToRsgbIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl :brmo-loader > /tmp/postgresql.log}
 * voor bijvoorbeeld PostgreSQL
 */
@RunWith(Parameterized.class)
public class BedrijfAlsFuntionarisNhrToStagingToRsgbIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BedrijfAlsFuntionarisNhrToStagingToRsgbIntegrationTest.class);
    private final Lock sequential = new ReentrantLock(true);
    private final String bestandNaam;
    private final Map<String, Integer> rowCounts;
    private BrmoFramework brmo;

    // dbunit
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;

    public BedrijfAlsFuntionarisNhrToStagingToRsgbIntegrationTest(String bestandNaam, Map<String, Integer> rowCounts) {
        this.bestandNaam = bestandNaam;
        this.rowCounts = rowCounts;
    }

    @Parameterized.Parameters(name = "{index}: type: {0}, bestand: {1}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
                // Eldon N.V. / 01037917 / een buitenlandseVennootschap als functionaris
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-153436-01037917.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 2},
                        // maar 1 hoofdvestiging
                        {"select sc_identif as hoofdvestiging from vestg where fk_19mac_kvk_nummer is not null", 1},
                        {"select sc_identif as hoofdvestiging from vestg where hoofdvestiging = 'Ja'", 1},
                        {"select sc_identif as hoofdvestiging from vestg where hoofdvestiging = 'Nee'", 1},
                        {"functionaris", 5},
                        {"select * from functionaris where fk_sc_lh_pes_sc_identif='nhr.buitenlVenn.naam.782f4c4bef5a6bf48206ed2c6c6b9fbe138af4a7' and functie='Enig aandeelhouder'", 1},
                        {"select * from subject where identif='nhr.buitenlVenn.naam.782f4c4bef5a6bf48206ed2c6c6b9fbe138af4a7'", 1},
                        {"ander_btnlnds_niet_nat_prs", 1},
                })},
                // BVNN/Boskalis Dolman V.O.F. / 01083730 / 2 samenwerkingsverbanden als functionaris
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154135-01083730.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 2},
                        {"functionaris", 3},
                })},
                // Maatschap Rozema-Kist / 5 natuurlijke personen in samenwerkingsverband
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154143-01173390.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 2},
                        {"functionaris", 5},
                })},
                // Tiktak/Segafredo Zanetti Nederland B.V. / 4 nat. personen als functionaris, met een aantal buitenlandse pers. en een buitenlandseVennootschap
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154147-02000099.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 1},
                        {"functionaris", 4},
                })},
                // Visser Transport B.V. / heeft een buitenlandseVennootschap als functionaris met een ampersand in de naam..
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154151-02014331.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 3},
                        {"functionaris", 3},
                        {"ander_btnlnds_niet_nat_prs", 1},
                })},
                // Etex Building Performance B.V. / heeft een buitenlandseVennootschap als functionaris + een aantal andere personen
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154155-02320379.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 2},
                        {"functionaris", 11},
                        {"ander_btnlnds_niet_nat_prs", 1},
                })},
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154200-02328563.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 2},
                        {"functionaris", 6},
                })},
                // Handelsonderneming Koen Meijer B.V. /
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154203-02334440.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 2},
                        {"functionaris", 1},
                })},
                // Aareon Nederland B.V. / buitenlandseVennootschap en natuurlijke personen als functionaris
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154209-04026125.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 5},
                        {"functionaris", 16},
                        {"ander_btnlnds_niet_nat_prs", 1},
                })},
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154213-04033993.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 3},
                        {"functionaris", 2},
                })},
                // KGH Customs Services B.V.
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154218-04040443.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 3},
                        {"functionaris", 9},
                        {"ander_btnlnds_niet_nat_prs", 1},
                })},
                // Animal Lovers B.V.
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154222-04058882.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 2},
                        {"ander_btnlnds_niet_nat_prs", 3},
                        {"functionaris", 4},
                })},
                // Molecaten Horeca B.V.
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154225-08059166.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 9},
                        {"ander_btnlnds_niet_nat_prs", 1},
                        {"functionaris", 2},
                })},
                // DNV GL Netherlands B.V.
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154229-09006404.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 2},
                        {"ander_btnlnds_niet_nat_prs", 1},
                        {"functionaris", 10},
                })},
                // Solar Nederland B.V.
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154232-09013687.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 18},
                        {"ander_btnlnds_niet_nat_prs", 1},
                        {"functionaris", 6},
                })},
                // Eismann B.V.
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154235-09049151.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 6},
                        {"ander_btnlnds_niet_nat_prs", 1},
                        {"functionaris", 4},
                })},
                // Sonoco Alcore Nederland B.V.
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154239-09107097.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 2},
                        {"ander_btnlnds_niet_nat_prs", 1},
                        {"functionaris", 4},
                })},
                // DPD (Nederland) B.V.
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154242-09118128.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 11},
                        {"ander_btnlnds_niet_nat_prs", 1},
                        // er zitten subjecten in met >1 functionaris rol
                        {"functionaris", 14},
                })},
                // Melspring International B.V.
                {"/nhr-v3/bedrijf-als-functionaris/2020-06-18-154245-09131564.anon.xml", MapUtils.putAll(new HashMap<String, Integer>(), new Object[][]{
                        {"maatschapp_activiteit", 1},
                        {"vestg", 3},
                        // "Melspring-Farmershouse" en "Melspring international" hebben hetzelfde vestigingsnummer
                        {"vestg_naam", 4},
                        {"ander_btnlnds_niet_nat_prs", 1},
                        // er zitten subjecten in met >1 functionaris rol
                        {"functionaris", 5},
                        {"ingeschr_nat_prs", 4},
                        {"niet_nat_prs", 3},
                        {"ingeschr_niet_nat_prs", 2},
                        {"subject", 10},
                })},
        });
    }

    @Before
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

        assumeTrue("Er zijn geen STAGING_OK berichten", 0l == brmo.getCountBerichten(null, null, "nhr", "STAGING_OK"));
        assumeTrue("Er zijn geen STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, "nhr", "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        CleanUtil.cleanSTAGING(staging, false);
        staging.close();
        CleanUtil.cleanRSGB_NHR(rsgb);
        rsgb.close();
        sequential.unlock();
    }

    @Test
    public void testNhrXMLToStagingToRsgb() throws Exception {

        assumeNotNull("Het test bestand moet er zijn.", BedrijfAlsFuntionarisNhrToStagingToRsgbIntegrationTest.class.getResource(bestandNaam));

        brmo.loadFromFile("nhr", BedrijfAlsFuntionarisNhrToStagingToRsgbIntegrationTest.class.getResource(bestandNaam).getFile(), null);
        LOG.info("klaar met laden van berichten in staging DB.");

        // alleen het eerste bericht heeft br_orgineel_xml, de rest niet
        ITable bericht = staging.createQueryTable("bericht", "select * from bericht where volgordenummer=0");
        assertEquals("Er zijn meer of minder dan 1 rij", 1, bericht.getRowCount());
        LOG.debug("\n\n" + bericht.getValue(0, "br_orgineel_xml") + "\n\n");
        assertNotNull("BR origineel xml is null", bericht.getValue(0, "br_orgineel_xml"));
        Object berichtId = bericht.getValue(0, "id");

        bericht = staging.createQueryTable("bericht", "select * from bericht where object_ref like 'nhr.%Vestg%'");
        assertEquals("aantal (niet)commerciele vestiging berichten onjuist", rowCounts.get("vestg"), Integer.valueOf(bericht.getRowCount()));

        LOG.info("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        // na de verwerking moet soap payload er ook nog zijn
        bericht = staging.createQueryTable("bericht", "select * from bericht where br_orgineel_xml is not null");
        assertEquals("Er zijn meer of minder dan 1 rij", 1, bericht.getRowCount());
        assertNotNull("BR origineel xml is null na transformatie", bericht.getValue(0, "br_orgineel_xml"));
        assertEquals("bericht met br_orgineel_xml moet hetzelfde id hebben na transformatie", berichtId, bericht.getValue(0, "id"));

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

        assertEquals("aantal '" + table + "' records klopt niet", aantal, _tbl.getRowCount());
    }
}
