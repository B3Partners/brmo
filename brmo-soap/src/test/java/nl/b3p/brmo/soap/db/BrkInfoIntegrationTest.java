/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.soap.db;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.soap.brk.BrkInfoRequest;
import nl.b3p.brmo.soap.brk.BrkInfoResponse;
import nl.b3p.brmo.soap.brk.KadOnrndZkInfoRequest;
import nl.b3p.brmo.soap.brk.KadOnrndZkInfoResponse;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Draaien met: {@code mvn -Dit.test=BrkInfoIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql
 * > target/postgresql.log} voor bijvoorbeeld PostgreSQL.
 *
 * @author mprins
 */
public class BrkInfoIntegrationTest extends TestUtil {

  private final Lock sequential = new ReentrantLock(true);
  /*
   * test parameters.
   */
  private final String sBestandsNaam = "/rsgb.xml";
  private final int aantalPercelen = 1;
  private final String zoekLocatie = "POINT(230341 472237)";
  private final int bufferAfstand = 100;
  private final long eersteKadId = 66860489870000L;
  private final double oppPerceel = 1050;
  private final String aardCultuurOnbebouwd = "Wegen";
  private final String gemCode = "MKL00";
  private final int perceelNummer = 4898;
  private IDatabaseConnection rsgb;

  @BeforeEach
  @Override
  public void setUp() throws Exception {
    rsgb = new DatabaseDataSourceConnection(this.dsRsgb, DBPROPS.getProperty("rsgb.schema"));
    IDataSet rsgbDataSet =
        new XmlDataSet(
            new FileInputStream(
                new File(BrkInfoIntegrationTest.class.getResource(sBestandsNaam).toURI())));

    if (this.isOracle) {
      rsgb =
          new DatabaseConnection(
              dsRsgb.getConnection().unwrap(oracle.jdbc.OracleConnection.class),
              DBPROPS.getProperty("rsgb.schema").toUpperCase());
      rsgb.getConfig()
          .setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
      rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

      rsgbDataSet =
          new XmlDataSet(
              new FileInputStream(
                  new File(
                      BrkInfoIntegrationTest.class
                          .getResource("/oracle-" + sBestandsNaam.substring(1))
                          .toURI())));
    } else if (this.isPostgis) {
      rsgb.getConfig()
          .setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
    }

    sequential.lock();

    DatabaseOperation.CLEAN_INSERT.execute(rsgb, rsgbDataSet);
  }

  @AfterEach
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
    Assertions.assertEquals(
        this.isPostgis, BrkInfo.DB_POSTGRES.equals(BrkInfo.getDbType(this.dsRsgb.getConnection())));
    Assertions.assertEquals(
        this.isOracle, BrkInfo.DB_ORACLE.equals(BrkInfo.getDbType(this.dsRsgb.getConnection())));
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
    Assertions.assertFalse(kozIds.isEmpty());
    Assertions.assertEquals(Long.valueOf(eersteKadId), kozIds.get(0));
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
    Assertions.assertFalse(kozIds.isEmpty());
    Assertions.assertEquals(Long.valueOf(eersteKadId), kozIds.get(0));
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
    Assertions.assertFalse(kozIds.isEmpty());
    Assertions.assertEquals(Long.valueOf(eersteKadId), kozIds.get(0));
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
    Assertions.assertEquals(Integer.valueOf(aantalPercelen), aantal);
  }

  /**
   * testcase voor {@link BrkInfo#createResponse(java.util.List, java.util.Map)}.
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

    BrkInfoResponse resp = BrkInfo.createResponse(Arrays.asList(eersteKadId), searchContext);
    List<KadOnrndZkInfoResponse> kadrnrdzkn = resp.getKadOnrndZk();
    Assertions.assertFalse(kadrnrdzkn.isEmpty());

    KadOnrndZkInfoResponse koz = kadrnrdzkn.get(0);
    Assertions.assertEquals(oppPerceel, koz.getGroottePerceel(), 5);
    Assertions.assertEquals(aardCultuurOnbebouwd, koz.getAardCultuurOnbebouwd());
  }
}
