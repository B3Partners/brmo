package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.jdbc.util.converter.OracleConnectionUnwrapper;
import org.apache.commons.dbcp2.BasicDataSource;
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
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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

    @BeforeEach
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

        if (this.isOracle) {
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

        brmo = new BrmoFramework(dsStaging, dsRsgb, null);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(BrkToStagingToRsgbIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        assumeTrue(0l == brmo.getCountBerichten(BrmoFramework.BR_BRK, "STAGING_OK"),
                "Er zijn geen STAGING_OK berichten");
        assumeTrue(0l == brmo.getCountLaadProcessen( BrmoFramework.BR_BRK, "STAGING_OK"),
                "Er zijn geen STAGING_OK laadprocessen");
    }

    /**
     * Opruimen na afloop van de test.
     *
     * @throws Exception if any
     */
    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging, false);
        staging.close();

        CleanUtil.cleanRSGB_BRK(rsgb, true);
        rsgb.close();

        sequential.unlock();
    }

    @Test
    public void testAfgeschermdBericht() throws BrmoException, InterruptedException, SQLException, DataSetException {
        assumeFalse(null == Art37aIntegrationTest.class.getResource(bestandNaam), "Het test bestand moet er zijn.");

        brmo.loadFromFile(BrmoFramework.BR_BRK, Art37aIntegrationTest.class.getResource(bestandNaam).getFile(), null);
        LOG.debug("klaar met laden van berichten in staging DB.");

        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull(berichten, "De verzameling berichten bestaat niet.");
        assertEquals(1, berichten.size(), "Het aantal berichten is niet als verwacht.");
        assertNotNull(processen, "De verzameling processen bestaat niet.");
        assertEquals(1, processen.size(), "Het aantal processen is niet als verwacht.");

        LOG.debug("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals(1, brmo.getCountBerichten(BrmoFramework.BR_BRK, "RSGB_OK"),
                "Niet alle berichten zijn OK getransformeerd");
        berichten = brmo.listBerichten();
        for (Bericht b : berichten) {
            assertNotNull(b, "Bericht is 'null'");
            assertNotNull(b.getDbXml(), "'db-xml' van bericht is 'null'");
        }

        // test RSGB tabellen
        final List<String> kadaster_indentifs = Arrays.asList("NL.KAD.Tenaamstelling.1000020370", "NL.KAD.ZakelijkRecht.AKR1.1447782");
        final List<String> sc_indentifs = Arrays.asList("NL.KAD.Persoon.888888888888888", "NL.KAD.Persoon.172059846");

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals(1, kad_onrrnd_zk.getRowCount(), "Het aantal onroerende zaak is incorrect");
        assertEquals("2018-10-11", kad_onrrnd_zk.getValue(0, "dat_beg_geldh"),
                "Datum eerste record komt niet overeen");
        assertEquals("11540745610018", kad_onrrnd_zk.getValue(0, "kad_identif").toString(),
                "kad_identif eerste record komt niet overeen");

        ITable zak_recht = rsgb.createDataSet().getTable("zak_recht");
        assertEquals(2, zak_recht.getRowCount(), "Het aantal zak_recht is incorrect");
        for (int row = 0; row < zak_recht.getRowCount(); row++) {
            assertTrue(
                    kadaster_indentifs.contains(zak_recht.getValue(row, "kadaster_identif").toString()),
                    "Gevonden waarde zit niet in de lijst verwachte waarden.");
        }

        // subject structuur
        // er zitten meerdere natuurlijk personen in het bericht maar allemaal met dezelfde identifier, dus die worden overschreven, er blijft 1 nat pers over
        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals(1, nat_prs.getRowCount(), "Het aantal natuurlijke personen is incorrect");
        assertEquals("NL.KAD.Persoon.888888888888888", nat_prs.getValue(0, "sc_identif"),
                "sc_identif klopt niet");
        assertEquals("O", nat_prs.getValue(0, "geslachtsaand"), "geslachtsaand klopt niet");
        assertEquals(
                "Dit is afgeschermde informatie. Verstrekking is alleen mogelijk op grond van artikel 37a Kadasterbesluit. Neem contact op met het Kadaster via (088) 183 22 43 of APGloket@kadaster.nl",
                nat_prs.getValue(0, "nm_geslachtsnaam"), "nm_geslachtsnaam klopt niet");
        assertEquals(null, nat_prs.getValue(0, "nm_voornamen"), "nm_voornamen klopt niet");

        ITable prs = rsgb.createDataSet().getTable("prs");
        // prs bevat nat. + niet-nat. pers
        assertEquals(2, prs.getRowCount(), "Het aantal personen is incorrect");
        for (int row = 0; row < prs.getRowCount(); row++) {
            assertTrue(
                    sc_indentifs.contains(prs.getValue(row, "sc_identif").toString()),
                    "Gevonden waarde zit niet in de lijst verwachte waarden.");
        }

    }
}
