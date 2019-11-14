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

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AfgifteNummerScannerProces;
import nl.b3p.brmo.service.testutil.TestUtil;
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
import org.dbunit.ext.mssql.InsertIdentityOperation;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * {@code mvn -Dit.test=AfgifteNummerScannerIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl brmo-service > target/pg.log}
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
public class AfgifteNummerScannerIntegrationTest extends TestUtil {

    private static final Log LOG = LogFactory.getLog(AfgifteNummerScannerIntegrationTest.class);

    private final Lock sequential = new ReentrantLock();
    private final String sBestandsNaam;
    private final String sContractNummer;
    private final String[] sContractNummers;
    private final long lAantalLaadProcessen;
    private final int iAantalOntbrekendRecords;
    private BrmoFramework brmo;
    private AfgifteNummerScanner scanner;
    private IDatabaseConnection staging;

    /**
     * @return een test data {@code Object[]} met daarin
     * {@code {"sBestandsNaam", lAantalLaadProcessen, iAantalOntbrekendRecords, sContractNummer, sContractNummers aanwezig}}
     */
    @Parameterized.Parameters(name = "{index}: bestand: {0}, aantal laadprocessen {1}, aantal ontbrekende nummers {2} voor contractnummer {3}")
    public static Collection testdata() {
        return Arrays.asList(new Object[][]{
                // {"sBestandsNaam", lAantalLaadProcessen, iAantalOntbrekendRecords, sContractNummer, sContractNummers aanwezig},
                {"/afgiftescanner/geenontbrekendenummers.xml", 49, 0, "6666666", new String[]{"6666666"}},
                {"/afgiftescanner/geenontbrekendenummers2.xml", 11, 0, "6666666", new String[]{"1111", "6666666", "999999"}},
                {"/afgiftescanner/metontbrekendenummers.xml", 2317, 2, "999999", new String[]{"999999"}},
                {"/afgiftescanner/combi.xml", 2317 + 49, 2, "999999", new String[]{"6666666", "999999"}},
                {"/afgiftescanner/combi.xml", 2317 + 49, 0, "6666666", new String[]{"6666666", "999999"}},
                {"/afgiftescanner/combi2.xml", 6, 0, "6666666", new String[]{"1111", "6666666", "999999"}}
        });
    }

    public AfgifteNummerScannerIntegrationTest(String sBestandsNaam, long lAantalLaadProcessen, int iAantalOntbrekendRecords, String sContractNummer, String[] sContractNummers) {
        this.sBestandsNaam = sBestandsNaam;
        this.lAantalLaadProcessen = lAantalLaadProcessen;
        this.iAantalOntbrekendRecords = iAantalOntbrekendRecords;
        this.sContractNummer = sContractNummer;
        this.sContractNummers = sContractNummers;
    }

    @Before
    @Override
    public void setUp() throws Exception {
        assumeTrue("Het bestand met staging testdata zou moeten bestaan.", AfgifteNummerScannerIntegrationTest.class.getResource(sBestandsNaam) != null);

        staging = new DatabaseDataSourceConnection(dsStaging);

        if (this.isMsSQL) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            dsStaging.getConnection().setAutoCommit(true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), DBPROPS.getProperty("staging.username").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegegeven");
        }

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(AfgifteNummerScannerIntegrationTest.class.getResource(sBestandsNaam).toURI())));

        sequential.lock();
        if (this.isMsSQL) {
            // SET IDENTITY_INSERT op ON
            InsertIdentityOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        } else {
            DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        }

        brmo = new BrmoFramework(dsStaging, null);

        scanner = new AfgifteNummerScanner(new AfgifteNummerScannerProces());
        assumeTrue("Er zijn anders dan verwacht aantal laadprocessen", lAantalLaadProcessen == brmo.getCountLaadProcessen(null, null, null, null));
    }

    @After
    public void tearDown() throws Exception {
        if (brmo != null) {
            brmo.closeBrmoFramework();
        }
        if (staging != null) {
            CleanUtil.cleanSTAGING(staging, true);
            staging.close();
        }
        try {
            sequential.unlock();
        } catch (IllegalMonitorStateException e) {
            // in geval van niet waar gemaakte assumptions
            LOG.debug("unlock van thread is mislukt, mogelijk niet ge-lock-ed of test overgeslagen.");
        }

    }

    @Test
    public void testGetOntbrekendeAfgiftenummersDefault() throws BrmoException, SQLException {
        List<Map<String, Object>> afgiftenummers = scanner.getOntbrekendeAfgiftenummers(sContractNummer, "");
        assertEquals("aantal ontbrekende afgifte nummer ranges klopt niet", iAantalOntbrekendRecords, afgiftenummers.size());
        assertEquals("gevonden ontbrekende records vlag klopt niet", iAantalOntbrekendRecords != 0, scanner.getOntbrekendeNummersGevonden());

        if (iAantalOntbrekendRecords > 0) {
            Iterator<Map<String, Object>> records = afgiftenummers.iterator();
            while (records.hasNext()) {
                LOG.debug(records.next());
            }
        }
    }

    @Test
    public void testGetOntbrekendeKlantAfgiftenummers() throws BrmoException, SQLException {
        List<Map<String, Object>> afgiftenummers = scanner.getOntbrekendeAfgiftenummers(sContractNummer, "klantafgiftenummer");
        assertEquals("aantal ontbrekende afgifte nummer ranges klopt niet", iAantalOntbrekendRecords, afgiftenummers.size());
        assertEquals("gevonden ontbrekende records vlag klopt niet", iAantalOntbrekendRecords != 0, scanner.getOntbrekendeNummersGevonden());
    }

    @Test
    public void testGetOntbrekendeContractAfgiftenummers() throws BrmoException, SQLException {
        List<Map<String, Object>> afgiftenummers = scanner.getOntbrekendeAfgiftenummers(sContractNummer, "contractafgiftenummer");
        assertEquals("aantal ontbrekende afgifte nummer ranges klopt niet", iAantalOntbrekendRecords, afgiftenummers.size());
        assertEquals("gevonden ontbrekende records vlag klopt niet", iAantalOntbrekendRecords != 0, scanner.getOntbrekendeNummersGevonden());
    }

    @Test(expected = BrmoException.class)
    public void testGetOntbrekendeAfgiftenummersZonderContractnummer() throws BrmoException, SQLException {
        assertTrue("should fail", true);
        List<Map<String, Object>> afgiftenummers = scanner.getOntbrekendeAfgiftenummers(null, "");
        fail("should not pass");
    }

    @Test(expected = NullPointerException.class)
    public void testGetOntbrekendeAfgiftenummersZonderNummerType() throws BrmoException, SQLException {
        assertTrue("should fail", true);
        List<Map<String, Object>> afgiftenummers = scanner.getOntbrekendeAfgiftenummers(sContractNummer, null);
        fail("should not pass");
    }

    @Test
    public void testContractnummers() {
        List<String> nummers = AfgifteNummerScanner.contractnummers();
        assertFalse(nummers.isEmpty());
        assertEquals("aantal contractnummers klopt niet", sContractNummers.length, nummers.size());
        assertEquals(Arrays.asList(sContractNummers), nummers);
    }
}
