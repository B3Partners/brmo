package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
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
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 *
 * Draaien met:
 * {@code mvn -Dit.test=BrkToStagingToRsgbIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl brmo-loader > /tmp/postgresql.log} of
 * {@code mvn -Dit.test=BrkToStagingToRsgbIntegrationTest -Dtest.onlyITs=true verify -Pmssql -pl brmo-loader > /tmp/mssql.log}
 * voor bijvoorbeeld MSSQL of
 * {@code mvn -Dit.test=BrkToStagingToRsgbIntegrationTest -Dtest.onlyITs=true verify -Poracle -pl brmo-loader > /tmp/oracle.log}
 * voor Oracle.
 *
 * @author Boy de Wit
 * @author mprins
 */
@Tag("skip-windows-java11")
public class BrkToStagingToRsgbIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(BrkToStagingToRsgbIntegrationTest.class);

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // {"type","filename", aantalBerichten, aantalProcessen, "datumEersteMutatie"},
                arguments("brk", "/nl/b3p/brmo/loader/xml/MUTBX01-ASN00T1660-20091119-1-singleline.xml", 1, 1, "2009-11-19"),
                arguments("brk", "/nl/b3p/brmo/loader/xml/BURBX01-ASN00-AA331-big-geom.xml", 1, 1, "2009-10-31"),
                /* dit bestand zit in de DVD Proefbestanden BRK Levering oktober 2012 (Totaalstanden)
                 * /mnt/v_b3p_projecten/BRMO/BRK/BRK_STUF_IMKAD/BRK/Levering(dvd)/Proefbestanden BRK Levering oktober
                 * 2012 (Totaalstanden)/20091130/ en staat op de git-ignore lijst omdat 't 18.5MB groot is, `grep -o KadastraalObjectSnapshot BURBX01.xml | wc -w`/2 geeft aantal berichten
                 */
                // arguments("brk", "/nl/b3p/brmo/loader/xml/BURBX01-ASN00-20091130-6000015280-9100000039.zip", (63104 / 2), 1, "2009-10-31"),
                arguments("brk", "/nl/b3p/brmo/loader/xml/BURBX01-ASN00-AA513.xml", 1, 1, "2009-10-31"),
                // dit bestand bevat een datafout in het element //GbaPersoon:aanduidingNaamgebruik/Typen:code waarin de waarde staat (en niet de code)
                arguments("brk", "/SUPPORT-12817_DATAFOUT_aanduiding_naamgebruik/BRK_XML.anon.xml", 1, 1, "2021-09-02")
            );
    }


    private BrmoFramework brmo;
    // dbunit
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;
    private BasicDataSource dsStaging;
    private BasicDataSource dsRsgb;
    private final Lock sequential = new ReentrantLock(true);

    @BeforeEach
    @Override
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

        Assumptions.assumeTrue(0L == brmo.getCountBerichten(null, null, "brk", "STAGING_OK"),
                "Er zijn geen STAGING_OK berichten");
        Assumptions.assumeTrue(0L == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"),
                "Er zijn geen STAGING_OK laadprocessen");
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging, false);
        staging.close();
        dsStaging.close();

        CleanUtil.cleanRSGB_BRK(rsgb, true);
        rsgb.close();
        dsRsgb.close();

        sequential.unlock();
    }

    @DisplayName("BRK XML in staging")
    @ParameterizedTest(name = "testBrkXMLToStaging #{index}: type: {0}, bestand: {1}, mutatiedatum: {4}")
    @MethodSource("argumentsProvider")
    public void testBrkXMLToStaging(String bestandType, String bestandNaam, long aantalBerichten, long aantalProcessen, String datumEersteMutatie) throws Exception {
        assumeFalse(null == BrkToStagingToRsgbIntegrationTest.class.getResource(bestandNaam), "Het test bestand moet er zijn.");

        brmo.loadFromFile(bestandType, BrkToStagingToRsgbIntegrationTest.class.getResource(bestandNaam).getFile(), null);
        LOG.debug("klaar met laden van berichten in staging DB.");

        List<Bericht> berichten = brmo.listBerichten();
        List<LaadProces> processen = brmo.listLaadProcessen();
        assertNotNull(berichten, "De verzameling berichten bestaat niet.");
        assertEquals(aantalBerichten, berichten.size(), "Het aantal berichten is niet als verwacht.");
        assertNotNull(processen, "De verzameling processen bestaat niet.");
        assertEquals(aantalProcessen, processen.size(), "Het aantal processen is niet als verwacht.");

        LOG.debug("Transformeren berichten naar rsgb DB.");
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals(aantalBerichten, brmo.getCountBerichten(null, null, "brk", "RSGB_OK"),
                "Niet alle berichten zijn OK getransformeerd");
        berichten = brmo.listBerichten();
        for (Bericht b : berichten) {
            assertNotNull(b, "Bericht is 'null'");
            assertNotNull(b.getDbXml(), "'db-xml' van bericht is 'null'");
        }

        ITable kad_onrrnd_zk = rsgb.createDataSet().getTable("kad_onrrnd_zk");
        assertEquals(aantalBerichten, kad_onrrnd_zk.getRowCount(), "Het aantal kad_onrrnd_zk klopt niet");
        assertEquals(datumEersteMutatie, kad_onrrnd_zk.getValue(0, "dat_beg_geldh"),
                "Datum eerste record komt niet overeen");

        if(bestandNaam.contains("BRK_XML.anon.xml")){
            // check of aand_naamgebruik leeg is voor alle personen
            ITable nat_prs =rsgb.createDataSet().getTable("nat_prs");
            assertEquals(2, nat_prs.getRowCount(), "aantal natuurlijke personen klopt niet");

            nat_prs = rsgb.createQueryTable("nat_prs","select * from nat_prs where geslachtsaand = 'M'");
            assertEquals(1, nat_prs.getRowCount(), "aantal 'M' klopt niet");
            assertNull(nat_prs.getValue(0,"aand_naamgebruik"));

            // ondanks dat bericht zegt "Eigen naam" wat een ongeldige waarde is voor //GbaPersoon:aanduidingNaamgebruik/Typen:code
            nat_prs = rsgb.createQueryTable("nat_prs","select * from nat_prs where geslachtsaand = 'V'");
            assertEquals(1, nat_prs.getRowCount(), "aantal 'V' klopt niet");
            assertNull(nat_prs.getValue(0,"aand_naamgebruik"));
        }
    }
}
