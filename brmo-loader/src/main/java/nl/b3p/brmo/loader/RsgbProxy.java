package nl.b3p.brmo.loader;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.updates.UpdateProcess;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.DataComfortXMLReader;
import nl.b3p.brmo.loader.util.RsgbBRPTransformer;
import nl.b3p.brmo.loader.util.RsgbTransformer;
import nl.b3p.brmo.loader.util.RsgbWOZTransformer;
import nl.b3p.brmo.loader.util.TableData;
import nl.b3p.brmo.loader.util.TableRow;
import nl.b3p.jdbc.util.converter.ColumnMetadata;
import nl.b3p.jdbc.util.converter.GeometryJdbcConverter;
import nl.b3p.jdbc.util.converter.GeometryJdbcConverterFactory;
import nl.b3p.jdbc.util.converter.OracleJdbcConverter;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.locationtech.jts.io.ParseException;
import org.w3c.dom.Node;

/**
 * @author Boy de Wit
 */
public class RsgbProxy implements Runnable, BerichtenHandler {

  private static final Log log = LogFactory.getLog(RsgbProxy.class);

  private final boolean alsoExecuteSql = true;

  private final ProgressUpdateListener listener;
  private final BerichtSelectMode mode;
  private final UpdateProcess updateProcess;
  private final Map<String, PreparedStatement> checkRowExistsStatements = new HashMap<>();
  private final Map<String, PreparedStatement> insertSqlPreparedStatements = new HashMap<>();
  private final Map<String, PreparedStatement> updateSqlPreparedStatements = new HashMap<>();
  private final Map<String, PreparedStatement> createDeleteSqlStatements = new HashMap<>();
  private final Map<String, List<String>> primaryKeyCache = new HashMap<>();
  private final DataComfortXMLReader dbXmlReader = new DataComfortXMLReader();
  // Gebruik andere instantie voor lezen oude berichten, gebeurt gelijktijdig
  // in andere thread als lezen van dbxml van berichten om te verwerken
  private final DataComfortXMLReader oldDbXmlReader = new DataComfortXMLReader();
  private int processed;

  /** De status voor BY_STATUS. */
  private Bericht.STATUS status;

  /** De bericht ID's voor BY_IDS. */
  private long[] berichtIds;

  /** Het ID van het laadproces vor BY_LAADPROCES. */
  private long[] laadprocesIds;

  private PreparedStatement insertMetadataStatement;
  private DatabaseMetaData dbMetadata = null;
  private DatabaseMetaData dbMetadataBrk = null;
  /* Map van lowercase tabelnaam naar originele case tabelnaam */
  private final Map<String, String> tables = new HashMap<>();
  private final Map<String, SortedSet<ColumnMetadata>> tableColumns = new HashMap<>();
  private Connection connRsgb = null;
  private Connection connRsgbBrk = null;
  private GeometryJdbcConverter geomToJdbc = null;
  private GeometryJdbcConverter geomToJdbcBrk = null;
  private Savepoint recentSavepoint = null;
  private String herkomstMetadata = null;
  private final DataSource dataSourceRsgb;
  private final DataSource dataSourceRsgbBrk;
  private final StagingProxy stagingProxy;
  private boolean enablePipeline = false;
  private int pipelineCapacity = 25;
  private boolean renewConnectionAfterCommit = false;
  private boolean orderBerichten = true;
  private String errorState = null;
  private final Map<String, RsgbTransformer> rsgbTransformers = new HashMap<>();
  private String simonNamePrefix = "b3p.rsgb.";

  public RsgbProxy(
      DataSource dataSourceRsgb,
      DataSource dataSourceRsgbBrk,
      StagingProxy stagingProxy,
      Bericht.STATUS status,
      ProgressUpdateListener listener) {
    this.stagingProxy = stagingProxy;
    this.dataSourceRsgb = dataSourceRsgb;
    this.dataSourceRsgbBrk = dataSourceRsgbBrk;

    mode = BerichtSelectMode.BY_STATUS;
    this.status = status;
    this.listener = listener;
    this.updateProcess = null;
  }

  /**
   * @param dataSourceRsgb de te gebruiken RSGB database
   * @param stagingProxy de te gebruiken StagingProxy
   * @param mode geeft aan wat ids zijn (laadprocessen of berichten)
   * @param ids record id's (afhankelijk van de {@code BerichtSelectMode})
   * @param listener voortgangs listener
   */
  public RsgbProxy(
      DataSource dataSourceRsgb,
      DataSource dataSourceRsgbBrk,
      StagingProxy stagingProxy,
      BerichtSelectMode mode,
      long[] ids,
      ProgressUpdateListener listener) {
    this.stagingProxy = stagingProxy;
    this.dataSourceRsgb = dataSourceRsgb;
    this.dataSourceRsgbBrk = dataSourceRsgbBrk;

    this.mode = mode;
    if (mode == BerichtSelectMode.BY_LAADPROCES) {
      this.laadprocesIds = ids;
    } else if (mode == BerichtSelectMode.BY_IDS) {
      this.berichtIds = ids;
    }
    this.listener = listener;
    this.updateProcess = null;
  }

  public RsgbProxy(
      DataSource dataSourceRsgb,
      DataSource dataSourceRsgbBrk,
      StagingProxy stagingProxy,
      UpdateProcess updateProcess,
      ProgressUpdateListener listener) {
    this.stagingProxy = stagingProxy;
    this.dataSourceRsgb = dataSourceRsgb;
    this.dataSourceRsgbBrk = dataSourceRsgbBrk;

    this.mode = BerichtSelectMode.FOR_UPDATE;
    this.updateProcess = updateProcess;
    this.listener = listener;
  }

  public void setSimonNamePrefix(String prefix) {
    this.simonNamePrefix = prefix;
  }

  public void setEnablePipeline(boolean enablePipeline) {
    this.enablePipeline = enablePipeline;
  }

  public void setPipelineCapacity(int pipelineCapacity) {
    this.pipelineCapacity = pipelineCapacity;
  }

  public void setRenewConnectionAfterCommit(boolean renewConnectionAfterCommit) {
    this.renewConnectionAfterCommit = renewConnectionAfterCommit;
  }

  public void setOrderBerichten(boolean orderBerichten) {
    this.orderBerichten = orderBerichten;
  }

  public void setErrorState(String errorState) {
    this.errorState = errorState;
  }

  public void init() throws SQLException {
    ResultSet tablesRs = null;
    if (null != this.dataSourceRsgb) {
      connRsgb = dataSourceRsgb.getConnection();
      geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(connRsgb);

      try {
        dbMetadata = connRsgb.getMetaData();
        tablesRs = dbMetadata.getTables(null, geomToJdbc.getSchema(), "%", new String[] {"TABLE"});
        while (tablesRs.next()) {
          tables.put(
              tablesRs.getString("TABLE_NAME").toLowerCase(), tablesRs.getString("TABLE_NAME"));
        }
      } catch (SQLException sqlEx) {
        throw new SQLException("Fout bij ophalen rsgb tabellen: ", sqlEx);
      } finally {
        DbUtils.closeQuietly(tablesRs);
      }
    }

    if (null != dataSourceRsgbBrk) {
      connRsgbBrk = dataSourceRsgbBrk.getConnection();
      geomToJdbcBrk = GeometryJdbcConverterFactory.getGeometryJdbcConverter(connRsgbBrk);

      try {
        dbMetadataBrk = connRsgbBrk.getMetaData();
        tablesRs =
            dbMetadataBrk.getTables(null, geomToJdbcBrk.getSchema(), "%", new String[] {"TABLE"});
        while (tablesRs.next()) {
          tables.put(
              tablesRs.getString("TABLE_NAME").toLowerCase(), tablesRs.getString("TABLE_NAME"));
        }
      } catch (SQLException sqlEx) {
        throw new SQLException("Fout bij ophalen rsgb brk tabellen: ", sqlEx);
      } finally {
        DbUtils.closeQuietly(tablesRs);
      }
    }
  }

  private PreparedStatement checkAndGetPreparedStatement(
      Map<String, PreparedStatement> m, String name) throws SQLException {
    PreparedStatement stm = m.get(name);
    if (stm == null || stm.isClosed()) {
      return null;
    }
    stm.clearParameters();
    return stm;
  }

  public void checkAndAddStatement(
      Map<String, PreparedStatement> m, String tableName, PreparedStatement stmt) {
    m.put(tableName, stmt);
  }

  public void close() {
    for (PreparedStatement stmt : checkRowExistsStatements.values()) {
      DbUtils.closeQuietly(stmt);
    }
    checkRowExistsStatements.clear();
    for (PreparedStatement stmt : insertSqlPreparedStatements.values()) {
      DbUtils.closeQuietly(stmt);
    }
    insertSqlPreparedStatements.clear();
    for (PreparedStatement stmt : updateSqlPreparedStatements.values()) {
      DbUtils.closeQuietly(stmt);
    }
    updateSqlPreparedStatements.clear();
    for (PreparedStatement stmt : createDeleteSqlStatements.values()) {
      DbUtils.closeQuietly(stmt);
    }
    createDeleteSqlStatements.clear();
    if (insertMetadataStatement != null) {
      DbUtils.closeQuietly(insertMetadataStatement);
    }
    insertMetadataStatement = null;
    DbUtils.closeQuietly(this.connRsgb);
    this.connRsgb = null;
    DbUtils.closeQuietly(this.connRsgbBrk);
    this.connRsgbBrk = null;
  }

  @Override
  public void renewConnection() throws SQLException {
    close();
    init();
  }

  @Override
  public void run() {
    try {
      init();
      // Set all berichten to waiting
      long total = setWaitingStatus();
      if (listener != null) {
        listener.total(total);
      }
      // Do the work by querying all berichten, berichten are passed to
      // handle() method
      if (total > 0) {
        stagingProxy.handleBerichtenByJob(
            total, this, enablePipeline, pipelineCapacity, orderBerichten);
      }
    } catch (Exception e) {
      // user is informed via status in database
      log.error("Fout tijdens verwerken berichten", e);

      if (listener != null) {
        listener.exception(e);
      }
    }

    close();
  }

  private long setWaitingStatus() throws SQLException, BrmoException {
    // verwijder al verwerkte berichten uit afgebroken job
    stagingProxy.cleanJob();
    // zijn er nog berichten over uit de vorige job
    long count = stagingProxy.getCountJob();
    if (count > 0 && !mode.equals(BerichtSelectMode.RETRY_WAITING)) {
      throw new BrmoException(
          "Vorige transformatie is afgebroken," + " verwerk eerst die berichten (zie job-tabel)!");
    }

    switch (mode) {
      case BY_STATUS:
        return stagingProxy.setBerichtenJobByStatus(status, orderBerichten);
      case BY_IDS:
        return stagingProxy.setBerichtenJobByIds(berichtIds, orderBerichten);
      case BY_LAADPROCES:
        return stagingProxy.setBerichtenJobByLaadprocessen(laadprocesIds, orderBerichten);
      case FOR_UPDATE:
        return stagingProxy.setBerichtenJobForUpdate(updateProcess.getSoort(), orderBerichten);
      case RETRY_WAITING:
        return count;
      default:
        return 0L;
    }
  }

  @Override
  public void updateProcessingResult(Bericht ber) {
    if (updateProcess != null) {
      return;
    }
    ber.setStatusDatum(new Date());
    try {
      stagingProxy.updateBerichtProcessing(ber);
    } catch (SQLException ex) {
      log.error("Kan status niet updaten", ex);
    }
  }

  @Override
  public List<TableData> transformToTableData(Bericht ber) throws BrmoException {
    if (updateProcess != null) {
      return transformUpdateTableData(ber);
    }

    StringBuilder loadLog = new StringBuilder();
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    try {
      loadLog.append(
          String.format(
              "%s: transformeren %s bericht object ref %s, met id %s, berichtdatum %s\n",
              dateTimeFormat.format(new Date()),
              ber.getSoort(),
              ber.getObjectRef(),
              ber.getId(),
              dateFormat.format(ber.getDatum())));

      RsgbTransformer transformer = getTransformer(ber.getSoort());

      long startTime = System.currentTimeMillis();
      stagingProxy.updateBerichtenDbXml(Collections.singletonList(ber), transformer);

      Split split = SimonManager.getStopwatch("b3p.staging.bericht.dbxml.read").start();
      StringReader nReader = new StringReader(ber.getDbXml());
      List<TableData> data = dbXmlReader.readDataXML(new StreamSource(nReader));
      split.stop();

      loadLog.append(
          String.format(
              "Transformeren naar RSGB database-xml en lezen resultaat: %4.1fs\n\n",
              (System.currentTimeMillis() - startTime) / 1000.0));
      ber.setOpmerking(loadLog.toString());
      return data;
    } catch (Exception e) {
      try {
        updateBerichtException(ber, e);
      } finally {
        updateProcessingResult(ber);
      }
      return null;
    }
  }

  public List<TableData> transformUpdateTableData(Bericht ber) throws BrmoException {

    RsgbTransformer transformer = rsgbTransformers.get(updateProcess.getName());

    if (transformer == null) {
      try {
        transformer = new RsgbTransformer(updateProcess.getXsl());
      } catch (Exception e) {
        throw new BrmoException("Fout bij laden XSL stylesheet: " + updateProcess.getXsl(), e);
      }
      rsgbTransformers.put(updateProcess.getName(), transformer);
    }
    try {
      Node dbxml = transformer.transformToDbXmlNode(ber);
      return dbXmlReader.readDataXML(new DOMSource(dbxml));
    } catch (Exception e) {
      log.error("Fout bij transformeren bericht #" + ber.getId() + " voor update", e);
      throw new BrmoException(
          "Fout bij transformeren bericht #"
              + ber.getId()
              + " voor update: "
              + e.getClass()
              + ": "
              + e.getMessage());
    }
  }

  public void updateBerichtException(Bericht ber, Throwable e) throws BrmoException {
    ber.setStatus(Bericht.STATUS.RSGB_NOK);
    StringWriter sw = new StringWriter();
    e.printStackTrace(new PrintWriter(sw, true));
    ber.setOpmerking(ber.getOpmerking() + "\nFout: " + sw);
    if (orderBerichten) {
      if (errorState != null && errorState.equalsIgnoreCase("ignore")) {
        // TODO omdat we te vaak moeten stoppen, later soort exception erbij betrekken.
      } else {
        // volgorde belangrijk dus stoppen
        log.fatal("Mutatieverwerking en foutief bericht, dus stoppen!", e);
        throw new BrmoException("Mutatieverwerking en foutief bericht, dus stoppen!");
      }
    }
  }

  /**
   * verwerk een bericht.
   *
   * @param ber te verwerken bericht
   * @param pretransformedTableData tabel data uit staging
   * @param updateResult {@code true} update verwerkings resultaat in staging
   * @throws BrmoException if any
   */
  @Override
  public void handle(Bericht ber, List<TableData> pretransformedTableData, boolean updateResult)
      throws BrmoException {
    if (updateProcess != null) {
      update(ber, pretransformedTableData);
      return;
    }
    Connection conn = this.connRsgb;
    GeometryJdbcConverter jdbcConverter = this.geomToJdbc;
    DatabaseMetaData metaData = this.dbMetadata;
    if (ber.getSoort().equalsIgnoreCase(BrmoFramework.BR_BRK2)) {
      conn = this.connRsgbBrk;
      jdbcConverter = this.geomToJdbcBrk;
      metaData = this.dbMetadataBrk;
    }

    Bericht.STATUS newStatus = Bericht.STATUS.RSGB_OK;

    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    log.debug(
        String.format(
            "RSGB verwerking van %s bericht met id %s, object_ref %s, datum %tc",
            ber.getSoort(), ber.getId(), ber.getObjectRef(), ber.getDatum()));
    StringBuilder loadLog;
    if (pretransformedTableData == null) {
      loadLog = new StringBuilder();
    } else {
      loadLog = new StringBuilder(ber.getOpmerking());
    }
    loadLog.append(
        String.format(
            "%s: start verwerking %s bericht object ref %s, met id %s, berichtdatum %s\n",
            dateTimeFormat.format(new Date()),
            ber.getSoort(),
            ber.getObjectRef(),
            ber.getId(),
            dateFormat.format(ber.getDatum())));

    setHerkomstMetadata(ber.getSoort());

    long startTime = 0;
    try {

      if (conn.getAutoCommit()) {
        conn.setAutoCommit(false);
      }

      List<TableData> newList;
      if (pretransformedTableData != null) {
        loadLog.append("Bericht was al getransformeerd in pipeline\n");
        newList = pretransformedTableData;
      } else {
        loadLog.append("Transformeren naar RSGB database-xml en lezen resultaat... ");
        startTime = System.currentTimeMillis();
        newList = transformToTableData(ber);
        loadLog.append(
            String.format("%4.1fs\n", (System.currentTimeMillis() - startTime) / 1000.0));
      }
      // nieuwe starttijd voor laden in RSGB
      startTime = System.currentTimeMillis();

      // per bericht kijken of er een oud bericht is
      Bericht oud = null;
      if (orderBerichten) {
        oud = stagingProxy.getOldBericht(ber, loadLog);
      } else {
        // als geen orderberichten dan wordt stand ingelezen
        // en indien volgordenummer >=0 (geen stand) dan afbreken
        if (ber.getVolgordeNummer() >= 0) {
          throw new BrmoException("Standverwerking, maar bericht is mutatie: niet verwerken!");
        }
        loadLog.append(
            "Standverwerking, gegevens worden alleen toegevoegd als ze nog niet bestaan!\n");
      }

      // oud bericht
      if (oud != null) {
        loadLog.append(
            String.format(
                "Eerder bericht (id: %d) voor zelfde object gevonden van datum %s, volgnummer %d,status %s op %s\n",
                oud.getId(),
                dateTimeFormat.format(oud.getDatum()),
                oud.getVolgordeNummer(),
                oud.getStatus().toString(),
                dateTimeFormat.format(oud.getStatusDatum())));

        if (ber.getDatum().equals(oud.getDatum())
            && ber.getVolgordeNummer().equals(oud.getVolgordeNummer())) {
          if (Objects.equals(ber.getId(), oud.getId())) {
            // dit kan voorkomen bij herstart van job terwijl bericht nog in pipeline
            // van andere job zat
            // niets doen, log overnemen.
            loadLog.append(oud.getOpmerking());
          } else {
            loadLog.append(
                "Datum en volgordenummer van nieuw bericht hetzelfde als de oude, negeer dit bericht!\n");
            boolean dbXmlEquals = ber.getDbXml().equals(oud.getDbXml());
            if (!dbXmlEquals) {
              String s =
                  String.format(
                      "Bericht %d met zelfde datum en volgnummer als eerder verwerkt bericht %d heeft andere db xml! Object ref %s",
                      ber.getId(), oud.getId(), ber.getObjectRef());
              log.warn(s);
              loadLog.append("Waarschuwing: ").append(s).append("\n");
            }
          }
        } else {
          // Check of eerder bericht toevallig niet nieuwere datum
          if (oud.getDatum().after(ber.getDatum())) {
            newStatus = Bericht.STATUS.RSGB_OUTDATED;
            loadLog.append(
                "Bericht bevat oudere data dan eerder verwerkt bericht, status RSGB_OUTDATED\n");
            // Check of eerder bericht toevallig niet zelfde datum met hoger volgorde
            // nummer, mantis-6098
          } else if (oud.getDatum().equals(ber.getDatum())
              && oud.getVolgordeNummer() > ber.getVolgordeNummer()) {
            newStatus = Bericht.STATUS.RSGB_OUTDATED;
            loadLog.append(
                "Bericht bevat oudere data dan eerder verwerkt bericht met hoger volgnummer, status RSGB_OUTDATED\n");
          } else {
            StringReader oReader = new StringReader(oud.getDbXml());
            List<TableData> oudList = oldDbXmlReader.readDataXML(new StreamSource(oReader));

            parseNewData(oudList, newList, loadLog, jdbcConverter, conn, metaData);
            parseOldData(oudList, newList, ber.getDatum(), loadLog, jdbcConverter, conn, metaData);
          }
        }
      } else {
        parseNewData(null, newList, loadLog, jdbcConverter, conn, metaData);
      }

      Split commit = SimonManager.getStopwatch(simonNamePrefix + "commit").start();
      conn.commit();
      commit.stop();
      ber.setStatus(newStatus);

      this.processed++;
      if (listener != null) {
        listener.progress(this.processed);
      }

    } catch (Throwable ex) {
      log.error(
          "Fout bij verwerking bericht met id "
              + ber.getId()
              + ", melding: "
              + ex.getLocalizedMessage());
      log.debug("Fout bij verwerking bericht met id " + ber.getId(), ex);
      try {
        conn.rollback();
      } catch (SQLException e) {
        log.debug("Rollback exception", e);
      }
      ber.setOpmerking(loadLog.toString());
      loadLog = null;
      updateBerichtException(ber, ex);
    } finally {
      if (loadLog == null) {
        loadLog = new StringBuilder(ber.getOpmerking());
      }
      loadLog.append("\n\n");
      loadLog.append(
          String.format(
              "%s: einde verwerking, duur: %4.1fs",
              dateTimeFormat.format(new Date()),
              (System.currentTimeMillis() - startTime) / 1000.0));
      ber.setOpmerking(loadLog.toString());

      if (updateResult) {
        updateProcessingResult(ber);
      }

      if (recentSavepoint != null) {
        // Set savepoint to null, as it is automatically released after commit the
        // transaction.
        recentSavepoint = null;
      }

      if (renewConnectionAfterCommit) {
        log.debug("Vernieuwen van verbinding om cursors in Oracle vrij te geven!");
        if (!(jdbcConverter instanceof OracleJdbcConverter)) {
          log.info(
              "Vernieuwen van verbinding is alleen nodig bij sommige Oracle implementeties: lijkt hier overbodig!");
        }
        try {
          renewConnection();
        } catch (SQLException ex) {
          throw new BrmoException("Error renewing connection", ex);
        }
      }
    }
  }

  public void update(Bericht jobBericht, List<TableData> pretransformedTableData)
      throws BrmoException {
    Connection conn = this.connRsgb;
    GeometryJdbcConverter jdbcConverter = this.geomToJdbc;
    DatabaseMetaData metaData = dbMetadata;
    if (jobBericht.getSoort().equalsIgnoreCase(BrmoFramework.BR_BRK2)) {
      conn = this.connRsgbBrk;
      jdbcConverter = this.geomToJdbcBrk;
      metaData = this.dbMetadataBrk;
    }

    try {
      if (conn.getAutoCommit()) {
        conn.setAutoCommit(false);
      }

      List<TableData> newList;
      if (pretransformedTableData != null) {
        newList = pretransformedTableData;
      } else {
        newList = transformUpdateTableData(jobBericht);
      }

      log.trace("data voor update: " + newList);
      for (TableData newData : newList) {
        for (TableRow row : newData.getRows()) {

          boolean comfortFound =
              rowExistsInDb(row, new StringBuilder(), jdbcConverter, conn, metaData);

          // zoek obv natural key op in rsgb
          if (comfortFound) {
            createUpdateSql(row, new StringBuilder(), jdbcConverter, conn, metaData);
          } else {
            // insert all comfort records into hoofdtabel
            createInsertSql(row, false, new StringBuilder(), jdbcConverter, conn, metaData);
          }
        }
      }
      conn.commit();

      this.processed++;
      if (listener != null) {
        listener.progress(this.processed);
      }
    } catch (Throwable ex) {
      log.error("Fout bij updaten bericht met id " + jobBericht.getId(), ex);
      try {
        conn.rollback();
      } catch (SQLException e) {
        log.debug("Rollback exception", e);
      }
    }
  }

  private RsgbTransformer getTransformer(String brType)
      throws TransformerConfigurationException, ParserConfigurationException {
    RsgbTransformer t = rsgbTransformers.get(brType);
    if (t == null) {
      t =
          switch (brType) {
            case BrmoFramework.BR_BRK2 -> new RsgbTransformer(BrmoFramework.XSL_BRK2);
            case BrmoFramework.BR_BRP ->
                new RsgbBRPTransformer(BrmoFramework.XSL_BRP, this.stagingProxy);
            case BrmoFramework.BR_NHR -> new RsgbTransformer(BrmoFramework.XSL_NHR);
            case BrmoFramework.BR_GBAV -> new RsgbTransformer(BrmoFramework.XSL_GBAV);
            case BrmoFramework.BR_WOZ ->
                new RsgbWOZTransformer(BrmoFramework.XSL_WOZ, this.stagingProxy);
            default -> throw new IllegalArgumentException("Onbekende basisregistratie: " + brType);
          };
      rsgbTransformers.put(brType, t);
    }
    return t;
  }

  private StringBuilder parseNewData(
      List<TableData> oldList,
      List<TableData> newList,
      StringBuilder loadLog,
      GeometryJdbcConverter jdbcConverter,
      Connection conn,
      DatabaseMetaData metaData)
      throws SQLException, ParseException, BrmoException {
    Split split = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata").start();

    // parse new db xml
    loadLog.append("\nVerwerk nieuw bericht");
    if (newList == null || newList.isEmpty()) {
      if (oldList == null || oldList.isEmpty()) {
        throw new BrmoException(
            "Bericht verwijdert object waarvoor geen eerder bericht gevonden is!");
      }
      split.stop();
      return loadLog;
    }

    String lastExistingBrondocumentId = null;
    for (TableData newData : newList) {
      if (!newData.isComfortData() && !newData.isDeleteData()) {
        Split splitAuthentic =
            SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.authentic").start();
        // auth data
        loadLog.append("\n");
        for (TableRow row : newData.getRows()) {

          // Controleer op al bestaand stukdeel
          if (lastExistingBrondocumentId != null
              && "brondocument".equalsIgnoreCase(row.getTable())
              && lastExistingBrondocumentId.equals(row.getColumnValue("tabel_identificatie"))) {
            loadLog.append("\nOverslaan stukdeel voor stuk ").append(lastExistingBrondocumentId);
            SimonManager.getCounter(simonNamePrefix + "parsenewdata.authentic.skipstukdeel")
                .increase();
            continue;
          } else {
            lastExistingBrondocumentId = null;
          }

          Split split2 =
              SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.authentic.getpk").start();
          List<String> pkColumns = getPrimaryKeys(row.getTable(), jdbcConverter, metaData);
          split2.stop();

          boolean existsInOldList = doesRowExist(row, pkColumns, oldList);
          split2 =
              SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.authentic.rowexistsindb")
                  .start();
          // er kan een record bestaan zonder dat er
          // een oud bericht aan ten grondslag ligt.
          // dan maar update maar zonder historie vastlegging.
          // oud record kan niet worden terugherleid.
          // TODO: dure check, kan het anders?
          boolean doInsert = !rowExistsInDb(row, loadLog, jdbcConverter, conn, metaData);
          split2.stop();

          // insert hoofdtabel
          if (doInsert) {
            if (existsInOldList) {
              // insert in hoofdtabel, terwijl er wel oud bericht beschikbaar is
              // dit lijkt op een fout situatie, wat te doen?
              loadLog.append(
                  "\nOud bericht bestaat, maar staat niet (meer) in database, daarom toevoeging van object aan tabel: ");
            } else {
              // normale insert in hoofdtabel
              loadLog.append("\nNormale toevoeging van object aan tabel: ");
            }
            loadLog.append(row.getTable());
            split2 =
                SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.authentic.insert")
                    .start();
            createInsertSql(row, false, loadLog, jdbcConverter, conn, metaData);
            split2.stop();
          } else {
            split2 =
                SimonManager.getStopwatch(
                        simonNamePrefix + "parsenewdata.authentic.isalreadyinmetadata")
                    .start();
            boolean inMetaDataTable =
                isAlreadyInMetadata(row, loadLog, jdbcConverter, conn, metaData);
            split2.stop();
            // wis metadata en update hoofdtabel
            if (inMetaDataTable) {
              loadLog.append("\nwis uit metadata tabel (upgrade van comfort naar authentiek). ");
              split2 =
                  SimonManager.getStopwatch(
                          simonNamePrefix + "parsenewdata.authentic.deletefrommetadata")
                      .start();
              deleteFromMetadata(row, loadLog, jdbcConverter, conn, metaData);
              split2.stop();
            }
            if ("brondocument".equalsIgnoreCase(row.getTable())) {
              // Nooit stukdeel wat al bestaat updaten, en ook alle
              // volgende stukdelen niet
              lastExistingBrondocumentId = row.getColumnValue("tabel_identificatie");
              loadLog
                  .append("\nRij voor stukdeel ")
                  .append(lastExistingBrondocumentId)
                  .append(" bestaat al, geen update");
              continue;
            }

            if (existsInOldList) {
              // update end date old record
              loadLog.append("\nUpdate einddatum in vorige versie object");
              TableRow oldRow = getMatchingRowFromTableData(row, pkColumns, oldList);
              String newBeginDate = getValueFromTableRow(row, row.getColumnDatumBeginGeldigheid());
              updateValueInTableRow(oldRow, row.getColumnDatumEindeGeldigheid(), newBeginDate);

              // write old to archive table
              loadLog.append("\nSchrijf vorige versie naar archief tabel");
              oldRow.setIgnoreDuplicates(true);

              split2 =
                  SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.authentic.archive")
                      .start();
              createInsertSql(oldRow, true, loadLog, jdbcConverter, conn, metaData);
              split2.stop();

              loadLog.append("\nUpdate object in tabel: ");
            } else {
              loadLog.append("\nUpdate object (zonder vastlegging historie) in tabel: ");
            }
            loadLog.append(row.getTable());
            split2 =
                SimonManager.getStopwatch(
                        simonNamePrefix + "parsenewdata.authentic.update." + row.getTable())
                    .start();
            createUpdateSql(row, loadLog, jdbcConverter, conn, metaData);
            split2.stop();
          }
        }
        splitAuthentic.stop();

      } else if (!newData.isDeleteData()) {
        Split splitComfort =
            SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.comfort").start();

        // TODO: select herkomst_br/datum uit herkomst_metadata voor tabel,kolom,waarde
        // indien niet bestaat: nieuwe comfort data, alles unconditional insert
        // indien wel bestaat en herkomst_br/datum identiek: gegevens uit stand al geinsert,
        // negeer
        // indien wel bestaat en herkomst_br/datum anders: update en insert nieuwe herkomst
        // comfort data
        String tabel = newData.getComfortSearchTable();
        String kolom = newData.getComfortSearchColumn();
        String waarde = newData.getComfortSearchValue();
        String datum = newData.getComfortSnapshotDate();

        Split split2;

        for (TableRow row : newData.getRows()) {
          String subclass = row.getTable();
          loadLog.append(
              String.format(
                  "\n\nComfort data voor %s (class: %s, %s=%s), controleer aanwezigheid in tabel",
                  subclass, tabel, kolom, waarde));
          split2 =
              SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.comfort.exists").start();
          boolean comfortFound = rowExistsInDb(row, loadLog, jdbcConverter, conn, metaData);
          split2.stop();

          // zoek obv natural key op in rsgb
          if (comfortFound) {
            split2 =
                SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.comfort.update").start();
            createUpdateSql(row, loadLog, jdbcConverter, conn, metaData);
            split2.stop();
          } else {
            // insert all comfort records into hoofdtabel
            split2 =
                SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.comfort.insert").start();
            createInsertSql(row, false, loadLog, jdbcConverter, conn, metaData);
            split2.stop();
          }
        }

        // Voeg herkomst van metadata toe, alleen voor superclass tabel!
        split2 =
            SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.comfort.metadata").start();
        if (metaData != dbMetadataBrk) {
          // overslaan voor brk 2
          createInsertMetadataSql(tabel, kolom, waarde, datum, loadLog, jdbcConverter, conn);
        }
        split2.stop();

        splitComfort.stop();
        loadLog.append("\n");

      } else if (newData.isDeleteData() && (oldList == null || oldList.isEmpty())) {
        // only use delete in newData if there is no old record (this is more complete)
        Split splitDelete =
            SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.delete").start();
        loadLog.append("\n\nWissen van object zonder oud record.");
        for (TableRow row : newData.getRows()) {
          // zoek obv natural key op in rsgb
          if (rowExistsInDb(row, loadLog, jdbcConverter, conn, metaData)) {
            Split split2 =
                SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.delete.update").start();
            createDeleteSql(row, loadLog, jdbcConverter, conn, metaData);
            split2.stop();
          } else {
            loadLog.append("\nTe wissen record niet in database.");
          }
        }
        splitDelete.stop();
        loadLog.append("\n");
      }
    }
    split.stop();
    return loadLog;
  }

  private StringBuilder parseOldData(
      List<TableData> oldList,
      List<TableData> newList,
      Date newDate,
      StringBuilder loadLog,
      GeometryJdbcConverter jdbcConverter,
      Connection connection,
      DatabaseMetaData metaData)
      throws SQLException, ParseException {

    List<TableRow> rowsToDelete = new ArrayList<>();
    // als heel object verwijderd moet worden omdat newList een verwijderbericht is
    boolean deleteAll = !newList.isEmpty() && newList.get(0).isDeleteData();

    // parse old db xml
    loadLog.append("\nControleren te verwijderen gegevens vorig bericht...\n");
    for (TableData oldData : oldList) {
      if (oldData.isComfortData()) {
        // comfort block ? do nothing
      } else {
        for (TableRow oldRow : oldData.getRows()) {
          List<String> pkColumns = getPrimaryKeys(oldRow.getTable(), jdbcConverter, metaData);
          boolean exists = doesRowExist(oldRow, pkColumns, newList);
          if (deleteAll || !exists) {
            loadLog
                .append("Vervallen record te verwijderen: ")
                .append(oldRow.toString(pkColumns))
                .append("\n");
            rowsToDelete.add(oldRow);
          }
        }
      }
    }

    if (!rowsToDelete.isEmpty()) {
      loadLog.append("Verwijder rijen in omgekeerde volgorde...\n");

      Collections.reverse(rowsToDelete);
      for (TableRow rowToDelete : rowsToDelete) {
        Split split = SimonManager.getStopwatch(simonNamePrefix + "parseolddata.delete").start();
        createDeleteSql(rowToDelete, loadLog, jdbcConverter, connection, metaData);
        split.stop();

        String beginDate =
            getValueFromTableRow(rowToDelete, rowToDelete.getColumnDatumBeginGeldigheid());
        String endDate = formatDateLikeOtherDate(newDate, beginDate);
        updateValueInTableRow(rowToDelete, rowToDelete.getColumnDatumEindeGeldigheid(), endDate);

        split = SimonManager.getStopwatch(simonNamePrefix + "parseolddata.archive").start();
        rowToDelete.setIgnoreDuplicates(true);
        createInsertSql(rowToDelete, true, loadLog, jdbcConverter, connection, metaData);
        split.stop();
      }
    }

    return loadLog;
  }

  /**
   * Probeer de datum te formatteren aan de hand van de formatting van de {@code otherDate}
   * formatting. De fallback optie voor formatting is {@code yyyy-MM-dd}, ondersteunde opties zijn:
   *
   * <ul>
   *   <li>{@code yyyy-MM-dd} (BRK2)
   *   <li>{@code yyyyMMddHHmmssSSS0} (BAG)
   * </ul>
   *
   * @param newDate te formatten datum
   * @param otherDate de formatted datum
   * @return formatted datum, indien mogelijk in de vorm van {@code otherDate}
   */
  private String formatDateLikeOtherDate(Date newDate, String otherDate) {
    // 2010-06-29 (BRK2)
    SimpleDateFormat dfltFmt = new SimpleDateFormat("yyyy-MM-dd");
    // 201006291200000000 (BAG)
    SimpleDateFormat f2 = new SimpleDateFormat("yyyyMMddHHmmssSSS0");

    if (otherDate == null || otherDate.isEmpty()) {
      // fallback op f1, er zijn gevallen waar otherDate null is, bijv. zak_recht
      return dfltFmt.format(newDate);
    }

    try {
      f2.parse(otherDate);
      return f2.format(newDate);
    } catch (java.text.ParseException ex) {
      // ignore
    }
    // add other formats
    return dfltFmt.format(newDate);
  }

  private String getValueFromTableRow(TableRow row, String column) {
    int i = 0;
    for (String c : row.getColumns()) {
      if (c.equalsIgnoreCase(column)) {
        return row.getValues().get(i);
      }
      i++;
    }
    return null;
  }

  private void updateValueInTableRow(TableRow row, String column, String newValue) {
    // TODO, mag later weer weg
    repairOldRowIfRequired(row, newValue);

    if (column == null) {
      return;
    }
    int i = 0;
    for (String c : row.getColumns()) {
      if (c.equalsIgnoreCase(column)) {
        row.getValues().set(i, newValue);
        return;
      }
      i++;
    }
  }

  private void repairOldRowIfRequired(TableRow row, String guessDate) {

    if (row.getTable().equals("kad_perceel")
        || row.getTable().equals("app_re")
        || row.getTable().equals("nummeraand")
        || row.getTable().equals("ligplaats")
        || row.getTable().equals("standplaats")
        || row.getTable().equals("verblijfsobj")) {
      if (!row.getColumns().contains("sc_dat_beg_geldh")) {
        row.getColumns().add("sc_dat_beg_geldh");
        row.getValues().add(guessDate);
      }
    }
  }

  private TableRow getMatchingRowFromTableData(
      TableRow row, List<String> pkColumns, List<TableData> testList) {
    TableRow newRow = null;

    for (TableData data : testList) {
      for (TableRow testRow : data.getRows()) {
        if (row.getTable().equals(testRow.getTable())) {
          int matches = 0;
          for (String pkColumn : pkColumns) {
            int rowIdx = row.getColumns().indexOf(pkColumn);
            int testIdx = testRow.getColumns().indexOf(pkColumn);

            String val = row.getValues().get(rowIdx);
            String testVal = testRow.getValues().get(testIdx);

            if (val.equals(testVal)) {
              matches++;
            }
          }

          if (matches == pkColumns.size()) {
            newRow = testRow;
          }
        }
      }
    }

    return newRow;
  }

  private boolean doesRowExist(TableRow row, List<String> pkColumns, List<TableData> testList) {
    boolean found = false;

    if (testList != null && testList.size() > 0) {
      for (TableData data : testList) {
        for (TableRow testRow : data.getRows()) {
          if (row.getTable().equals(testRow.getTable())) {
            int matches = 0;
            for (String pkColumn : pkColumns) {
              int rowIdx = row.getColumns().indexOf(pkColumn);
              int testIdx = testRow.getColumns().indexOf(pkColumn);

              String val = row.getValues().get(rowIdx);
              String testVal = testRow.getValues().get(testIdx);

              if (val.equals(testVal)) {
                matches++;
              }
            }

            if (matches == pkColumns.size()) {
              found = true;
            }
          }
        }
      }
    }

    return found;
  }

  private void createInsertSql(
      TableRow row,
      boolean useArchiveTable,
      StringBuilder loadLog,
      GeometryJdbcConverter jdbcConverter,
      Connection connection,
      DatabaseMetaData metaData)
      throws SQLException, ParseException {

    StringBuilder sql = new StringBuilder("insert into ");

    String tableName;
    if (!useArchiveTable) {
      tableName = row.getTable();
    } else {
      tableName = row.getTable() + "_archief";
    }

    sql.append(tableName);

    SortedSet<ColumnMetadata> tableColumnMetadata;
    tableColumnMetadata = getTableColumnMetadata(tableName, jdbcConverter, metaData);

    if (tableColumnMetadata == null) {
      if (useArchiveTable) {
        // Wanneer archief tabellen niet zijn aangemaakt negeren
        return;
      } else {
        throw new IllegalStateException("Kan tabel metadata niet vinden voor tabel " + tableName);
      }
    }

    sql.append(" (");
    StringBuilder valuesSql = new StringBuilder();
    List<Object> params = new ArrayList<>();

    // TODO maak sql op basis van alle columns en gebruik null values
    // zodat insert voor elke tabel hetzelfde, reuse preparedstatement
    Iterator<String> valuesIt = row.getValues().iterator();
    for (Iterator<String> it = row.getColumns().iterator(); it.hasNext(); ) {
      String column = it.next();
      String stringValue = valuesIt.next();

      if (row.isAlleenArchiefColumn(column) && !useArchiveTable) {
        continue;
      }

      ColumnMetadata cm = findColumnMetadata(tableColumnMetadata, column);
      Object param = getValueAsObject(tableName, column, stringValue, cm, jdbcConverter, metaData);
      params.add(param);

      sql.append(cm.getName());

      String insertValuePlaceholder = "?";
      if (cm.getTypeName().equals(jdbcConverter.getGeomTypeName())
          // in geval we in een ander dan public schema werken, en postgis zit in public
          // schema
          || cm.getTypeName()
              .replace("\"", "")
              .equals("public." + jdbcConverter.getGeomTypeName())) {
        // waarde altijd als WKT aanbieden en placeholder per db type
        insertValuePlaceholder = jdbcConverter.createPSGeometryPlaceholder();
      }
      valuesSql.append(insertValuePlaceholder);

      if (it.hasNext()) {
        sql.append(", ");
        valuesSql.append(", ");
      }
    }

    boolean conditionalInsert = row.isIgnoreDuplicates();

    if (!conditionalInsert) {
      sql.append(") values (");
      sql.append(valuesSql);
      sql.append(")");
    } else {
      sql.append(") select ");
      sql.append(valuesSql);
      if (jdbcConverter instanceof OracleJdbcConverter) {
        sql.append(" from dual");
      }
      sql.append(" where not exists (select 1 from ");
      sql.append(tableName);
      sql.append(" where ");

      boolean first = true;
      for (String column : getPrimaryKeys(tableName, jdbcConverter, metaData)) {
        if (first) {
          first = false;
        } else {
          sql.append(" and ");
        }
        sql.append(column);
        sql.append(" = ? ");

        String val = getValueFromTableRow(row, column);
        Object obj = getValueAsObject(tableName, column, val, null, jdbcConverter, metaData);
        params.add(obj);
      }
      sql.append(")");
    }

    PreparedStatement stm =
        checkAndGetPreparedStatement(insertSqlPreparedStatements, sql.toString());
    if (stm == null) {
      SimonManager.getCounter("b3p.rsgb.insertsql.preparestatement").increase();
      stm = connection.prepareStatement(sql.toString());
      checkAndAddStatement(insertSqlPreparedStatements, sql.toString(), stm);
    } else {
      SimonManager.getCounter("b3p.rsgb.insertsql.reusestatement").increase();
    }
    loadLog.append("\nSQL: ");
    loadLog.append(sql);
    loadLog.append(", params (");
    loadLog.append(params);
    loadLog.append(")");
    log.trace("insert SQL: " + sql + ", params: " + params);

    for (int i = 0; i < params.size(); i++) {
      Object param = params.get(i);
      // voor "null" geom moet hier iets extra gedaan worden voor sommige databases...
      if (null == param
          && (findColumnMetadata(tableColumnMetadata, row.getColumns().get(i))
                  .getTypeName()
                  .equals(jdbcConverter.getGeomTypeName())
              || findColumnMetadata(tableColumnMetadata, row.getColumns().get(i))
                  .getTypeName()
                  .replace("\"", "")
                  .equals("public." + jdbcConverter.getGeomTypeName()))
      // geeft: java.sql.SQLFeatureNotSupportedException: Unsupported feature: checkValidIndex
      // && stm.getParameterMetaData().getParameterTypeName(i +
      // 1).equals(geomToJdbc.getGeomTypeName())
      ) {
        if (jdbcConverter instanceof OracleJdbcConverter) {
          stm.setNull(i + 1, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
        } else {
          stm.setNull(i + 1, stm.getParameterMetaData().getParameterType(i + 1));
        }
      } else {
        stm.setObject(i + 1, param);
      }
    }

    int count = -1;
    try {
      count = stm.executeUpdate();
    } catch (SQLException e) {
      String message = e.getMessage();
      if (jdbcConverter.isDuplicateKeyViolationMessage(message) && row.isIgnoreDuplicates()) {
        if (recentSavepoint != null) {
          log.debug(
              "Ignoring duplicate key violation by rolling back to savepoint with id "
                  + recentSavepoint.getSavepointId());
          connection.rollback(recentSavepoint);
        }
      } else {
        throw e;
      }
    }

    loadLog.append("\nAantal toegevoegde records: ").append(count);
  }

  private void createUpdateSql(
      TableRow row,
      StringBuilder loadLog,
      GeometryJdbcConverter jdbcConverter,
      Connection connection,
      DatabaseMetaData metaData)
      throws SQLException, ParseException {
    // doSavePoint(row);

    StringBuilder sql = new StringBuilder("update ");

    String tableName = row.getTable();

    sql.append(tableName);

    SortedSet<ColumnMetadata> tableColumnMetadata;
    tableColumnMetadata = getTableColumnMetadata(tableName, jdbcConverter, metaData);

    if (tableColumnMetadata == null) {
      throw new IllegalStateException("Kan tabel metadata niet vinden voor tabel " + tableName);
    }

    sql.append(" set ");

    List<Object> params = new ArrayList<>();

    // XXX does set previously set columns to NULL!
    // need statement for all columns from column metadata, not only
    // columns in TableRow
    Iterator<String> valuesIt = row.getValues().iterator();
    for (Iterator<String> it = row.getColumns().iterator(); it.hasNext(); ) {
      String column = it.next();
      String stringValue = valuesIt.next();

      if (row.isAlleenArchiefColumn(column)) {
        continue;
      }

      ColumnMetadata cm = findColumnMetadata(tableColumnMetadata, column);
      Object param = getValueAsObject(tableName, column, stringValue, cm, jdbcConverter, metaData);
      params.add(param);

      sql.append(cm.getName());
      sql.append(" = ");
      String insertValuePlaceholder = "?";
      if (cm.getTypeName().equals(jdbcConverter.getGeomTypeName())
          // in geval we in een ander dan public schema werken, en postgis zit in public
          // schema
          || cm.getTypeName()
              .replace("\"", "")
              .equals("public." + jdbcConverter.getGeomTypeName())) {
        // waarde altijd als WKT aanbieden en placeholder per db type
        insertValuePlaceholder = jdbcConverter.createPSGeometryPlaceholder();
      }
      sql.append(insertValuePlaceholder);

      if (it.hasNext()) {
        sql.append(", ");
      }
    }

    // Where clause using pk columns
    sql.append(" where ");
    boolean first = true;
    for (String column : getPrimaryKeys(tableName, jdbcConverter, metaData)) {
      if (first) {
        first = false;
      } else {
        sql.append(" and ");
      }
      sql.append(column);
      sql.append(" = ? ");

      String val = getValueFromTableRow(row, column);
      Object obj = getValueAsObject(tableName, column, val, null, jdbcConverter, metaData);
      params.add(obj);
    }

    PreparedStatement stm =
        checkAndGetPreparedStatement(updateSqlPreparedStatements, sql.toString());
    if (stm == null) {
      SimonManager.getCounter("b3p.rsgb.updatesql.preparestatement").increase();
      stm = connection.prepareStatement(sql.toString());
      checkAndAddStatement(updateSqlPreparedStatements, sql.toString(), stm);
    } else {
      SimonManager.getCounter("b3p.rsgb.updatesql.reusestatement").increase();
    }
    loadLog.append("\nSQL: ");
    loadLog.append(sql);
    loadLog.append(", params (");
    loadLog.append(params);
    loadLog.append(")");
    log.debug("update SQL: " + sql + ", params: " + params);

    for (int i = 0; i < params.size(); i++) {
      Object param = params.get(i);
      // voor "null" geom moet hier iets extra gedaan worden voor sommige databases...
      if (null == param
          && (stm.getParameterMetaData()
                  .getParameterTypeName(i + 1)
                  .equals(jdbcConverter.getGeomTypeName())
              || stm.getParameterMetaData()
                  .getParameterTypeName(i + 1)
                  .replace("\"", "")
                  .equals("public." + jdbcConverter.getGeomTypeName()))) {
        log.trace(
            "gebruik `setNull` voor kolom: "
                + i
                + " "
                + row.getColumns().get(i)
                + ", waarde "
                + param
                + "("
                + stm.getParameterMetaData().getParameterTypeName(i + 1)
                + ")");
        if (jdbcConverter instanceof OracleJdbcConverter) {
          stm.setNull(i + 1, Types.STRUCT, "MDSYS.SDO_GEOMETRY");
        } else {
          stm.setNull(i + 1, stm.getParameterMetaData().getParameterType(i + 1));
        }
      } else {
        stm.setObject(i + 1, param, stm.getParameterMetaData().getParameterType(i + 1));
      }
    }

    int count = -1;
    try {
      count = stm.executeUpdate();
    } finally {
      loadLog.append("\nAantal record updates: ").append(count);
    }
  }

  /* Columns id, tabel, kolom, waarde, herkomst_br, datum (toestandsdatum) */
  private boolean createInsertMetadataSql(
      String tabel,
      String kolom,
      String waarde,
      String datum,
      StringBuilder loadLog,
      GeometryJdbcConverter jdbcConverter,
      Connection connection)
      throws SQLException {

    if (insertMetadataStatement == null || insertMetadataStatement.isClosed()) {
      String sql =
          "insert into herkomst_metadata (tabel, kolom, waarde, herkomst_br, datum) "
              + "select ?, ?, ?, ?, ? "
              + (jdbcConverter instanceof OracleJdbcConverter ? "from dual" : "")
              + " where not exists ("
              + "  select 1 from herkomst_metadata where tabel = ? and kolom = ? and waarde = ? and herkomst_br = ? and datum = ?"
              + ")";

      insertMetadataStatement = connection.prepareStatement(sql);
    } else {
      insertMetadataStatement.clearParameters();
    }

    Date date = null;
    Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(datum);
    if (calendar != null) {
      date = new java.sql.Date(calendar.getTimeInMillis());
    }

    insertMetadataStatement.setString(1, tabel);
    insertMetadataStatement.setString(2, kolom);
    insertMetadataStatement.setString(3, waarde);
    insertMetadataStatement.setString(4, getHerkomstMetadata());
    insertMetadataStatement.setObject(5, date);
    insertMetadataStatement.setString(6, tabel);
    insertMetadataStatement.setString(7, kolom);
    insertMetadataStatement.setString(8, waarde);
    insertMetadataStatement.setString(9, getHerkomstMetadata());
    insertMetadataStatement.setObject(10, date);

    loadLog.append(
        String.format(
            "\nConditioneel toevoegen herkomst metadata tabel=%s, kolom=%s, waarde=%s, herkomst_br=%s, datum=%s",
            tabel, kolom, waarde, getHerkomstMetadata(), datum));

    int count = insertMetadataStatement.executeUpdate();
    loadLog.append(": record geinsert: ").append(count > 0 ? "ja" : "nee (rij bestond al)");
    return count > 0;
  }

  private boolean rowExistsInDb(
      TableRow row,
      StringBuilder loadLog,
      GeometryJdbcConverter jdbcConverter,
      Connection conn,
      DatabaseMetaData metaData)
      throws SQLException, ParseException {

    String tableName = row.getTable();
    List<String> pkColumns = getPrimaryKeys(tableName, jdbcConverter, metaData);

    List<Object> params = new ArrayList<>();
    for (String column : pkColumns) {
      String val = getValueFromTableRow(row, column);
      Object obj = getValueAsObject(row.getTable(), column, val, null, jdbcConverter, metaData);
      params.add(obj);
    }

    loadLog.append("\nControleer ").append(tableName).append(" op primary key: ").append(params);

    PreparedStatement stm = checkAndGetPreparedStatement(checkRowExistsStatements, tableName);

    if (stm == null) {
      StringBuilder sql = new StringBuilder("select 1 from ").append(tableName);

      boolean first = true;
      for (String column : pkColumns) {
        if (first) {
          sql.append(" where ").append(column).append(" = ?");
          first = false;
        } else {
          sql.append(" and ").append(column).append(" = ?");
        }
      }

      stm =
          conn.prepareStatement(
              sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      checkAndAddStatement(checkRowExistsStatements, tableName, stm);
    }

    int i = 1;
    for (Object p : params) {
      stm.setObject(i++, p);
    }

    ResultSet rs = null;
    boolean exists = false;
    try {
      rs = stm.executeQuery();
      exists = rs.next();
    } finally {
      if (rs != null) {
        rs.close();
      }
    }
    loadLog.append(", rij bestaat: ").append(exists ? "ja" : "nee");

    return exists;
  }

  private boolean isAlreadyInMetadata(
      TableRow row,
      StringBuilder loadLog,
      GeometryJdbcConverter jdbcConverter,
      Connection connection,
      DatabaseMetaData metaData)
      throws SQLException, BrmoException {

    if (metaData == this.dbMetadataBrk) {
      // geen metadata voor brk 2
      return false;
    }

    if (connection == null || connection.isClosed()) {
      // if used not-threaded, init() required
      throw new BrmoException("No connection found");
    }

    List<String> params = new ArrayList<>();
    String herkomstTableName = "herkomst_metadata";
    String tableName = row.getTable();
    params.add(tableName);
    List<String> pkColumns = getPrimaryKeys(tableName, jdbcConverter, metaData);
    String column = null;
    if (pkColumns.size() == 1) {
      column = pkColumns.get(0);
    }
    params.add(column);
    String waarde = getValueFromTableRow(row, column);
    params.add(waarde);

    loadLog
        .append("\nControleer ")
        .append(herkomstTableName)
        .append(" op primary key: ")
        .append(params);

    PreparedStatement stm =
        checkAndGetPreparedStatement(checkRowExistsStatements, herkomstTableName);
    if (stm == null) {
      String sql =
          "select 1 from " + herkomstTableName + " WHERE tabel = ? AND kolom = ? AND kolom = ?";

      stm =
          connection.prepareStatement(
              sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      checkAndAddStatement(checkRowExistsStatements, herkomstTableName, stm);
    }

    int i = 1;
    for (Object p : params) {
      stm.setObject(i++, p);
    }

    ResultSet rs = null;
    boolean exists = false;
    try {
      rs = stm.executeQuery();
      exists = rs.next();
    } finally {
      if (rs != null) {
        rs.close();
      }
    }
    loadLog.append(", rij bestaat: ").append(exists ? "ja" : "nee");

    return exists;
  }

  private boolean createDeleteSql(
      TableRow row,
      StringBuilder loadLog,
      GeometryJdbcConverter jdbcConverter,
      Connection connection,
      DatabaseMetaData metaData)
      throws SQLException, ParseException {
    doSavePoint(row, jdbcConverter, connection);

    String tableName = row.getTable();
    // Where clause using pk columns
    List<String> pkColumns = getPrimaryKeys(tableName, jdbcConverter, metaData);

    List<Object> params = new ArrayList<>();
    for (String column : pkColumns) {
      String val = getValueFromTableRow(row, column);
      Object obj = getValueAsObject(row.getTable(), column, val, null, jdbcConverter, metaData);
      params.add(obj);
    }
    loadLog
        .append("\nVerwijderen rij uit ")
        .append(tableName)
        .append(" met primary key: ")
        .append(params);

    PreparedStatement stm = checkAndGetPreparedStatement(createDeleteSqlStatements, tableName);

    if (stm == null) {
      StringBuilder sql = new StringBuilder("delete from ").append(tableName);

      boolean first = true;
      for (String column : pkColumns) {
        if (first) {
          sql.append(" where ").append(column).append(" = ?");
          first = false;
        } else {
          sql.append(" and ").append(column).append(" = ?");
        }
      }

      stm =
          connection.prepareStatement(
              sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      checkAndAddStatement(createDeleteSqlStatements, tableName, stm);
    }

    int i = 1;
    for (Object p : params) {
      stm.setObject(i++, p);
    }

    int count = 1; // all ok
    try {
      if (alsoExecuteSql) {
        count = stm.executeUpdate();
      }
    } catch (SQLException e) {
      log.error("Error executing delete statement", e);
    }
    loadLog.append(", aantal rijen verwijderd: ").append(count);

    return count > 0;
  }

  private boolean deleteFromMetadata(
      TableRow row,
      StringBuilder loadLog,
      GeometryJdbcConverter jdbcConverter,
      Connection connection,
      DatabaseMetaData metaData)
      throws SQLException {

    List<String> params = new ArrayList<>();
    String herkomstTableName = "herkomst_metadata";
    String tableName = row.getTable();
    params.add(tableName);
    List<String> pkColumns = getPrimaryKeys(tableName, jdbcConverter, metaData);
    String column = null;
    if (pkColumns.size() == 1) {
      column = pkColumns.get(0);
    }
    params.add(column);
    String waarde = getValueFromTableRow(row, column);
    params.add(waarde);

    loadLog
        .append("\nVerwijder rij uit ")
        .append(herkomstTableName)
        .append(" met primary key: ")
        .append(params);

    PreparedStatement stm =
        checkAndGetPreparedStatement(createDeleteSqlStatements, herkomstTableName);
    if (stm == null) {
      String sql =
          "delete from " + herkomstTableName + " WHERE tabel = ? AND kolom = ? AND kolom = ?";

      stm =
          connection.prepareStatement(
              sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
      checkAndAddStatement(createDeleteSqlStatements, herkomstTableName, stm);
    }

    int i = 1;
    for (Object p : params) {
      stm.setObject(i++, p);
    }

    int count = 1; // all ok
    try {
      if (alsoExecuteSql) {
        count = stm.executeUpdate();
      }
    } catch (SQLException e) {
      log.error("Error executing delete statement", e);
    }
    loadLog.append(", aantal rijen verwijderd: ").append(count);

    return count > 0;
  }

  private List<String> getPrimaryKeys(
      String tableName, GeometryJdbcConverter jdbcConverter, DatabaseMetaData metaData)
      throws SQLException {

    List<String> pks = primaryKeyCache.get(tableName);
    if (pks != null) {
      return pks;
    }

    pks = new ArrayList<>();

    String origName = tables.get(tableName);

    if (origName == null) {
      throw new IllegalArgumentException("Tabel bestaat niet: " + tableName);
    }

    ResultSet set = metaData.getPrimaryKeys(null, jdbcConverter.getSchema(), origName);
    while (set.next()) {
      String column = set.getString("COLUMN_NAME");
      pks.add(column.toLowerCase());
    }

    set.close();
    primaryKeyCache.put(tableName, pks);

    return pks;
  }

  private SortedSet<ColumnMetadata> getTableColumnMetadata(
      String table, GeometryJdbcConverter jdbcConverter, DatabaseMetaData metaData)
      throws SQLException {

    table = table.toLowerCase();

    if (!tables.containsKey(table)) {
      return null;
    }

    SortedSet<ColumnMetadata> columnMetadata = tableColumns.get(table);
    if (columnMetadata == null) {
      columnMetadata = new TreeSet<>();
      tableColumns.put(table, columnMetadata);

      ResultSet columnsRs =
          metaData.getColumns(null, jdbcConverter.getSchema(), tables.get(table), "%");
      while (columnsRs.next()) {
        ColumnMetadata cm = new ColumnMetadata(columnsRs);
        columnMetadata.add(cm);
      }

      columnsRs.close();
    }

    return columnMetadata;
  }

  private ColumnMetadata findColumnMetadata(
      SortedSet<ColumnMetadata> tableColumnMetadata, String columnName) {

    if (tableColumnMetadata == null || tableColumnMetadata.size() < 1) {
      return null;
    }

    for (ColumnMetadata cm : tableColumnMetadata) {
      if (cm.getName().equalsIgnoreCase(columnName)) {
        return cm;
      }
    }

    return null;
  }

  public String getHerkomstMetadata() {
    return herkomstMetadata;
  }

  public void setHerkomstMetadata(String herkomstMetadata) {
    this.herkomstMetadata = herkomstMetadata;
  }

  private void doSavePoint(TableRow row, GeometryJdbcConverter jdbcConverter, Connection connection)
      throws SQLException {
    if (!alsoExecuteSql) {
      return;
    }
    if (jdbcConverter.useSavepoints()) {
      if (row.isIgnoreDuplicates()) {
        if (recentSavepoint == null) {
          recentSavepoint = connection.setSavepoint();
          log.trace("Created savepoint with id: " + recentSavepoint.getSavepointId());
        } else {
          log.trace(
              "No need for new savepoint, previous insert caused rollback to recent savepoint with id "
                  + recentSavepoint.getSavepointId());
        }
      } else if (recentSavepoint != null) {
        log.trace(
            "About to insert non-recoverable row, discarding savepoint with id "
                + recentSavepoint.getSavepointId());
        connection.releaseSavepoint(recentSavepoint);
        recentSavepoint = null;
      }
    }
  }

  private Object getValueAsObject(
      String tableName,
      String column,
      String value,
      ColumnMetadata cm,
      GeometryJdbcConverter jdbcConverter,
      DatabaseMetaData metaData)
      throws SQLException, ParseException {
    if (cm == null) {
      SortedSet<ColumnMetadata> tableColumnMetadata;
      tableColumnMetadata = getTableColumnMetadata(tableName, jdbcConverter, metaData);
      cm = findColumnMetadata(tableColumnMetadata, column);
    }

    Object param = null;
    if (cm == null) {
      throw new IllegalArgumentException("Column not found: " + column + " in table " + tableName);
    } else if (cm.getTypeName().equals(jdbcConverter.getGeomTypeName())
        // in geval we in een ander dan public schema werken, en postgis zit in public
        // schema
        || cm.getTypeName().replace("\"", "").equals("public." + jdbcConverter.getGeomTypeName())) {
      param = jdbcConverter.convertToNativeGeometryObject(value);
    } else if (value != null) {
      param = GeometryJdbcConverter.convertToSQLObject(value, cm, tableName, column);
    }

    return param;
  }

  public enum BerichtSelectMode {
    BY_STATUS,
    BY_IDS,
    BY_LAADPROCES,
    FOR_UPDATE,
    RETRY_WAITING
  }
}
