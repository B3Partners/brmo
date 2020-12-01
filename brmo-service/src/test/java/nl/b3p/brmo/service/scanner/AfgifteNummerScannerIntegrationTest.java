/*
 * Copyright (C) 2019 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.brmo.service.scanner;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AfgifteNummerScannerProces;
import nl.b3p.brmo.service.testutil.TestUtil;
import nl.b3p.brmo.test.util.database.SequenceUtil;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.InsertIdentityOperation;
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
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * {@code mvn -Dit.test=AfgifteNummerScannerIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl brmo-service
 * > /tmp/mvn.log}
 * of
 * {@code mvn -Dit.test=AfgifteNummerScannerIntegrationTest -Dtest.onlyITs=true verify -Poracle -pl brmo-service >
 * /tmp/mvn.log}
 *
 * @author mprins
 */
public class AfgifteNummerScannerIntegrationTest extends TestUtil {

    private static final Log LOG = LogFactory.getLog(AfgifteNummerScannerIntegrationTest.class);

    private final Lock sequential = new ReentrantLock();
    private BrmoFramework brmo;
    private AfgifteNummerScanner scanner;
    private IDatabaseConnection staging;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                /* ("sBestandsNaam", lAantalLaadProcessen, iAantalOntbrekendeNummerRanges, sContractNummer,
                sContractNummers aanwezig, iAantalOntbrekendRecords) */
                arguments("/afgiftescanner/geenontbrekendenummers.xml", 49, 0, "6666666", new String[]{"6666666"}, 0),
                arguments("/afgiftescanner/geenontbrekendenummers2.xml", 11, 0, "6666666",
                        new String[]{"1111", "6666666", "999999"}, 0),
                arguments("/afgiftescanner/metontbrekendenummers.xml", 2317, 2, "999999", new String[]{"999999"},
                        1 + 4),
                arguments("/afgiftescanner/combi.xml", 2317 + 49, 2, "999999", new String[]{"6666666", "999999"},
                        1 + 4),
                arguments("/afgiftescanner/combi.xml", 2317 + 49, 0, "6666666", new String[]{"6666666", "999999"}, 0),
                arguments("/afgiftescanner/combi2.xml", 6, 0, "6666666", new String[]{"1111", "6666666", "999999"}, 0)
        );
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        staging = new DatabaseDataSourceConnection(dsStaging);

        if (this.isMsSQL) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            dsStaging.getConnection().setAutoCommit(true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()),
                    DBPROPS.getProperty("staging.username").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegeven");
        }
        brmo = new BrmoFramework(dsStaging, null);

        scanner = new AfgifteNummerScanner(new AfgifteNummerScannerProces());
        FieldUtils.writeField(scanner, "listener", new ProgressUpdateListener() {
            @Override
            public void total(long total) {
                LOG.info("Totaal verwerkt: " + total);
            }

            @Override
            public void progress(long progress) {
            }

            @Override
            public void exception(Throwable t) {
                LOG.error("Fout in AfgifteNummerScannerTest", t);
            }

            @Override
            public void updateStatus(String status) {
            }

            @Override
            public void addLog(String log) {
                LOG.info(log);
            }
        }, true);

        sequential.lock();
    }

    private void loadTestData(String sBestandsNaam, long lAantalLaadProcessen) throws Exception {
        assumeTrue(
                AfgifteNummerScannerIntegrationTest.class.getResource(sBestandsNaam) != null,
                "Het bestand met staging testdata zou moeten bestaan."
        );
        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(
                new File(AfgifteNummerScannerIntegrationTest.class.getResource(sBestandsNaam).toURI())));

        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }
        assumeTrue(
                lAantalLaadProcessen == brmo.getCountLaadProcessen(null, null, null, null),
                "Er zijn anders dan verwacht aantal laadprocessen"
        );

        if (!this.isMsSQL) {
            // mssql gebruikt identity kolom
            try {
                ITable lp = staging.createQueryTable("laadproces", "select max(id) as maxid from laadproces");
                Number maxLP = (Number) lp.getValue(0, "maxid");
                SequenceUtil.updateSequence("laadproces_id_seq", maxLP.longValue() + 9999999, dsStaging);
            } catch (SQLException s) {
                LOG.error("Bijwerken laadproces sequence is mislukt", s);
            }
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (brmo != null) {
            brmo.closeBrmoFramework();
            brmo = null;
        }
        if (staging != null) {
            CleanUtil.cleanSTAGING(staging, true);
            staging.close();
            dsStaging.close();
        }
        try {
            sequential.unlock();
        } catch (IllegalMonitorStateException e) {
            // in geval van niet waar gemaakte assumptions
            LOG.debug("unlock van thread is mislukt, mogelijk niet ge-lock-ed of test overgeslagen.");
        }
    }

    @DisplayName("getOntbrekendeAfgiftenummers default")
    @ParameterizedTest(name = "{index}: testbestand ''{0}''")
    @MethodSource("argumentsProvider")
    public void testGetOntbrekendeAfgiftenummersDefault(String sBestandsNaam, long lAantalLaadProcessen,
                                                        int iAantalOntbrekendeNummerRanges, String sContractNummer,
                                                        String[] sContractNummers, int iAantalOntbrekendRecords)
            throws Exception {

        loadTestData(sBestandsNaam, lAantalLaadProcessen);

        List<Map<String, Object>> afgiftenummers = scanner.getOntbrekendeAfgiftenummers(sContractNummer, "");
        assertEquals(iAantalOntbrekendeNummerRanges, afgiftenummers.size(),
                "aantal ontbrekende afgifte nummer ranges klopt niet");
        assertEquals(iAantalOntbrekendeNummerRanges != 0, scanner.getOntbrekendeNummersGevonden(),
                "gevonden ontbrekende records vlag klopt niet");

        if (iAantalOntbrekendeNummerRanges > 0) {
            Iterator<Map<String, Object>> records = afgiftenummers.iterator();
            while (records.hasNext()) {
                LOG.debug(records.next());
            }
        }
    }

    @DisplayName("getOntbrekendeKlantAfgiftenummers")
    @ParameterizedTest(name = "{index}: testbestand ''{0}''")
    @MethodSource("argumentsProvider")
    public void testGetOntbrekendeKlantAfgiftenummers(String sBestandsNaam, long lAantalLaadProcessen,
                                                      int iAantalOntbrekendeNummerRanges, String sContractNummer,
                                                      String[] sContractNummers, int iAantalOntbrekendRecords)
            throws Exception {

        loadTestData(sBestandsNaam, lAantalLaadProcessen);
        List<Map<String, Object>> afgiftenummers = scanner.getOntbrekendeAfgiftenummers(sContractNummer,
                "klantafgiftenummer");
        assertEquals(iAantalOntbrekendeNummerRanges, afgiftenummers.size(),
                "aantal ontbrekende afgifte nummer ranges klopt niet");
        assertEquals(iAantalOntbrekendeNummerRanges != 0, scanner.getOntbrekendeNummersGevonden(),
                "gevonden ontbrekende records vlag klopt niet");
    }

    @DisplayName("getOntbrekendeContractAfgiftenummers")
    @ParameterizedTest(name = "{index}: testbestand ''{0}''")
    @MethodSource("argumentsProvider")
    public void testGetOntbrekendeContractAfgiftenummers(String sBestandsNaam, long lAantalLaadProcessen,
                                                         int iAantalOntbrekendeNummerRanges, String sContractNummer,
                                                         String[] sContractNummers, int iAantalOntbrekendRecords)
            throws Exception {

        loadTestData(sBestandsNaam, lAantalLaadProcessen);
        List<Map<String, Object>> afgiftenummers = scanner.getOntbrekendeAfgiftenummers(sContractNummer,
                "contractafgiftenummer");
        assertEquals(iAantalOntbrekendeNummerRanges, afgiftenummers.size(),
                "aantal ontbrekende afgifte nummer ranges klopt niet");
        assertEquals(iAantalOntbrekendeNummerRanges != 0, scanner.getOntbrekendeNummersGevonden(),
                "gevonden ontbrekende records vlag klopt niet");
    }

    @DisplayName("insertOntbrekendeContractAfgiftenummers")
    @ParameterizedTest(name = "{index}: testbestand ''{0}'' ontbrekende records {5}")
    @MethodSource("argumentsProvider")
    public void testInsertOntbrekendeContractAfgiftenummers(String sBestandsNaam, long lAantalLaadProcessen,
                                                            int iAantalOntbrekendeNummerRanges, String sContractNummer,
                                                            String[] sContractNummers, int iAantalOntbrekendRecords)
            throws Exception {

        loadTestData(sBestandsNaam, lAantalLaadProcessen);
        List<Map<String, Object>> afgiftenummers = scanner.getOntbrekendeAfgiftenummers(sContractNummer,
                "contractafgiftenummer");

        Iterator<Map<String, Object>> records = afgiftenummers.iterator();
        Map<String, Object> rec;
        long inserted = 0;
        while (records.hasNext()) {
            rec = records.next();

            inserted += scanner.insertOntbrekendeAfgiftenummers(
                    ((Number) rec.get("eerst_ontbrekend")).longValue(),
                    ((Number) rec.get("laatst_ontbrekend")).longValue(),
                    sContractNummer, "contractafgiftenummer"
            );
        }

        assertEquals(iAantalOntbrekendRecords, inserted, "aantal toegevoegde laadprocessen klopt niet");
    }

    @DisplayName("insertOntbrekendeKlantAfgiftenummers")
    @ParameterizedTest(name = "{index}: testbestand ''{0}'' ontbrekende records {5}")
    @MethodSource("argumentsProvider")
    public void testInsertOntbrekendeKlantAfgiftenummers(String sBestandsNaam, long lAantalLaadProcessen,
                                                         int iAantalOntbrekendeNummerRanges, String sContractNummer,
                                                         String[] sContractNummers, int iAantalOntbrekendRecords)
            throws Exception {

        loadTestData(sBestandsNaam, lAantalLaadProcessen);
        List<Map<String, Object>> afgiftenummers = scanner.getOntbrekendeAfgiftenummers(sContractNummer,
                "klantafgiftenummer");

        Iterator<Map<String, Object>> records = afgiftenummers.iterator();
        Map<String, Object> rec;
        long inserted = 0;
        while (records.hasNext()) {
            rec = records.next();
            inserted += scanner.insertOntbrekendeAfgiftenummers(
                    ((Number) rec.get("eerst_ontbrekend")).longValue(),
                    ((Number) rec.get("laatst_ontbrekend")).longValue(),
                    sContractNummer, "klantafgiftenummer"
            );
        }
        assertEquals(iAantalOntbrekendRecords, inserted, "aantal toegevoegde laadprocessen klopt niet");
    }

    @Test
    public void testGetOntbrekendeContractAfgiftenummersZonderContractnummer() {
        assertThrows(BrmoException.class, () -> {
            assertTrue(true, "should fail");
            List<Map<String, Object>> afgiftenummers = scanner.getOntbrekendeAfgiftenummers(
                    null,
                    "contractafgiftenummer"
            );
            fail("should not pass");
        });
    }

    @DisplayName("Contractnummers test")
    @ParameterizedTest(name = "{index}: testbestand ''{0}'' contractnummers {4}")
    @MethodSource("argumentsProvider")
    public void testContractnummers(String sBestandsNaam, long lAantalLaadProcessen, int iAantalOntbrekendeNummerRanges,
                                    String sContractNummer, String[] sContractNummers, int iAantalOntbrekendRecords)
            throws Exception {

        loadTestData(sBestandsNaam, lAantalLaadProcessen);
        List<String> nummers = AfgifteNummerScanner.contractnummers();
        Assertions.assertFalse(nummers.isEmpty());
        Assertions.assertEquals(sContractNummers.length, nummers.size(), "aantal contractnummers klopt niet");
        Assertions.assertEquals(Arrays.asList(sContractNummers), nummers);
    }
}
