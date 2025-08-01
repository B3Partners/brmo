package nl.b3p.brmo.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.checks.AfgifteChecker;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.updates.UpdateProcess;
import nl.b3p.brmo.loader.util.BrmoDuplicaatLaadprocesException;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import nl.b3p.jdbc.util.converter.GeometryJdbcConverter;
import nl.b3p.jdbc.util.converter.GeometryJdbcConverterFactory;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BRMO framework.
 *
 * @author Matthijs Laan
 */
public class BrmoFramework {

  private static final Log log = LogFactory.getLog(BrmoFramework.class);

  public static final String BR_BRK2 = "brk2";
  public static final String BR_NHR = "nhr";
  public static final String BR_BRP = "brp";
  public static final String BR_GBAV = "gbav";
  public static final String BR_WOZ = "woz";

  public static final String XSL_BRK2 = "/xsl/brk2-snapshot-to-rsgb-xml.xsl";
  public static final String XSL_NHR = "/xsl/nhr-to-rsgb-xml-3.0.xsl";
  public static final String XSL_BRP = "/xsl/brp-to-rsgb-xml.xsl";
  public static final String XSL_GBAV = "/xsl/gbav-to-rsgb-xml.xsl";
  public static final String XSL_WOZ = "/xsl/woz-to-rsgb.xsl";

  public static final String LAADPROCES_TABEL = "laadproces";
  public static final String BERICHT_TABLE = "bericht";
  public static final String JOB_TABLE = "job";
  public static final String BRMO_METADATA_TABEL = "brmo_metadata";

  private StagingProxy stagingProxy = null;
  private final DataSource dataSourceRsgb;
  private final DataSource dataSourceRsgbBrk;
  private DataSource dataSourceRsgbBgt = null;
  private final DataSource dataSourceStaging;
  private boolean enablePipeline = false;
  private Integer pipelineCapacity;
  private boolean renewConnectionAfterCommit = false;
  private boolean orderBerichten = true;
  private String errorState = null;

  public BrmoFramework(
      DataSource dataSourceStaging, DataSource dataSourceRsgb, DataSource dataSourceRsgbBrk)
      throws BrmoException {
    if (dataSourceStaging != null) {
      try {
        stagingProxy = new StagingProxy(dataSourceStaging);
      } catch (SQLException ex) {
        throw new BrmoException(ex);
      }
    }
    this.dataSourceStaging = dataSourceStaging;
    this.dataSourceRsgb = dataSourceRsgb;
    this.dataSourceRsgbBrk = dataSourceRsgbBrk;
  }

  public BrmoFramework(
      DataSource dataSourceStaging,
      DataSource dataSourceRsgb,
      DataSource dataSourceRsgbBgt,
      DataSource dataSourceRsgbBrk)
      throws BrmoException {
    if (dataSourceStaging != null) {
      try {
        stagingProxy = new StagingProxy(dataSourceStaging);
      } catch (SQLException ex) {
        throw new BrmoException(ex);
      }
    }
    this.dataSourceRsgb = dataSourceRsgb;
    this.dataSourceRsgbBgt = dataSourceRsgbBgt;
    this.dataSourceStaging = dataSourceStaging;
    this.dataSourceRsgbBrk = dataSourceRsgbBrk;
  }

  public void setDataSourceRsgbBgt(DataSource dataSourceRsgbBgt) {
    this.dataSourceRsgbBgt = dataSourceRsgbBgt;
  }

  public String getStagingVersion() {
    try {
      return getVersion(dataSourceStaging);
    } catch (SQLException ex) {
      log.error("Versienummer kon niet uit de STAGING database worden gelezen.", ex);
      return "";
    }
  }

  public String getRsgbVersion() {
    try {
      return getVersion(dataSourceRsgb);
    } catch (SQLException ex) {
      log.error("Versienummer kon niet uit de RSGB database worden gelezen.", ex);
      return "";
    }
  }

  public String getRsgbBgtVersion() {
    try {
      return getVersion(dataSourceRsgbBgt);
    } catch (SQLException ex) {
      log.error("Versienummer kon niet uit de RSGBBGT database worden gelezen.", ex);
      return "";
    }
  }

  public String getRsgbBrkVersion() {
    try {
      return getVersion(dataSourceRsgbBrk);
    } catch (SQLException ex) {
      log.error("Versienummer kon niet uit de RSGB BRK database worden gelezen.", ex);
      return "";
    }
  }

  private String getVersion(DataSource dataSource) throws SQLException {
    String sql =
        "SELECT waarde FROM " + BrmoFramework.BRMO_METADATA_TABEL + " WHERE naam = 'brmoversie'";
    final Connection c = dataSource.getConnection();
    GeometryJdbcConverter geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
    Object o = new QueryRunner(geomToJdbc.isPmdKnownBroken()).query(c, sql, new ScalarHandler<>());
    DbUtils.closeQuietly(c);
    if (o instanceof Clob) {
      return ((Clob) o).getSubString(1, (int) ((Clob) o).length());
    }
    return o.toString();
  }

  public void setEnablePipeline(boolean enablePipeline) {
    this.enablePipeline = enablePipeline;
  }

  public void setRenewConnectionAfterCommit(boolean renewConnectionAfterCommit) {
    this.renewConnectionAfterCommit = renewConnectionAfterCommit;
  }

  public void setTransformPipelineCapacity(int pipelineCapacity) {
    this.pipelineCapacity = pipelineCapacity;
  }

  public void setBatchCapacity(int batchCapacity) {
    if (stagingProxy != null) {
      stagingProxy.setBatchCapacity(batchCapacity);
    }
  }

  public void setLimitStandBerichtenToTransform(Integer limitStandBerichtenToTransform) {
    if (stagingProxy != null) {
      stagingProxy.setLimitStandBerichtenToTransform(limitStandBerichtenToTransform);
    }
  }

  /**
   * stel bericht sortering van berichten in.
   *
   * @param orderBerichten {@code false} voor stand, {@code true} voor mutaties
   */
  public void setOrderBerichten(boolean orderBerichten) {
    this.orderBerichten = orderBerichten;
  }

  public boolean isOrderBerichten() {
    return this.orderBerichten;
  }

  public void setErrorState(String errorState) {
    this.errorState = errorState;
  }

  public void closeBrmoFramework() {
    if (stagingProxy != null) {
      stagingProxy.closeStagingProxy();
    }
    // rsgbProxy is thread and will be closed in thread
  }

  public Thread toRsgb() throws BrmoException {
    return toRsgb((ProgressUpdateListener) null);
  }

  public Thread toRsgb(ProgressUpdateListener listener) throws BrmoException {
    return toRsgb(Bericht.STATUS.STAGING_OK, listener);
  }

  public Thread toRsgb(Bericht.STATUS status, ProgressUpdateListener listener)
      throws BrmoException {
    RsgbProxy rsgbProxy =
        new RsgbProxy(dataSourceRsgb, dataSourceRsgbBrk, stagingProxy, status, listener);
    rsgbProxy.setEnablePipeline(enablePipeline);
    if (pipelineCapacity != null) {
      rsgbProxy.setPipelineCapacity(pipelineCapacity);
    }
    rsgbProxy.setRenewConnectionAfterCommit(renewConnectionAfterCommit);
    rsgbProxy.setOrderBerichten(orderBerichten);
    rsgbProxy.setErrorState(errorState);
    Thread t = new Thread(rsgbProxy);
    t.start();
    return t;
  }

  /**
   * @param mode geeft aan wat ids zijn (laadprocessen of berichten)
   * @param ids array van ids
   * @param listener voortgangs listener
   * @return running transformatie thread
   * @throws BrmoException if any
   */
  public Thread toRsgb(
      RsgbProxy.BerichtSelectMode mode, long[] ids, ProgressUpdateListener listener)
      throws BrmoException {
    String soort = "";
    if (mode == RsgbProxy.BerichtSelectMode.BY_LAADPROCES) {
      try {
        soort = stagingProxy.getLaadProcesById(ids[0]).getSoort();
      } catch (SQLException ex) {
        throw new BrmoException(ex);
      }
    }

    Runnable worker;
    worker = new RsgbProxy(dataSourceRsgb, dataSourceRsgbBrk, stagingProxy, mode, ids, listener);
    ((RsgbProxy) worker).setEnablePipeline(enablePipeline);
    if (pipelineCapacity != null) {
      ((RsgbProxy) worker).setPipelineCapacity(pipelineCapacity);
    }
    ((RsgbProxy) worker).setRenewConnectionAfterCommit(renewConnectionAfterCommit);
    ((RsgbProxy) worker).setOrderBerichten(orderBerichten);
    ((RsgbProxy) worker).setErrorState(errorState);
    Thread t = new Thread(worker);
    t.start();
    return t;
  }

  public Thread toRsgb(UpdateProcess updateProcess, ProgressUpdateListener listener)
      throws BrmoException {
    RsgbProxy rsgbProxy =
        new RsgbProxy(dataSourceRsgb, dataSourceRsgbBrk, stagingProxy, updateProcess, listener);
    rsgbProxy.setEnablePipeline(enablePipeline);
    if (pipelineCapacity != null) {
      rsgbProxy.setPipelineCapacity(pipelineCapacity);
    }
    rsgbProxy.setRenewConnectionAfterCommit(renewConnectionAfterCommit);
    rsgbProxy.setOrderBerichten(orderBerichten);
    rsgbProxy.setErrorState(errorState);
    Thread t = new Thread(rsgbProxy);
    t.start();
    return t;
  }

  public void delete(Long id) throws BrmoException {
    if (id != null) {
      try {
        stagingProxy.deleteByLaadProcesId(id);
      } catch (SQLException ex) {
        throw new BrmoException(ex);
      }
    }
  }

  public List<LaadProces> listLaadProcessen() throws BrmoException {
    try {
      return stagingProxy.getLaadProcessen();
    } catch (SQLException ex) {
      throw new BrmoException(ex);
    }
  }

  public List<Bericht> listBerichten() throws BrmoException {
    try {
      return stagingProxy.getBerichten();
    } catch (SQLException ex) {
      throw new BrmoException(ex);
    }
  }

  /**
   * @param type basis registratie type
   * @param fileName bestand
   * @throws BrmoException als er een fout optreed tijdens verwerking
   * @deprecated gebruik van de variant met automatischProces ID heeft de voorkeur
   * @see #loadFromFile(java.lang.String, java.lang.String, java.lang.Long)
   */
  @Deprecated
  public void loadFromFile(String type, String fileName) throws BrmoException {
    loadFromFile(type, fileName, null);
  }

  /**
   * @see #loadFromFile(java.lang.String, java.lang.String,
   *     nl.b3p.brmo.loader.ProgressUpdateListener, Long)
   * @param type basis registratie type
   * @param fileName bestand
   * @param automatischProces id van automatisch proces dat deze functie aanroept
   * @throws BrmoException als er een fout optreed tijdens verwerking
   */
  public void loadFromFile(String type, String fileName, Long automatischProces)
      throws BrmoException {
    try {
      loadFromFile(type, fileName, null, automatischProces);
    } catch (Exception e) {
      if (e instanceof BrmoException) {
        throw (BrmoException) e;
      } else {
        throw new BrmoException("Fout bij loaden basisregistratie gegevens", e);
      }
    }
  }

  /**
   * laden van BR data uit een bestand. <br>
   * NB na gebruik zelf de database verbinding sluiten / opruimen met {@link #closeBrmoFramework()}.
   *
   * @param type basis registratie type
   * @param fileName bestand
   * @param listener voortgangsmonitor
   * @param automatischProces id van automatisch proces dat deze functie aanroept
   * @throws BrmoException als er een fout optreed tijdens verwerking
   */
  public void loadFromFile(
      String type, String fileName, final ProgressUpdateListener listener, Long automatischProces)
      throws BrmoException {
    try {
      if (fileName.toLowerCase().endsWith(".zip")) {
        log.info("Openen ZIP bestand " + fileName);
        ZipInputStream zip = null;
        try {
          File f = new File(fileName);
          if (listener != null) {
            listener.total(f.length());
          }
          CountingInputStream zipCis =
              new CountingInputStream(new FileInputStream(f)) {
                @Override
                protected synchronized void afterRead(int n) throws IOException {
                  super.afterRead(n);
                  if (listener != null) {
                    listener.progress(getByteCount());
                  }
                }
              };
          zip = new ZipInputStream(zipCis);
          ZipEntry entry = zip.getNextEntry();
          while (entry != null) {
            if (!entry.getName().toLowerCase().endsWith(".xml")) {
              log.warn("Overslaan zip entry geen XML: " + entry.getName());
            } else {
              log.info("Lezen XML bestand uit zip: " + entry.getName());
              stagingProxy.loadBr(
                  CloseShieldInputStream.wrap(zip),
                  type,
                  fileName + "/" + entry.getName(),
                  null,
                  null,
                  automatischProces);
            }
            entry = zip.getNextEntry();
          }
        } finally {
          if (zip != null) {
            zip.close();
          }
        }
        log.info("Klaar met ZIP bestand " + fileName);
      } else {
        File f = new File(fileName);
        if (listener != null) {
          listener.total(f.length());
        }

        try (FileInputStream fis = new FileInputStream(fileName)) {
          stagingProxy.loadBr(fis, type, fileName, null, listener, automatischProces);
        }
      }
    } catch (Exception e) {
      if (e instanceof BrmoException) {
        throw (BrmoException) e;
      } else {
        throw new BrmoException("Fout bij loaden basisregistratie gegevens", e);
      }
    }
  }

  /**
   * laden van BR data uit een stream.
   *
   * @param type type registratie, bijv. {@value BrmoFramework#BR_BRK2}
   * @param stream datastream
   * @param fileName te gebruiken bestandsnaam om laadproces te identificeren
   * @throws BrmoException als er een algemene fout optreed
   * @throws BrmoDuplicaatLaadprocesException als het "bestand" {@code fileName} al geladen is
   * @throws BrmoLeegBestandException als het "bestand" {@code fileName} leeg is
   * @deprecated probeer de variant met automatischProces argument te gebruiken
   * @see #loadFromStream(String, InputStream, String, Long)
   */
  @Deprecated
  public void loadFromStream(String type, InputStream stream, String fileName)
      throws BrmoException, BrmoDuplicaatLaadprocesException, BrmoLeegBestandException {
    this.loadFromStream(type, stream, fileName, (Long) null);
  }

  /**
   * laden van BR data uit een stream.
   *
   * @param type type registratie, bijv. {@value BrmoFramework#BR_BRK2}
   * @param stream datastream
   * @param fileName te gebruiken bestandsnaam om laadproces te identificeren
   * @param automatischProces id van automatisch proces dat deze functie aanroept
   * @throws BrmoException als er een algemene fout optreed
   * @throws BrmoDuplicaatLaadprocesException als het "bestand" {@code fileName} al geladen is
   * @throws BrmoLeegBestandException als het "bestand" {@code fileName} leeg is
   */
  public void loadFromStream(
      String type, InputStream stream, String fileName, Long automatischProces)
      throws BrmoException, BrmoDuplicaatLaadprocesException, BrmoLeegBestandException {
    try {
      stagingProxy.loadBr(stream, type, fileName, null, null, automatischProces);
    } catch (Exception e) {
      if (e instanceof BrmoDuplicaatLaadprocesException) {
        throw (BrmoDuplicaatLaadprocesException) e;
      }
      if (e instanceof BrmoLeegBestandException) {
        throw (BrmoLeegBestandException) e;
      }
      throw new BrmoException("Fout bij laden " + type + " berichten uit bestand " + fileName, e);
    }
  }

  /**
   * laden van BR data uit een stream.
   *
   * @param type type registratie, bijv. {@value BrmoFramework#BR_BRK2}
   * @param stream datastream
   * @param fileName te gebruiken bestandsnaam om laadproces te identificeren
   * @param d bestandsdatum
   * @throws BrmoException als er een algemene fout optreed
   * @throws BrmoDuplicaatLaadprocesException als het "bestand" {@code fileName} al geladen is
   * @throws BrmoLeegBestandException als het "bestand" {@code fileName} leeg is
   * @deprecated Gebruik {@link #loadFromStream(String, InputStream, String, Date, Long)}
   */
  @Deprecated
  public void loadFromStream(String type, InputStream stream, String fileName, Date d)
      throws BrmoException, BrmoDuplicaatLaadprocesException, BrmoLeegBestandException {
    loadFromStream(type, stream, fileName, d, null);
  }

  /**
   * laden van BR data uit een stream.
   *
   * @param type type registratie, bijv. {@value BrmoFramework#BR_BRK2}
   * @param stream datastream
   * @param fileName te gebruiken bestandsnaam om laadproces te identificeren
   * @param d bestandsdatum
   * @param automatischProces id van automatisch proces dat deze functie aanroept
   * @throws BrmoException als er een algemene fout optreed
   * @throws BrmoDuplicaatLaadprocesException als het "bestand" {@code fileName} al geladen is
   * @throws BrmoLeegBestandException als het "bestand" {@code fileName} leeg is
   */
  public void loadFromStream(
      String type, InputStream stream, String fileName, Date d, Long automatischProces)
      throws BrmoException, BrmoDuplicaatLaadprocesException, BrmoLeegBestandException {
    try {
      stagingProxy.loadBr(stream, type, fileName, d, null, automatischProces);
    } catch (Exception e) {
      if (e instanceof BrmoDuplicaatLaadprocesException) {
        throw (BrmoDuplicaatLaadprocesException) e;
      }
      if (e instanceof BrmoLeegBestandException) {
        throw (BrmoLeegBestandException) e;
      }
      throw new BrmoException("Fout bij laden " + type + " berichten uit bestand " + fileName, e);
    }
  }

  /**
   * laden van BR data uit een stream.
   *
   * @param type type registratie, bijv. {@value BrmoFramework#BR_BRK2}
   * @param stream datastream
   * @param fileName te gebruiken bestandsnaam om laadproces te identificeren
   * @param listener mag {@code null} zijn
   * @param automatischProces id van automatisch proces dat deze functie aanroept
   * @throws BrmoException als er een algemene fout optreed
   * @throws BrmoDuplicaatLaadprocesException als het "bestand" {@code fileName} al geladen is
   * @throws BrmoLeegBestandException als het "bestand" {@code fileName} leeg is
   */
  public void loadFromStream(
      String type,
      InputStream stream,
      String fileName,
      ProgressUpdateListener listener,
      Long automatischProces)
      throws BrmoException, BrmoDuplicaatLaadprocesException, BrmoLeegBestandException {
    try {
      stagingProxy.loadBr(stream, type, fileName, null, listener, automatischProces);
    } catch (Exception e) {
      if (e instanceof BrmoDuplicaatLaadprocesException) {
        throw (BrmoDuplicaatLaadprocesException) e;
      }
      if (e instanceof BrmoLeegBestandException) {
        throw (BrmoLeegBestandException) e;
      }
      throw new BrmoException("Fout bij laden " + type + " berichten uit bestand " + fileName, e);
    }
  }

  public void emptyStagingDb() throws BrmoException {
    try {
      stagingProxy.emptyStagingDb();
    } catch (SQLException ex) {
      throw new BrmoException(ex);
    }
  }

  public Long getLaadProcesIdByFileName(String name) throws BrmoException {
    Long id = null;
    LaadProces lp;
    try {
      lp = stagingProxy.getLaadProcesByFileName(name);
    } catch (SQLException ex) {
      throw new BrmoException(ex);
    }

    if (lp != null) {
      id = lp.getId();
    }

    return id;
  }

  public Bericht getBerichtById(long id) throws BrmoException {
    try {
      return stagingProxy.getBerichtById(id);
    } catch (SQLException ex) {
      throw new BrmoException(ex);
    }
  }

  public LaadProces getLaadProcesById(long id) throws BrmoException {
    try {
      return stagingProxy.getLaadProcesById(id);
    } catch (SQLException ex) {
      throw new BrmoException(ex);
    }
  }

  public List<Bericht> getBerichten(
      int page,
      int start,
      int limit,
      String sort,
      String dir,
      String filterSoort,
      String filterStatus)
      throws BrmoException {

    try {
      return stagingProxy.getBerichten(page, start, limit, sort, dir, filterSoort, filterStatus);
    } catch (SQLException ex) {
      throw new BrmoException(ex);
    }
  }

  public List<LaadProces> getLaadprocessen(
      int page,
      int start,
      int limit,
      String sort,
      String dir,
      String filterSoort,
      String filterStatus)
      throws BrmoException {

    try {
      return stagingProxy.getLaadprocessen(
          page, start, limit, sort, dir, filterSoort, filterStatus);
    } catch (SQLException ex) {
      throw new BrmoException(ex);
    }
  }

  public Long[] getLaadProcessenIds(
      String sort, String dir, String filterSoort, String filterStatus) throws BrmoException {
    try {
      return stagingProxy.getLaadProcessenIds(sort, dir, filterSoort, filterStatus);
    } catch (SQLException ex) {
      throw new BrmoException(ex);
    }
  }

  public long getCountBerichten(String filterSoort, String filterStatus) throws BrmoException {
    try {
      return stagingProxy.getCountBerichten(filterSoort, filterStatus);
    } catch (SQLException ex) {
      throw new BrmoException(ex);
    }
  }

  public long getCountLaadProcessen(String filterSoort, String filterStatus) throws BrmoException {
    try {
      return stagingProxy.getCountLaadProces(filterSoort, filterStatus);
    } catch (SQLException ex) {
      throw new BrmoException(ex);
    }
  }

  public long getCountJob() throws BrmoException {
    try {
      return stagingProxy.getCountJob();
    } catch (SQLException ex) {
      throw new BrmoException(ex);
    }
  }

  /**
   * update laadproces (GDS2 afgifte) metadata.
   *
   * @param lpId laadproces id
   * @param klantafgiftenummer klantafgiftenummer
   * @param contractafgiftenummer contractafgiftenummer
   * @param artikelnummer artikelnummer
   * @param contractnummer contractnummer
   * @param afgifteid afgifteid
   * @param afgiftereferentie afgiftereferentie
   * @param bestandsreferentie bestandsreferentie
   * @param beschikbaar_tot beschikbaar_tot
   * @throws BrmoException if any
   */
  public void updateLaadProcesMeta(
      Long lpId,
      BigInteger klantafgiftenummer,
      BigInteger contractafgiftenummer,
      String artikelnummer,
      String contractnummer,
      String afgifteid,
      String afgiftereferentie,
      String bestandsreferentie,
      Date beschikbaar_tot)
      throws BrmoException {
    try {
      stagingProxy.updateLaadProcesMeta(
          lpId,
          klantafgiftenummer,
          contractafgiftenummer,
          artikelnummer,
          contractnummer,
          afgifteid,
          afgiftereferentie,
          bestandsreferentie,
          beschikbaar_tot);
    } catch (SQLException ex) {
      throw new BrmoException(ex);
    }
  }

  public File checkAfgiftelijst(String bestandsnaam, String output) throws IOException {
    File f = new File(bestandsnaam);
    return checkAfgiftelijst(bestandsnaam, new FileInputStream(f), new File(output));
  }

  public File checkAfgiftelijst(String bestandsnaam, InputStream is, File output)
      throws IOException {
    AfgifteChecker checker = new AfgifteChecker();
    checker.init(is, stagingProxy);
    checker.check();
    return checker.getResults(bestandsnaam, output);
  }
}
