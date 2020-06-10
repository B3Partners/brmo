package nl.b3p.brmo.datamodel;

import nl.b3p.brmo.test.util.database.ViewUtils;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeNotNull;

/**
 * test of database versie klopt en of alle materialized views er zijn.
 */
public class MaterializedViewsTest {
    private static final Log LOG = LogFactory.getLog(MaterializedViewsTest.class);
    private static String currentVersion;
    private IDatabaseConnection db;
    private BasicDataSource ds;
    /**
     * properties uit {@code <DB smaak>.properties} en
     * {@code local.<DB smaak>.properties}.
     *
     * @see #loadProps()
     */
    protected final Properties params = new Properties();

    /**
     * {@code true} als we met een Oracle database bezig zijn.
     */
    protected boolean isOracle;

    /**
     * {@code true} als we met een MS SQL Server database bezig zijn.
     */
    protected boolean isMsSQL;

    /**
     * {@code true} als we met een Postgis database bezig zijn.
     */
    protected boolean isPostgis;

    /**
     * init versienummer.
     */
    @BeforeClass
    public static void getEnvironment() {
        currentVersion = System.getProperty("project.version");
        LOG.debug("Werkversie is: " + currentVersion);
    }

    /**
     * test of de database properties zijn aangegeven, zo niet dan skippen we
     * alle tests in deze test.
     */
    @BeforeClass
    public static void checkDatabaseIsProvided() {
        assumeNotNull("Verwacht database omgeving te zijn aangegeven.", System.getProperty("database.properties.file"));
    }

    @Before
    public void setUpDB() throws Exception {
        this.loadProps();
        ds = new BasicDataSource();
        ds.setUrl(params.getProperty("rsgb.url"));
        ds.setUsername(params.getProperty("rsgb.username"));
        ds.setPassword(params.getProperty("rsgb.password"));
        ds.setAccessToUnderlyingConnectionAllowed(true);
        db = new DatabaseDataSourceConnection(ds);

        if (this.isMsSQL) {
            db.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            // db = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(db.getConnection()), params.getProperty(this.dbName + ".user").toUpperCase());
            db.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            db.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            db.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegegeven.");
        }

    }

    @After
    public void cleanup() throws SQLException {
        db.close();
        ds.close();
    }

    /**
     * test of het versienummer in de database juist is.
     *
     * @throws SQLException     als opzoeken in de dabase mislukt
     * @throws DataSetException if any
     */
    @Test
    public void testCurrentVersion() throws SQLException, DataSetException {
        ITable metadata = db.createTable("brmo_metadata");
        int rowCount = metadata.getRowCount();
        assertTrue("Verwacht tenminste 1 records.", (rowCount >= 1));

        boolean foundVersion = false;
        String waarde, naam;
        for (int i = 0; i < rowCount; i++) {
            waarde = (String) metadata.getValue(i, "waarde");
            naam = (String) metadata.getValue(i, "naam");
            LOG.debug(String.format("rsgb metadata tabel record: %d: naam: %s, waarde: %s", i, naam, waarde));
            if ("brmoversie".equalsIgnoreCase(naam)) {
                assertEquals("BRMO versinummer klopt niet", currentVersion, waarde);
                foundVersion = true;
            }
        }
        assertTrue("Geen versienummer gevonden voor rsgb", foundVersion);
    }

    /**
     * test of de bekende set met materialized in de database bestaan.
     *
     * @throws SQLException als opzoeken in de dabase mislukt
     */
    @Test
    public void testBasisMViews() throws SQLException {
        List<String> viewsFound = ViewUtils.listAllMaterializedViews(ds);
        assertNotNull("Geen materialized views gevonden", viewsFound);

        if (isMsSQL) {
            // geen m-views in mssql
            assertTrue("Gek! sqlserver heeft materialized views", viewsFound.isEmpty());
        } else {
            List<String> views = Arrays.asList(
                    // bag
                    "mb_adres",
                    "mb_pand",
                    "mb_benoemd_obj_adres",
                    "mb_ben_obj_nevenadres",
                    // brk
                    "mb_subject",
                    "mb_avg_subject",
                    "mb_util_app_re_kad_perceel",
                    "mb_kad_onrrnd_zk_adres",
                    "mb_percelenkaart",
                    "mb_zr_rechth",
                    "mb_avg_zr_rechth",
                    "mb_koz_rechth",
                    "mb_avg_koz_rechth",
                    "mb_kad_onrrnd_zk_archief"
            );

            // alles lower-case (ORACLE!) en gesorteerd vergelijken
            viewsFound.replaceAll(String::toLowerCase);
            views.replaceAll(String::toLowerCase);
            Collections.sort(viewsFound);
            Collections.sort(views);
            assertEquals("lijsten met materialized views zijn ongelijk", views, viewsFound);
        }
    }

    /**
     * Laadt de database propery file en eventuele overrides.
     *
     * @throws IOException als laden van property file mislukt
     * @todo naar superklasse extraheren
     */
    public void loadProps() throws IOException {
        // de `database.properties.file` is in de pom.xml of via commandline ingesteld
        params.load(MaterializedViewsTest.class.getClassLoader()
                .getResourceAsStream(System.getProperty("database.properties.file")));
        try {
            // probeer een local (override) versie te laden als die bestaat
            params.load(MaterializedViewsTest.class.getClassLoader()
                    .getResourceAsStream("local." + System.getProperty("database.properties.file")));
        } catch (IOException | NullPointerException e) {
            // negeren; het override bestand is normaal niet aanwezig
        }
        isOracle = "oracle".equalsIgnoreCase(params.getProperty("dbtype"));
        isMsSQL = "sqlserver".equalsIgnoreCase(params.getProperty("dbtype"));
        isPostgis = "postgis".equalsIgnoreCase(params.getProperty("dbtype"));

        try {
            Class stagingDriverClass = Class.forName(params.getProperty("jdbc.driverClassName"));
        } catch (ClassNotFoundException ex) {
            LOG.error("Database driver niet gevonden.", ex);
        }
    }

}
