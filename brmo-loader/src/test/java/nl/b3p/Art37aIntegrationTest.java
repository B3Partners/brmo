package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author mprins
 */
/**
 * testcase voor bericht met afgeschermde persoonsgegevens.
 *
 * Draaien met:
 * {@code mvn -Dit.test=Art37aIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * voor PostgreSQL.
 *
 * @author mprins
 */
public class Art37aIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(Art37aIntegrationTest.class);

    private final String bestandNaam = "/BRK-art37a/MUTBX01-ASD13Q7456A18-20181011-1-AlletypenNP-Afgeschermd.xml";

    private BrmoFramework brmo;

    // dbunit
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;

    private final Lock sequential = new ReentrantLock(true);

    @Before
    @Override
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

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(BrkToStagingToRsgbIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        assumeTrue("Er zijn geen STAGING_OK berichten", 0l == brmo.getCountBerichten(null, null, BrmoFramework.BR_BRK, "STAGING_OK"));
        assumeTrue("Er zijn geen STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRK, "STAGING_OK"));
    }

    /**
     * Opruimen na afloop van de test.
     *
     * @throws Exception if any
     */
    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging);
        staging.close();

        CleanUtil.cleanRSGB_BRK(rsgb, true);
        rsgb.close();

        sequential.unlock();
    }

    @Test
    public void testAfgeschermdBericht() throws BrmoException, InterruptedException, SQLException, DataSetException {
        assumeNotNull("Het test bestand moet er zijn.", Art37aIntegrationTest.class.getResource(bestandNaam));

        brmo.loadFromFile(BrmoFramework.BR_BRK, Art37aIntegrationTest.class.getResource(bestandNaam).getFile(), null);
        LOG.debug("klaar met laden van berichten in staging DB.");

        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull("De verzameling berichten bestaat niet.", berichten);
        assertEquals("Het aantal berichten is niet als verwacht.", 1, berichten.size());
        assertNotNull("De verzameling processen bestaat niet.", processen);
        assertEquals("Het aantal processen is niet als verwacht.", 1, processen.size());

        LOG.debug("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals("Niet alle berichten zijn OK getransformeerd", 1, brmo.getCountBerichten(null, null, BrmoFramework.BR_BRK, "RSGB_OK"));
        berichten = brmo.listBerichten();
        for (Bericht b : berichten) {
            assertNotNull("Bericht is 'null'", b);
            assertNotNull("'db-xml' van bericht is 'null'", b.getDbXml());
        }

        // test RSGB tabellen
        final List<String> kadaster_indentifs = Arrays.asList("NL.KAD.Tenaamstelling.1000020370", "NL.KAD.ZakelijkRecht.AKR1.1447782");
        final List<String> sc_indentifs = Arrays.asList("NL.KAD.Persoon.888888888888888", "NL.KAD.Persoon.172059846");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals("Het aantal onroerende zaak is incorrect", 1, kad_onrrnd_zk.getRowCount());
        assertEquals("Datum eerste record komt niet overeen", "2018-10-11", kad_onrrnd_zk.getValue(0, "dat_beg_geldh"));
        assertEquals("kad_identif eerste record komt niet overeen", "11540745610018", kad_onrrnd_zk.getValue(0, "kad_identif").toString());

        ITable zak_recht = rsgb.createDataSet().getTable("zak_recht");
        assertEquals("Het aantal zak_recht is incorrect", 2, zak_recht.getRowCount());
        for (int row = 0; row < zak_recht.getRowCount(); row++) {
            assertTrue("Gevonden waarde zit niet in de lijst verwachte waarden.",
                    kadaster_indentifs.contains(zak_recht.getValue(row, "kadaster_identif").toString())
            );
        }

        // subject structuur
        // er zitten meerdere natuurlijk personen in het bericht maar allemaal met dezelfde identifier, dus die worden overschreven, er blijft 1 nat pers over
        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals("Het aantal natuurlijke personen is incorrect", 1, nat_prs.getRowCount());
        assertEquals("sc_identif klopt niet", "NL.KAD.Persoon.888888888888888", nat_prs.getValue(0, "sc_identif"));
        assertEquals("geslachtsaand klopt niet", "O", nat_prs.getValue(0, "geslachtsaand"));
        assertEquals("nm_geslachtsnaam klopt niet",
                "Dit is afgeschermde informatie. Verstrekking is alleen mogelijk op grond van artikel 37a Kadasterbesluit. Neem contact op met het Kadaster via (088) 183 22 43 of APGloket@kadaster.nl",
                nat_prs.getValue(0, "nm_geslachtsnaam"));
        assertEquals("nm_voornamen klopt niet", null, nat_prs.getValue(0, "nm_voornamen"));

        ITable prs = rsgb.createDataSet().getTable("prs");
        // prs bevat nat. + niet-nat. pers
        assertEquals("Het aantal personen is incorrect", 2, prs.getRowCount());
        for (int row = 0; row < prs.getRowCount(); row++) {
            assertTrue("Gevonden waarde zit niet in de lijst verwachte waarden.",
                    sc_indentifs.contains(prs.getValue(row, "sc_identif").toString())
            );
        }

    }
}
