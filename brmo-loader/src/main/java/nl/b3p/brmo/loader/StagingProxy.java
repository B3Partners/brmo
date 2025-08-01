package nl.b3p.brmo.loader;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import javax.xml.transform.TransformerException;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.BerichtenSorter;
import nl.b3p.brmo.loader.entity.Brk2Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.pipeline.BerichtTypeOfWork;
import nl.b3p.brmo.loader.pipeline.BerichtWorkUnit;
import nl.b3p.brmo.loader.pipeline.ProcessDbXmlPipeline;
import nl.b3p.brmo.loader.util.BrmoDuplicaatLaadprocesException;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import nl.b3p.brmo.loader.util.RsgbTransformer;
import nl.b3p.brmo.loader.util.StagingRowHandler;
import nl.b3p.brmo.loader.util.TableData;
import nl.b3p.brmo.loader.xml.BRPXMLReader;
import nl.b3p.brmo.loader.xml.Brk2SnapshotXMLReader;
import nl.b3p.brmo.loader.xml.BrmoXMLReader;
import nl.b3p.brmo.loader.xml.GbavXMLReader;
import nl.b3p.brmo.loader.xml.NhrXMLReader;
import nl.b3p.brmo.loader.xml.WozXMLReader;
import nl.b3p.jdbc.util.converter.GeometryJdbcConverter;
import nl.b3p.jdbc.util.converter.GeometryJdbcConverterFactory;
import nl.b3p.jdbc.util.converter.OracleJdbcConverter;
import nl.b3p.jdbc.util.converter.PostgisJdbcConverter;
import nl.b3p.jdbc.util.dbutils.LongColumnListHandler;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.xml.sax.SAXException;

/**
 * @author Boy de Wit
 */
public class StagingProxy {

  private static final Log log = LogFactory.getLog(StagingProxy.class);

  private Connection connStaging;
  private DataSource dataSourceStaging;
  private GeometryJdbcConverter geomToJdbc;
  private Integer batchCapacity = 150;
  private Integer limitStandBerichtenToTransform = -1;

  public StagingProxy(DataSource dataSourceStaging) throws SQLException, BrmoException {
    this.dataSourceStaging = dataSourceStaging;
    this.connStaging = getConnection();
    geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(connStaging);
  }

  public void closeStagingProxy() {
    if (getOldBerichtStatement != null) {
      DbUtils.closeQuietly(getOldBerichtStatement);
    }
    DbUtils.closeQuietly(connStaging);
  }

  private Connection getConnection() throws SQLException {
    if (connStaging == null || connStaging.isClosed()) {
      connStaging = dataSourceStaging.getConnection();
    }
    return connStaging;
  }

  public LaadProces getLaadProcesById(Long id) throws SQLException {
    List<LaadProces> processen;

    ResultSetHandler<List<LaadProces>> h =
        new BeanListHandler<>(LaadProces.class, new StagingRowHandler());

    processen =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .query(
                getConnection(),
                "SELECT * FROM " + BrmoFramework.LAADPROCES_TABEL + " WHERE id = ?",
                h,
                id);

    if (processen != null && processen.size() == 1) {
      return processen.get(0);
    }

    return null;
  }

  public Bericht getBerichtById(Long id) throws SQLException {
    List<Bericht> berichten;
    ResultSetHandler<List<Bericht>> h =
        new BeanListHandler<>(Bericht.class, new StagingRowHandler());

    berichten =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .query(
                getConnection(),
                "SELECT * FROM " + BrmoFramework.BERICHT_TABLE + " WHERE id = ?",
                h,
                id);

    if (berichten != null && berichten.size() == 1) {
      return berichten.get(0);
    }

    return null;
  }

  public List<Bericht> getBerichtByLaadProces(LaadProces lp) throws SQLException {
    List<Bericht> berichten;
    ResultSetHandler<List<Bericht>> h =
        new BeanListHandler<>(Bericht.class, new StagingRowHandler());

    berichten =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .query(
                getConnection(),
                "SELECT * FROM " + BrmoFramework.BERICHT_TABLE + " WHERE laadprocesid = ?",
                h,
                lp.getId());
    return berichten;
  }

  public LaadProces getLaadProcesByFileName(String name) throws SQLException {
    List<LaadProces> processen;
    ResultSetHandler<List<LaadProces>> h =
        new BeanListHandler<>(LaadProces.class, new StagingRowHandler());

    processen =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .query(
                getConnection(),
                "SELECT * FROM " + BrmoFramework.LAADPROCES_TABEL + " WHERE bestand_naam = ?",
                h,
                name);

    if (processen != null && processen.size() == 1) {
      return processen.get(0);
    }

    return null;
  }

  public LaadProces getLaadProcesByRestoredFilename(String name) throws SQLException {
    List<LaadProces> processen;
    ResultSetHandler<List<LaadProces>> h =
        new BeanListHandler<>(LaadProces.class, new StagingRowHandler());

    processen =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .query(
                getConnection(),
                "SELECT * FROM "
                    + BrmoFramework.LAADPROCES_TABEL
                    + " WHERE bestand_naam_hersteld = ?",
                h,
                name);

    if (processen != null && processen.size() == 1) {
      return processen.get(0);
    }

    return null;
  }

  /**
   * bepaal of bericht bestaat aan de hand van laadprocesid, object_ref, datum en volgordenummer.
   *
   * @param b het bestaande bericht
   * @return het bestaande bericht uit de database
   * @throws SQLException if any
   */
  public Bericht getExistingBericht(Bericht b) throws SQLException {
    return getBerichtByNaturalKey(b.getObjectRef(), b.getDatum().getTime(), b.getVolgordeNummer());
  }

  private Bericht getBerichtByNaturalKey(String object_ref, long date, Integer volgnr)
      throws SQLException {
    List<Bericht> processen;
    ResultSetHandler<List<Bericht>> h =
        new BeanListHandler<>(Bericht.class, new StagingRowHandler());

    processen =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .query(
                getConnection(),
                "SELECT * FROM "
                    + BrmoFramework.BERICHT_TABLE
                    + " WHERE object_ref = ?"
                    + " AND datum = ? and volgordenummer = ?",
                h,
                object_ref,
                new Timestamp(date),
                volgnr);

    if (processen != null && processen.size() > 0) {
      return processen.get(0);
    }

    return null;
  }

  public long getCountJob() throws SQLException {
    Object o =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .query(
                getConnection(),
                "select count(*) from " + BrmoFramework.JOB_TABLE,
                new ScalarHandler<>());
    if (o instanceof BigDecimal) {
      return ((BigDecimal) o).longValue();
    } else if (o instanceof Integer) {
      return ((Integer) o).longValue();
    }
    return (Long) o;
  }

  public boolean removeJob() throws SQLException {
    int count =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .update(getConnection(), "truncate table " + BrmoFramework.JOB_TABLE);
    return count > 0;
  }

  public boolean cleanJob() throws SQLException {
    int count;
    if (geomToJdbc instanceof OracleJdbcConverter) {
      count =
          new QueryRunner(geomToJdbc.isPmdKnownBroken())
              .update(
                  getConnection(),
                  "delete from "
                      + BrmoFramework.JOB_TABLE
                      + " j where j.id in (select b.id from "
                      + BrmoFramework.BERICHT_TABLE
                      + " b "
                      // vanwege rare fout in oracle bij ophalen tabel
                      // metadata werkt dit niet
                      // https://issues.apache.org/jira/browse/DBUTILS-125
                      + " where b.id = j.id and status != 'STAGING_OK')");

    } else {
      count =
          new QueryRunner(geomToJdbc.isPmdKnownBroken())
              .update(
                  getConnection(),
                  "delete from "
                      + BrmoFramework.JOB_TABLE
                      + " j where j.id in (select b.id from "
                      + BrmoFramework.BERICHT_TABLE
                      + " b "
                      + " where b.id = j.id and status != ?)",
                  Bericht.STATUS.STAGING_OK.toString());
    }
    return count > 0;
  }

  public long setBerichtenJobByStatus(Bericht.STATUS status, boolean orderBerichten)
      throws SQLException {
    StringBuilder q =
        new StringBuilder(
            "insert into "
                + BrmoFramework.JOB_TABLE
                + " (id, datum, volgordenummer, object_ref, br_xml, soort) "
                + " select id, datum, volgordenummer, object_ref, br_xml, soort from "
                + BrmoFramework.BERICHT_TABLE
                + " where status = ? and datum <= ? ");
    if (orderBerichten) {
      q.append(" order by ").append(BerichtenSorter.SQL_ORDER_BY);
    } else {
      q.append(" and volgordenummer < 0 ");
      if (this.limitStandBerichtenToTransform > 0) {
        q = geomToJdbc.buildLimitSql(q, limitStandBerichtenToTransform);
      }
    }
    log.debug("pre-transformatie SQL: " + q);
    return new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .update(
            getConnection(),
            q.toString(),
            status.toString(),
            new java.sql.Timestamp((new Date()).getTime()));
  }

  public long setBerichtenJobForUpdate(String soort, boolean orderBerichten) throws SQLException {
    StringBuilder q =
        new StringBuilder(
            "insert into "
                + BrmoFramework.JOB_TABLE
                + " (id, datum, volgordenummer, object_ref, br_xml, soort) "
                + " select id, datum, volgordenummer, object_ref, br_xml, soort from "
                + BrmoFramework.BERICHT_TABLE
                + " where status = ? and soort = ? and datum <= ? ");
    if (orderBerichten) {
      q.append(" order by ").append(BerichtenSorter.SQL_ORDER_BY);
    } else {
      q.append(" and volgordenummer < 0 ");
      if (this.limitStandBerichtenToTransform > 0) {
        q = geomToJdbc.buildLimitSql(q, limitStandBerichtenToTransform);
      }
    }
    log.debug("pre-transformatie SQL: " + q);
    return new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .update(
            getConnection(),
            q.toString(),
            Bericht.STATUS.RSGB_OK.toString(),
            soort,
            new java.sql.Timestamp((new Date()).getTime()));
  }

  public long setBerichtenJobByIds(long[] ids, boolean orderBerichten) throws SQLException {
    StringBuilder q =
        new StringBuilder(
            "insert into "
                + BrmoFramework.JOB_TABLE
                + " (id, datum, volgordenummer, object_ref, br_xml, soort) "
                + " select id, datum, volgordenummer, object_ref, br_xml, soort from "
                + BrmoFramework.BERICHT_TABLE
                + " where id in (");
    for (int i = 0; i < ids.length; i++) {
      if (i != 0) {
        q.append(",");
      }
      q.append(ids[i]);
    }
    q.append(") and datum <= ? ");
    if (orderBerichten) {
      q.append(" order by ").append(BerichtenSorter.SQL_ORDER_BY);
    } else {
      q.append(" and volgordenummer < 0 ");
      if (this.limitStandBerichtenToTransform > 0) {
        q = geomToJdbc.buildLimitSql(q, limitStandBerichtenToTransform);
      }
    }
    log.debug("pre-transformatie SQL: " + q);
    return new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .update(getConnection(), q.toString(), new java.sql.Timestamp((new Date()).getTime()));
  }

  public long setBerichtenJobByLaadprocessen(long[] laadprocesIds, boolean orderBerichten)
      throws SQLException {

    StringBuilder q =
        new StringBuilder(
            "insert into "
                + BrmoFramework.JOB_TABLE
                + " (id, datum, volgordenummer, object_ref, br_xml, soort) "
                + " select id, datum, volgordenummer, object_ref, br_xml, soort from "
                + BrmoFramework.BERICHT_TABLE
                + " where laadprocesid in (");
    for (int i = 0; i < laadprocesIds.length; i++) {
      if (i != 0) {
        q.append(",");
      }
      q.append(laadprocesIds[i]);
    }
    q.append(") and status = ? and datum <= ? ");
    if (orderBerichten) {
      q.append(" order by ").append(BerichtenSorter.SQL_ORDER_BY);
    } else {
      q.append(" and volgordenummer < 0 ");
      if (this.limitStandBerichtenToTransform > 0) {
        q = geomToJdbc.buildLimitSql(q, limitStandBerichtenToTransform);
      }
    }
    log.debug("pre-transformatie SQL: " + q);
    return new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .update(
            getConnection(),
            q.toString(),
            Bericht.STATUS.STAGING_OK.toString(),
            new java.sql.Timestamp((new Date()).getTime()));
  }

  public void handleBerichtenByJob(
      long total,
      final BerichtenHandler handler,
      final boolean enablePipeline,
      int transformPipelineCapacity,
      boolean orderBerichten)
      throws Exception {
    Split split = SimonManager.getStopwatch("b3p.rsgb.job").start();
    final String dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    Split jobSplit = SimonManager.getStopwatch("b3p.rsgb.job." + dateTime).start();
    ((RsgbProxy) handler).setSimonNamePrefix("b3p.rsgb.job." + dateTime + ".");
    final RowProcessor processor = new StagingRowHandler();

    final ProcessDbXmlPipeline processDbXmlPipeline =
        new ProcessDbXmlPipeline(
            handler, transformPipelineCapacity, "b3p.rsgb.job." + dateTime + ".pipeline");
    if (enablePipeline) {
      processDbXmlPipeline.start();
    }
    long offset = 0L;
    int batch = batchCapacity;
    final MutableInt processed = new MutableInt(0);
    final MutableInt lastJid = new MutableInt(0);
    final boolean doOrderBerichten = orderBerichten;
    boolean abort = false;
    try {
      do {
        log.debug(
            String.format(
                "Ophalen berichten batch Job tabel, offset %d, limit %d",
                lastJid.intValue(), batch));
        String sql =
            "select jid, id, datum, volgordenummer, object_ref, br_xml, soort from "
                + BrmoFramework.JOB_TABLE
                + " where jid > "
                + lastJid.intValue()
                + " order by jid ";
        sql = geomToJdbc.buildPaginationSql(sql, 0, batch);
        log.debug("SQL voor ophalen berichten batch: " + sql);

        processed.setValue(0);
        final Split getBerichten =
            SimonManager.getStopwatch("b3p.rsgb.job." + dateTime + ".staging.berichten.getbatch")
                .start();
        Exception e =
            new QueryRunner(geomToJdbc.isPmdKnownBroken())
                .query(
                    getConnection(),
                    sql,
                    rs -> {
                      getBerichten.stop();
                      final Split processResultSet =
                          SimonManager.getStopwatch(
                                  "b3p.rsgb.job."
                                      + dateTime
                                      + ".staging.berichten.processresultset")
                              .start();

                      while (rs.next()) {
                        try {
                          Bericht bericht = processor.toBean(rs, Bericht.class);
                          if (bericht.getVolgordeNummer() >= 0 && !doOrderBerichten) {
                            // mutaties hebben volgnr >=0 dan sortering
                            // verplicht
                            throw new Exception(
                                String.format(
                                    "Het sorteren van berichten staat uit, terwijl bericht (id: %d) geen standbericht is (volgnummer >=0)",
                                    bericht.getId()));
                          }
                          final BerichtWorkUnit workUnit = new BerichtWorkUnit(bericht);

                          if (enablePipeline) {
                            List<TableData> tableData = handler.transformToTableData(bericht);
                            if (tableData == null) {
                              // Exception during transform
                              // updateProcessingResult() is
                              // aangeroepen, geen
                              // updateResultPipeline
                              continue;
                            }
                            workUnit.setTableData(tableData);
                            workUnit.setTypeOfWork(BerichtTypeOfWork.PROCESS_DBXML);
                            Split pipelinePut =
                                SimonManager.getStopwatch(
                                        "b3p.rsgb.job." + dateTime + ".pipeline.processdbxml.put")
                                    .start();
                            processDbXmlPipeline.getQueue().put(workUnit);
                            pipelinePut.stop();
                          } else {
                            Split berichtSplit =
                                SimonManager.getStopwatch("b3p.rsgb.job." + dateTime + ".bericht")
                                    .start();
                            handler.handle(bericht, null, true);
                            berichtSplit.stop();
                          }
                          lastJid.setValue(rs.getInt("jid"));
                        } catch (Exception e1) {
                          return e1;
                        }
                        processed.increment();
                      }
                      processResultSet.stop();
                      return null;
                    });

        offset += processed.intValue();

        // If handler threw exception processing row, rethrow it
        if (e != null) {
          throw e;
        }

      } while (processed.intValue() > 0 && (offset < total));
      if (offset < total) {
        log.warn(String.format("Minder berichten verwerkt (%d) dan verwacht (%d)!", offset, total));
      }
      // als succesvol beeindigd, dan verwijderen, anders herstart mogelijk maken
      removeJob();
    } catch (Exception t) {
      abort = true;
      throw t;
    } finally {
      if (enablePipeline) {
        if (abort) {
          // Let threads stop by themselves, don't join
          processDbXmlPipeline.setAbortFlag();
        } else {
          processDbXmlPipeline.stopWhenQueueEmpty();
          try {
            processDbXmlPipeline.join();
          } catch (InterruptedException e) {
          }
        }
      }
    }
    jobSplit.stop();
    split.stop();
  }

  public void updateBerichtenDbXml(List<Bericht> berichten, RsgbTransformer transformer)
      throws SAXException, IOException, TransformerException {
    for (Bericht ber : berichten) {
      Split split = SimonManager.getStopwatch("b3p.staging.bericht.dbxml.transform").start();
      String dbxml = transformer.transformToDbXml(ber);
      // HACK vanwege Oracle 8000 karakters bug, zie brmo wiki:
      // https://github.com/B3Partners/brmo/wiki/Oracle-8000-bytes-bug
      if (geomToJdbc instanceof OracleJdbcConverter && dbxml != null && dbxml.length() == 8000) {
        log.debug("DB XML is 8000 bytes, er wordt een spatie aangeplakt.");
        dbxml += " ";
        log.debug("DB XML is nu " + dbxml.length() + " bytes.");
      }

      ber.setDbXml(dbxml);
      split.stop();
    }
  }

  private PreparedStatement getOldBerichtStatement;
  private PreparedStatement getOldBerichtStatementById;

  public Bericht getOldBericht(Bericht nieuwBericht, StringBuilder loadLog) throws SQLException {
    return getOldBericht(nieuwBericht.getObjectRef(), loadLog);
  }

  public Bericht getOldBericht(String objectRef, StringBuilder loadLog) throws SQLException {
    Split split = SimonManager.getStopwatch("b3p.staging.bericht.getold").start();

    Bericht bericht = null;
    ResultSetHandler<List<Bericht>> h =
        new BeanListHandler<>(Bericht.class, new StagingRowHandler());

    if (getOldBerichtStatement == null) {
      String sql =
          "SELECT id, object_ref, datum, volgordenummer, soort, status, job_id, status_datum FROM "
              + BrmoFramework.BERICHT_TABLE
              + " WHERE"
              + " object_ref = ?"
              + " AND status in ('RSGB_OK', 'ARCHIVE')"
              + " ORDER BY datum desc, volgordenummer desc";
      sql = geomToJdbc.buildPaginationSql(sql, 0, 1);

      getOldBerichtStatement = getConnection().prepareStatement(sql);
    } else {
      getOldBerichtStatement.clearParameters();
    }
    getOldBerichtStatement.setString(1, objectRef);

    ResultSet rs = getOldBerichtStatement.executeQuery();
    List<Bericht> list = h.handle(rs);
    rs.close();

    if (!list.isEmpty()) {
      loadLog.append("Vorig bericht gevonden:\n");
      for (Bericht b : list) {
        if ((Bericht.STATUS.RSGB_OK.equals(b.getStatus())
                || Bericht.STATUS.ARCHIVE.equals(b.getStatus()))
            && bericht == null) {
          loadLog.append("Meest recent bericht gevonden: ").append(b).append("\n");
          bericht = b;
        } else {
          loadLog.append("Niet geschikt bericht: ").append(b).append("\n");
        }
      }
    }

    if (bericht != null) {
      // bericht nu wel vullen met alle kolommen
      if (getOldBerichtStatementById == null) {
        String sql = "SELECT * FROM " + BrmoFramework.BERICHT_TABLE + " WHERE id = ?";

        getOldBerichtStatementById = getConnection().prepareStatement(sql);
      } else {
        getOldBerichtStatementById.clearParameters();
      }
      getOldBerichtStatementById.setLong(1, bericht.getId());

      ResultSet rs2 = getOldBerichtStatementById.executeQuery();
      List<Bericht> list2 = h.handle(rs2);
      rs2.close();

      if (!list2.isEmpty()) {
        bericht = list2.get(0);
      }
    }

    split.stop();
    return bericht;
  }

  private PreparedStatement getPreviousBerichtStatement;

  public Bericht getPreviousBericht(Bericht ber, StringBuilder loadLog) throws SQLException {
    return getPreviousBericht(ber.getObjectRef(), ber.getDatum(), ber.getId(), loadLog);
  }

  /** Gets the previous bericht (not the first). */
  public Bericht getPreviousBericht(
      String objectRef, Date datum, Long currentBerichtId, StringBuilder loadLog)
      throws SQLException {
    Split split = SimonManager.getStopwatch("b3p.staging.bericht.getprevious").start();

    Bericht bericht = null;
    ResultSetHandler<List<Bericht>> h =
        new BeanListHandler<>(Bericht.class, new StagingRowHandler());

    if (getPreviousBerichtStatement == null) {
      String sql =
          "SELECT id, object_ref, datum, volgordenummer, soort, status, job_id, status_datum FROM "
              + BrmoFramework.BERICHT_TABLE
              + " WHERE"
              + " object_ref = ? and datum <= ? and id <> ?"
              + " ORDER BY datum asc, volgordenummer desc";
      sql = geomToJdbc.buildPaginationSql(sql, 0, 1);

      getPreviousBerichtStatement = getConnection().prepareStatement(sql);
    } else {
      getPreviousBerichtStatement.clearParameters();
    }
    getPreviousBerichtStatement.setString(1, objectRef);
    getPreviousBerichtStatement.setTimestamp(2, new java.sql.Timestamp(datum.getTime()));
    getPreviousBerichtStatement.setLong(3, currentBerichtId);

    ResultSet rs = getPreviousBerichtStatement.executeQuery();
    List<Bericht> list = h.handle(rs);
    rs.close();

    if (!list.isEmpty()) {
      loadLog.append("Vorig bericht gevonden:\n");
      for (Bericht b : list) {
        if (bericht == null) {
          loadLog.append("Meest recent bericht gevonden: ").append(b).append("\n");
          bericht = b;
        } else {
          loadLog.append("Niet geschikt bericht: ").append(b).append("\n");
        }
      }
    }

    if (bericht != null) {
      // bericht nu wel vullen met alle kolommen
      if (getOldBerichtStatementById == null) {
        String sql = "SELECT * FROM " + BrmoFramework.BERICHT_TABLE + " WHERE id = ?";

        getOldBerichtStatementById = getConnection().prepareStatement(sql);
      } else {
        getOldBerichtStatementById.clearParameters();
      }
      getOldBerichtStatementById.setLong(1, bericht.getId());

      ResultSet rs2 = getOldBerichtStatementById.executeQuery();
      List<Bericht> list2 = h.handle(rs2);
      rs2.close();

      if (!list2.isEmpty()) {
        bericht = list2.get(0);
      }
    }

    split.stop();
    return bericht;
  }

  /**
   * Update (overschrijft) een bericht in job tabel. (object_ref, datum, volgordenummer, soort,
   * opmerking, status, status_datum, br_xml, br_orgineel_xml, db_xml, xsl_version)
   *
   * @param b bij te werken bericht
   * @throws SQLException if any
   */
  public void updateBericht(Bericht b) throws SQLException {
    Split split = SimonManager.getStopwatch("b3p.staging.bericht.update").start();
    String brXML = b.getBrXml();
    // HACK vanwege Oracle 8000 karakters bug, zie brmo wiki:
    // https://github.com/B3Partners/brmo/wiki/Oracle-8000-bytes-bug
    if (geomToJdbc instanceof OracleJdbcConverter && brXML != null && brXML.length() == 8000) {
      log.debug("BR XML is 8000 bytes, er wordt een spatie aangeplakt.");
      brXML += " ";
      log.debug("BR XML is nu " + brXML.length() + " bytes.");
    }

    new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .update(
            getConnection(),
            "UPDATE "
                + BrmoFramework.BERICHT_TABLE
                + " set "
                + "object_ref = ?, "
                + "datum = ?, "
                + "volgordenummer = ?, "
                + "soort = ?, "
                + "opmerking = ?, "
                + "status = ?, "
                + "status_datum = ?, "
                + "br_xml = ?, "
                + "br_orgineel_xml = ?, "
                + "db_xml = ?, "
                + "xsl_version = ? "
                + "WHERE id = ?",
            b.getObjectRef(),
            new Timestamp(b.getDatum().getTime()),
            b.getVolgordeNummer(),
            b.getSoort(),
            b.getOpmerking(),
            b.getStatus().toString(),
            new Timestamp(b.getStatusDatum().getTime()),
            brXML,
            b.getBrOrgineelXml(),
            b.getDbXml(),
            b.getXslVersion(),
            b.getId());
    split.stop();
  }

  /**
   * Update een bericht in job tabel met processing status. (opmerking, status, status_datum,
   * db_xml)
   *
   * @param b updatebericht
   * @throws SQLException als de update mislukt
   */
  public void updateBerichtProcessing(Bericht b) throws SQLException {
    Split split = SimonManager.getStopwatch("b3p.staging.bericht.updateprocessing").start();

    new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .update(
            getConnection(),
            "UPDATE "
                + BrmoFramework.BERICHT_TABLE
                + " set "
                + "opmerking = ?, "
                + "status = ?, "
                + "status_datum = ?, "
                + "db_xml = ? "
                + " WHERE id = ?",
            b.getOpmerking(),
            b.getStatus().toString(),
            new Timestamp(b.getStatusDatum().getTime()),
            b.getDbXml(),
            b.getId());
    split.stop();
  }

  public void deleteByLaadProcesId(Long id) throws SQLException {
    new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .update(
            getConnection(),
            "DELETE FROM " + BrmoFramework.BERICHT_TABLE + " WHERE laadprocesid = ?",
            id);
    new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .update(
            getConnection(), "DELETE FROM " + BrmoFramework.LAADPROCES_TABEL + " WHERE id = ?", id);
  }

  public List<LaadProces> getLaadProcessen() throws SQLException {
    List<LaadProces> list;
    ResultSetHandler<List<LaadProces>> h =
        new BeanListHandler<>(LaadProces.class, new StagingRowHandler());
    list =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .query(getConnection(), "select * from " + BrmoFramework.LAADPROCES_TABEL, h);
    return list;
  }

  public List<Bericht> getBerichten() throws SQLException {
    List<Bericht> berichten;
    ResultSetHandler<List<Bericht>> h =
        new BeanListHandler<>(Bericht.class, new StagingRowHandler());
    berichten =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .query(getConnection(), "select * from " + BrmoFramework.BERICHT_TABLE, h);
    return berichten;
  }

  /**
   * Laadt het bestand uit de stream in de database.
   *
   * @param stream input
   * @param type type registratie, bijv. {@value BrmoFramework#BR_BRK2}
   * @param fileName naam van het bestand (ter identificatie)
   * @param d bestandsdatum
   * @param listener progress listener
   * @throws Exception if any
   * @deprecated gebruik de variant die een automatischProcesId als argument heeft
   * @see #loadBr(InputStream, String, String, Date, ProgressUpdateListener, Long)
   */
  @Deprecated
  public void loadBr(
      InputStream stream, String type, String fileName, Date d, ProgressUpdateListener listener)
      throws Exception {
    this.loadBr(stream, type, fileName, d, listener, null);
  }

  /**
   * Laadt het bestand uit de stream in de database.
   *
   * @param stream input
   * @param type type registratie, bijv. {@value BrmoFramework#BR_BRK2}
   * @param fileName naam van het bestand (ter identificatie)
   * @param d bestandsdatum
   * @param automatischProces id van het automatisch proces
   * @param listener progress listener
   * @throws Exception if any
   */
  public void loadBr(
      InputStream stream,
      String type,
      String fileName,
      Date d,
      ProgressUpdateListener listener,
      Long automatischProces)
      throws Exception {

    CountingInputStream cis = new CountingInputStream(stream);

    BrmoXMLReader brmoXMLReader;
    if (type.equals(BrmoFramework.BR_BRK2)) {
      brmoXMLReader = new Brk2SnapshotXMLReader(cis);
    } else if (type.equals(BrmoFramework.BR_NHR)) {
      brmoXMLReader = new NhrXMLReader(cis);
    } else if (type.equals(BrmoFramework.BR_BRP)) {
      brmoXMLReader = new BRPXMLReader(cis, d, this);
    } else if (type.equals(BrmoFramework.BR_GBAV)) {
      brmoXMLReader = new GbavXMLReader(cis);
    } else if (type.equals(BrmoFramework.BR_WOZ)) {
      brmoXMLReader = new WozXMLReader(cis, d, this);
    } else {
      throw new UnsupportedOperationException("Ongeldige basisregistratie: " + type);
    }

    if (brmoXMLReader.getBestandsDatum() == null) {
      throw new BrmoException("Header van bestand bevat geen datum, verkeerd formaat?");
    }

    LaadProces lp = new LaadProces();
    lp.setBestandNaam(fileName);
    lp.setBestandDatum(brmoXMLReader.getBestandsDatum());
    lp.setSoort(type);
    lp.setGebied(brmoXMLReader.getGebied());
    lp.setStatus(LaadProces.STATUS.STAGING_OK);
    lp.setStatusDatum(new Date());
    lp.setAutomatischProcesId(automatischProces);

    if (laadProcesExists(lp)) {
      throw new BrmoDuplicaatLaadprocesException("Laadproces al gerund voor bestand " + fileName);
    }
    lp = writeLaadProces(lp);

    if (!brmoXMLReader.hasNext()) {
      updateLaadProcesStatus(
          lp, LaadProces.STATUS.STAGING_OK, "Leeg bestand, geen berichten gevonden in " + fileName);
      throw new BrmoLeegBestandException("Leeg bestand, geen berichten gevonden in " + fileName);
    }

    boolean isBerichtGeschreven = false;
    int berichten = 0;
    int foutBerichten = 0;
    String lastErrorMessage = null;

    while (brmoXMLReader.hasNext()) {
      Bericht b;
      try {
        b = brmoXMLReader.next();
        b.setLaadProcesId(lp.getId());
        b.setStatus(Bericht.STATUS.STAGING_OK);
        b.setStatusDatum(new Date());
        b.setSoort(type);

        if (StringUtils.isEmpty(b.getObjectRef())) {
          // geen object_ref kunnen vaststellen; dan ook niet transformeren,
          // bijvoorbeeld bij WOZ
          b.setStatus(Bericht.STATUS.STAGING_NOK);
          b.setOpmerking(
              "Er kon geen object_ref bepaald worden uit de natuurlijke sleutel van het bericht.");
        }

        if (b.getDatum() == null) {
          throw new BrmoException("Datum bericht is null");
        }
        log.debug(b);

        Bericht existingBericht = getExistingBericht(b);
        // TODO BRK2 bepalen of dit nog nodig is voor BRK2
        if (type.equals(BrmoFramework.BR_BRK2) && !isBerichtGeschreven) {
          // haal alleen voor eerste
          Brk2Bericht brk2Bericht = (Brk2Bericht) b;
          lp.setBestandNaamHersteld(
              brk2Bericht.getRestoredFileName(lp.getBestandDatum(), b.getVolgordeNummer()));
          updateLaadProcesBestandNaamHersteld(lp);
        }

        if (existingBericht == null) {
          writeBericht(b);
          isBerichtGeschreven = true;
        } else if (existingBericht.getStatus().equals(Bericht.STATUS.STAGING_OK)) {
          // als bericht nog niet getransformeerd is, dan overschrijven.
          b.setId(existingBericht.getId());
          this.updateBericht(b);
        }
        if (listener != null) {
          listener.progress(cis.getByteCount());
        }
        berichten++;
      } catch (Exception e) {
        lastErrorMessage =
            String.format(
                "Laden bericht uit %s mislukt vanwege: %s", fileName, e.getLocalizedMessage());
        log.error(lastErrorMessage);
        log.trace(lastErrorMessage, e);
        if (listener != null) {
          listener.exception(e);
        }
        foutBerichten++;
      }
    }
    if (listener != null) {
      listener.total(berichten);
    }
    if (foutBerichten > 0) {
      String opmerking =
          "Laden van "
              + foutBerichten
              + " bericht(en) mislukt, laatste melding: "
              + lastErrorMessage
              + ", zie logs voor meer info.";
      this.updateLaadProcesStatus(lp, LaadProces.STATUS.STAGING_NOK, opmerking);
    } else if (!isBerichtGeschreven) {
      String opmerking = "Dit bestand is waarschijnlijk al eerder geladen.";
      this.updateLaadProcesStatus(lp, LaadProces.STATUS.STAGING_DUPLICAAT, opmerking);
    }
  }

  public void writeBericht(Bericht b) throws SQLException {
    String brXML = b.getBrXml();
    // HACK vanwege Oracle 8000 karakters bug, zie brmo wiki:
    // https://github.com/B3Partners/brmo/wiki/Oracle-8000-bytes-bug
    if (geomToJdbc instanceof OracleJdbcConverter && brXML != null && brXML.length() == 8000) {
      log.debug("BR XML is 8000 bytes, er wordt een spatie aangeplakt.");
      brXML += " ";
      log.debug("BR XML is nu " + brXML.length() + " bytes.");
    }
    Object berId =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .insert(
                getConnection(),
                "INSERT INTO "
                    + BrmoFramework.BERICHT_TABLE
                    + " (laadprocesid, "
                    + "object_ref, "
                    + "datum, "
                    + "volgordenummer, "
                    + "soort, "
                    + "opmerking, "
                    + "status, "
                    + "status_datum, "
                    + "br_xml, "
                    + "br_orgineel_xml, "
                    + "db_xml, "
                    + "xsl_version) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                new ScalarHandler<>(),
                b.getLaadProcesId(),
                b.getObjectRef(),
                new Timestamp(b.getDatum().getTime()),
                b.getVolgordeNummer(),
                b.getSoort(),
                b.getOpmerking(),
                b.getStatus().toString(),
                new Timestamp(b.getStatusDatum().getTime()),
                brXML,
                b.getBrOrgineelXml(),
                b.getDbXml(),
                b.getXslVersion());
    log.trace("opgeslagen bericht heeft (row) id: " + berId);
  }

  /** Bepaal aan de hand van bestandsnaam en bestandsdatum van het laadproces of dit al bestaat. */
  private boolean laadProcesExists(LaadProces lp) throws SQLException {
    Object o =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .query(
                getConnection(),
                "select 1 from laadproces where bestand_naam = ? and" + " bestand_datum = ?",
                new ScalarHandler<>(),
                lp.getBestandNaam(),
                new Timestamp(lp.getBestandDatum().getTime()));

    return o != null;
  }

  private LaadProces writeLaadProces(LaadProces lp) throws SQLException {
    log.trace("opslaan van laadproces: " + lp);
    if (lp == null) {
      return null;
    }

    final String sql =
        "INSERT INTO "
            + BrmoFramework.LAADPROCES_TABEL
            + "(bestand_naam, "
            + "bestand_datum, soort, gebied, opmerking, status, "
            + "status_datum, contact_email, automatisch_proces "
            // nieuwe kolommen in 2.0.0
            + ", afgifteid, afgiftereferentie, artikelnummer, beschikbaar_tot, bestandsreferentie, "
            + "contractafgiftenummer, contractnummer, klantafgiftenummer, bestand_naam_hersteld"
            + ") VALUES (?,?,?,?,?,?,?,?,?"
            + ",?,?,?,?,?,?,?,?,?"
            + ")";

    // Oracle geeft een ROWID terug als "generated key" en niet de PK-kolom "ID" die we willen
    // hebben, daarom zelf doen
    // zie ook: https://issues.apache.org/jira/browse/DBUTILS-54
    QueryRunner queryRunner = new QueryRunner(geomToJdbc.isPmdKnownBroken());
    try (
    // PG: Caused by: org.postgresql.util.PSQLException: ERROR: column "ID" does not exist
    // maar lowercase werkt ook met oracle
    PreparedStatement stmt = getConnection().prepareStatement(sql, new String[] {"id"})
    // door een bug in de PG driver geen kolom index gebruiken, zie
    // https://github.com/pgjdbc/pgjdbc/issues/1661
    // PreparedStatement stmt = getConnection().prepareStatement(sql, new int[]{1});
    ) {
      queryRunner.fillStatement(
          stmt,
          lp.getBestandNaam(),
          new Timestamp(lp.getBestandDatum().getTime()),
          lp.getSoort(),
          lp.getGebied(),
          lp.getOpmerking(),
          lp.getStatus().toString(),
          new Timestamp(lp.getStatusDatum().getTime()),
          lp.getContactEmail(),
          lp.getAutomatischProcesId(),
          // nieuwe kolommen in 2.0.0
          lp.getAfgifteid(),
          lp.getAfgiftereferentie(),
          lp.getArtikelnummer(),
          (lp.getBeschikbaar_tot() == null)
              ? null
              : new Timestamp(lp.getBeschikbaar_tot().getTime()),
          lp.getBestandsreferentie(),
          lp.getContractafgiftenummer(),
          lp.getContractnummer(),
          lp.getKlantafgiftenummer(),
          lp.getBestandNaamHersteld());
      stmt.executeUpdate();
      ResultSetHandler<Number> rsh = new ScalarHandler<>();
      Number lpId = rsh.handle(stmt.getGeneratedKeys());
      log.trace("opgeslagen laadproces heeft id: " + lpId);
      return this.getLaadProcesById(lpId.longValue());
    }
  }

  public void updateLaadProcesStatus(LaadProces lp, LaadProces.STATUS status, String opmerking)
      throws SQLException {
    log.trace("update van laadproces status: " + lp);
    new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .update(
            getConnection(),
            "update "
                + BrmoFramework.LAADPROCES_TABEL
                + " set status = ?, opmerking = ?, status_datum = ? where id = ?",
            status.toString(),
            opmerking,
            new Timestamp(new Date().getTime()),
            lp.getId());
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
   * @throws SQLException if any
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
      throws SQLException {
    new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .update(
            getConnection(),
            "update "
                + BrmoFramework.LAADPROCES_TABEL
                + " set "
                + "afgifteid = ?,"
                + "afgiftereferentie = ?,"
                + "artikelnummer = ?,"
                + "beschikbaar_tot = ?,"
                + "bestandsreferentie = ?,"
                + "contractafgiftenummer = ?,"
                + "contractnummer = ?,"
                + "klantafgiftenummer = ?"
                + "where id = ?",
            afgifteid,
            afgiftereferentie,
            artikelnummer,
            new Timestamp(beschikbaar_tot.getTime()),
            bestandsreferentie,
            contractafgiftenummer,
            contractnummer,
            klantafgiftenummer,
            lpId);
  }

  public void updateLaadProcesBestandNaamHersteld(LaadProces lp) throws SQLException {
    new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .update(
            getConnection(),
            "update "
                + BrmoFramework.LAADPROCES_TABEL
                + " set bestand_naam_hersteld = ? where id = ?",
            lp.getBestandNaamHersteld(),
            lp.getId());
  }

  public void emptyStagingDb() throws SQLException {
    new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .update(getConnection(), "DELETE FROM " + BrmoFramework.BERICHT_TABLE);
    new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .update(getConnection(), "DELETE FROM " + BrmoFramework.LAADPROCES_TABEL);
  }

  public List<Bericht> getBerichten(
      int page,
      int start,
      int limit,
      String sort,
      String dir,
      String filterSoort,
      String filterStatus)
      throws SQLException {

    List<String> params = new ArrayList<>();
    if (sort == null || sort.trim().isEmpty()) {
      sort = "id";
    }
    if (dir == null || dir.trim().isEmpty()) {
      sort = "asc";
    }
    String sql =
        "SELECT * FROM "
            + BrmoFramework.BERICHT_TABLE
            + buildFilterSql(page, sort, dir, filterSoort, filterStatus, params);

    sql = geomToJdbc.buildPaginationSql(sql, start, limit);

    return new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .query(
            getConnection(),
            sql,
            new BeanListHandler<>(Bericht.class, new StagingRowHandler()),
            params.toArray());
  }

  public long getCountBerichten(String filterSoort, String filterStatus) throws SQLException {

    List<String> params = new ArrayList<>();
    String filter = buildFilterSql(0, null, null, filterSoort, filterStatus, params);

    String sql;
    // gebruik estimate voor postgresql indien geen filter
    if (StringUtils.isBlank(filter) && geomToJdbc instanceof PostgisJdbcConverter) {
      sql =
          "SELECT reltuples::BIGINT AS estimate FROM pg_class WHERE relname= '"
              + BrmoFramework.BERICHT_TABLE
              + "'";
    } else {
      sql = "SELECT count(*) FROM " + BrmoFramework.BERICHT_TABLE + filter;
    }

    ResultSet rs = null;
    PreparedStatement pstmt = null;
    try {
      pstmt = getConnection().prepareStatement(sql);
      try {
        pstmt.setQueryTimeout(300); // seconds to wait
      } catch (Exception e) {
        log.warn("Driver does not support setQueryTimeout, please update driver.");
      }
      if (!params.isEmpty()) {
        for (int i = 0; i < params.size(); i++) {
          if (params.get(i) != null) {
            pstmt.setObject(i + 1, params.get(i));
          } else {
            pstmt.setNull(i + 1, Types.VARCHAR);
          }
        }
      }
      rs = pstmt.executeQuery();
      if (rs.next()) {
        Object o = rs.getObject(1);
        if (o instanceof BigDecimal) {
          return ((BigDecimal) o).longValue();
        } else if (o instanceof Integer) {
          return ((Integer) o).longValue();
        }
        return (Long) o;
      } else {
        return -1;
      }
    } catch (Exception e) {
      return -1;
    } finally {
      try {
        if (rs != null && !rs.isClosed()) {
          rs.close();
        }
      } catch (SQLException e) {
        // ignore
      }
      try {
        if (pstmt != null && !pstmt.isClosed()) {
          pstmt.close();
        }
      } catch (SQLException e) {
        // ignore
      }
    }
  }

  private String buildFilterSql(
      int page,
      String sort,
      String dir,
      String filterSoort,
      String filterStatus,
      List<String> params) {

    StringBuilder builder = new StringBuilder();

    List<String> conjunctions = new ArrayList<>();

    if (!StringUtils.isBlank(filterSoort)) {
      String[] soorten = filterSoort.split(",");
      params.addAll(Arrays.asList(soorten));
      conjunctions.add("soort in (" + StringUtils.repeat("?", ", ", soorten.length) + ")");
    }

    if (!StringUtils.isBlank(filterStatus)) {
      String[] statussen = filterStatus.split(",");
      params.addAll(Arrays.asList(statussen));
      conjunctions.add("status in (" + StringUtils.repeat("?", ", ", statussen.length) + ")");
    }

    // build where part
    if (!conjunctions.isEmpty()) {
      builder.append(" where ");
      builder.append(StringUtils.join(conjunctions.toArray(), " and "));
    }

    // build order by part
    if (sort != null && dir != null) {
      builder.append(" ORDER BY ");
      builder.append(sort);
      builder.append(" ");
      builder.append(dir);
    }

    return builder.toString();
  }

  public long getCountLaadProces(String filterSoort, String filterStatus) throws SQLException {

    List<String> params = new ArrayList<>();
    String sql =
        "SELECT count(*) FROM "
            + BrmoFramework.LAADPROCES_TABEL
            + buildFilterSql(0, null, null, filterSoort, filterStatus, params);

    Object o =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .query(getConnection(), sql, new ScalarHandler<>(), params.toArray());
    if (o instanceof BigDecimal) {
      return ((BigDecimal) o).longValue();
    } else if (o instanceof Integer) {
      return ((Integer) o).longValue();
    }
    return (Long) o;
  }

  public List<LaadProces> getLaadprocessen(
      int page,
      int start,
      int limit,
      String sort,
      String dir,
      String filterSoort,
      String filterStatus)
      throws SQLException {

    List<String> params = new ArrayList<>();
    if (sort == null || sort.trim().isEmpty()) {
      sort = "id";
    }
    if (dir == null || dir.trim().isEmpty()) {
      sort = "asc";
    }
    String sql =
        "SELECT * FROM "
            + BrmoFramework.LAADPROCES_TABEL
            + buildFilterSql(page, sort, dir, filterSoort, filterStatus, params);

    sql = geomToJdbc.buildPaginationSql(sql, start, limit);

    return new QueryRunner(geomToJdbc.isPmdKnownBroken())
        .query(
            getConnection(),
            sql,
            new BeanListHandler<>(LaadProces.class, new StagingRowHandler()),
            params.toArray());
  }

  public Long[] getLaadProcessenIds(
      String sort, String dir, String filterSoort, String filterStatus) throws SQLException {
    List<String> params = new ArrayList<>();
    if (sort == null || sort.trim().isEmpty()) {
      sort = "id";
    }
    if (dir == null || dir.trim().isEmpty()) {
      dir = "asc";
    }
    String sql =
        "SELECT ID FROM "
            + BrmoFramework.LAADPROCES_TABEL
            + buildFilterSql(-1, sort, dir, filterSoort, filterStatus, params);
    List<Long> ids =
        new QueryRunner(geomToJdbc.isPmdKnownBroken())
            .query(getConnection(), sql, new LongColumnListHandler("id"), params.toArray());
    return ids.toArray(new Long[ids.size()]);
  }

  /**
   * @param batchCapacity the batchCapacity to set
   */
  public void setBatchCapacity(Integer batchCapacity) {
    this.batchCapacity = batchCapacity;
  }

  public void setLimitStandBerichtenToTransform(Integer limitStandBerichtenToTransform) {
    this.limitStandBerichtenToTransform = limitStandBerichtenToTransform;
  }
}
