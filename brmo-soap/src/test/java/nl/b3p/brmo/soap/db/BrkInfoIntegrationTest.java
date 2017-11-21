/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.soap.db;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.brmo.soap.brk.BrkInfoRequest;
import nl.b3p.brmo.soap.brk.BrkInfoResponse;
import nl.b3p.brmo.soap.brk.KadOnrndZkInfoRequest;
import nl.b3p.brmo.soap.brk.KadOnrndZkInfoResponse;
import nl.b3p.brmo.test.util.database.JTDSDriverBasedFailures;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 *
 * Draaien met:
 * {@code mvn -Dit.test=BrkInfoIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * voor bijvoorbeeld PostgreSQL.
 *
 * <strong>Werkt niet met MS SQl server omdat de JTDS een
 * {@code java.lang.AbstractMethodError} opwerpt op aanroep van
 * {@code JtdsConnection.isValid(..)}</strong>
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
@Category(JTDSDriverBasedFailures.class)
public class BrkInfoIntegrationTest extends TestUtil {

    @Parameterized.Parameters(name = "{index}: verwerken bestand: {0}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"sBestandsNaam", aantalPercelen, zoekLocatie, bufferAfstand,eersteKadId,oppPerceel,aardCultuurOnbebouwd, gemCode, perceelNummer},
            {"/rsgb.xml", 1, "POINT(230341 472237)", 100, 66860489870000L, 1050, "Wegen", "MKL00", 4898}
        });
    }

    private IDatabaseConnection rsgb;

    private final Lock sequential = new ReentrantLock(true);

    /*
     * test parameters.
     */
    private final String sBestandsNaam;
    private final int aantalPercelen;
    private final String zoekLocatie;
    private final int bufferAfstand;
    private final long eersteKadId;
    private final double oppPerceel;
    private final String aardCultuurOnbebouwd;
    private final String gemCode;
    private final int perceelNummer;

    public BrkInfoIntegrationTest(String sBestandsNaam, int aantalPercelen, String zoekLocatie, int bufferAfstand, long eersteKadId,
            double oppPerceel, String aardCultuurOnbebouwd, String gemCode, int perceelNummer) {

        this.sBestandsNaam = sBestandsNaam;
        this.aantalPercelen = aantalPercelen;
        this.zoekLocatie = zoekLocatie;
        this.bufferAfstand = bufferAfstand;
        this.eersteKadId = eersteKadId;
        this.oppPerceel = oppPerceel;
        this.aardCultuurOnbebouwd = aardCultuurOnbebouwd;
        this.gemCode = gemCode;
        this.perceelNummer = perceelNummer;

    }

    @Before
    @Override
    public void setUp() throws Exception {
        rsgb = new DatabaseDataSourceConnection(this.dsRsgb);
        IDataSet rsgbDataSet = new XmlDataSet(new FileInputStream(new File(BrkInfoIntegrationTest.class.getResource(sBestandsNaam).toURI())));

        if (this.isMsSQL) {
            // TODO kijken of en hoe dit op mssql werkt, vooralsnog problemen met jDTS driver
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            rsgb = new DatabaseConnection(dsRsgb.getConnection().unwrap(oracle.jdbc.OracleConnection.class), DBPROPS.getProperty("rsgb.username").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

            rsgbDataSet = new XmlDataSet(new FileInputStream(new File(BrkInfoIntegrationTest.class.getResource("/oracle-" + sBestandsNaam.substring(1)).toURI())));
        } else if (this.isPostgis) {
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(rsgb, rsgbDataSet);
    }

    @After
    public void cleanup() throws Exception {
        CleanUtil.cleanRSGB_BRK(rsgb, true);
        rsgb.close();

        sequential.unlock();
    }

    /**
     * testcase voor {@link BrkInfo#getDbType(java.sql.Connection)}.
     *
     * @throws Exception if any
     * @see BrkInfo#getDbType(java.sql.Connection)
     */
    @Test
    public void testGetDbType() throws Exception {
        assertEquals(this.isPostgis, BrkInfo.DB_POSTGRES.equals(BrkInfo.getDbType(this.dsRsgb.getConnection())));
        assertEquals(this.isMsSQL, BrkInfo.DB_MSSQL.equals(BrkInfo.getDbType(this.dsRsgb.getConnection())));
        assertEquals(this.isOracle, BrkInfo.DB_ORACLE.equals(BrkInfo.getDbType(this.dsRsgb.getConnection())));
    }

    /**
     * testcase voor {@link BrkInfo#findKozIDs(java.util.Map)}.
     *
     * @throws Exception if any
     * @see BrkInfo#findKozIDs(java.util.Map)
     */
    @Test
    public void testFindKozIDs() throws Exception {
        BrkInfoRequest r = new BrkInfoRequest();
        r.setGevoeligeInfoOphalen(true);
        r.setSubjectsToevoegen(true);
        r.setAdressenToevoegen(true);
        r.setBufferAfstand(bufferAfstand);
        r.setZoekgebied(zoekLocatie);
        Map<String, Object> searchContext = BrkInfo.createSearchContext(r);

        ArrayList<Long> kozIds = BrkInfo.findKozIDs(searchContext);
        assertFalse(kozIds.isEmpty());
        assertEquals(Long.valueOf(eersteKadId), kozIds.get(0));
    }

    /**
     * testcase voor {@link BrkInfo#findKozIDs(java.util.Map)}.
     *
     * @throws Exception if any
     * @see BrkInfo#findKozIDs(java.util.Map)
     */
    @Test
    public void testFindKozIDsByID() throws Exception {
        BrkInfoRequest r = new BrkInfoRequest();
        r.setGevoeligeInfoOphalen(true);
        r.setSubjectsToevoegen(true);
        r.setAdressenToevoegen(true);
        r.setKadOnrndZk(new KadOnrndZkInfoRequest());
        r.getKadOnrndZk().setIdentificatie(Long.toString(eersteKadId));

        Map<String, Object> searchContext = BrkInfo.createSearchContext(r);

        ArrayList<Long> kozIds = BrkInfo.findKozIDs(searchContext);
        assertFalse(kozIds.isEmpty());
        assertEquals(Long.valueOf(eersteKadId), kozIds.get(0));
    }

    /**
     * testcase voor {@link BrkInfo#findKozIDs(java.util.Map)}.
     *
     * @throws Exception if any
     * @see BrkInfo#findKozIDs(java.util.Map)
     */
    @Test
    public void testFindKozIDsByGemPercNum() throws Exception {
        BrkInfoRequest r = new BrkInfoRequest();
        r.setGevoeligeInfoOphalen(true);
        r.setSubjectsToevoegen(true);
        r.setAdressenToevoegen(true);
        r.setKadOnrndZk(new KadOnrndZkInfoRequest());
        r.getKadOnrndZk().setGemeentecode(gemCode);
        r.getKadOnrndZk().setPerceelnummer(Integer.toString(perceelNummer));

        Map<String, Object> searchContext = BrkInfo.createSearchContext(r);

        ArrayList<Long> kozIds = BrkInfo.findKozIDs(searchContext);
        assertFalse(kozIds.isEmpty());
        assertEquals(Long.valueOf(eersteKadId), kozIds.get(0));
    }

    /**
     * testcase voor {@link BrkInfo#countResults(java.util.Map)}.
     *
     * @throws Exception if any
     * @see BrkInfo#countResults(java.util.Map)
     */
    @Test
    public void testCountResults() throws Exception {
        BrkInfoRequest r = new BrkInfoRequest();
        r.setGevoeligeInfoOphalen(true);
        r.setSubjectsToevoegen(true);
        r.setAdressenToevoegen(true);
        r.setBufferAfstand(bufferAfstand);
        r.setZoekgebied(zoekLocatie);
        Map<String, Object> searchContext = BrkInfo.createSearchContext(r);

        Integer aantal = BrkInfo.countResults(searchContext);
        assertEquals(Integer.valueOf(aantalPercelen), aantal);
    }

    /**
     * testcase voor
     * {@link BrkInfo#createResponse(java.util.List, java.util.Map)}.
     *
     * @throws Exception if any
     * @see BrkInfo#createResponse(java.util.List, java.util.Map)
     */
    @Test
    public void testCreateResponse() throws Exception {
        BrkInfoRequest r = new BrkInfoRequest();
        r.setGevoeligeInfoOphalen(true);
        r.setSubjectsToevoegen(true);
        r.setAdressenToevoegen(true);

        Map<String, Object> searchContext = BrkInfo.createSearchContext(r);

        BrkInfoResponse resp = BrkInfo.createResponse(Arrays.asList(new Long[]{eersteKadId}), searchContext);
        List<KadOnrndZkInfoResponse> kadrnrdzkn = resp.getKadOnrndZk();
        assertFalse(kadrnrdzkn.isEmpty());

        KadOnrndZkInfoResponse koz = kadrnrdzkn.get(0);
        assertEquals(oppPerceel, koz.getGroottePerceel(), 5);
        assertEquals(aardCultuurOnbebouwd, koz.getAardCultuurOnbebouwd());
    }
}
