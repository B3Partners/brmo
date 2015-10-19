package nl.b3p.brmo.loader;

import nl.b3p.brmo.loader.pipeline.ProcessDbXmlPipeline;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import javax.xml.transform.TransformerException;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.BerichtenSorter;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.pipeline.BerichtTypeOfWork;
import nl.b3p.brmo.loader.pipeline.BerichtWorkUnit;
import nl.b3p.brmo.loader.pipeline.UpdateResultPipeline;
import nl.b3p.brmo.loader.util.BrmoDuplicaatLaadprocesException;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import nl.b3p.brmo.loader.util.RsgbTransformer;
import nl.b3p.brmo.loader.util.StagingRowHandler;
import nl.b3p.brmo.loader.util.TableData;
import nl.b3p.brmo.loader.xml.BagXMLReader;
import nl.b3p.brmo.loader.xml.BrkSnapshotXMLReader;
import nl.b3p.brmo.loader.xml.BrmoXMLReader;
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
 *
 * @author Boy de Wit
 */
public class StagingProxy {

    private static final Log log = LogFactory.getLog(StagingProxy.class);

    private Connection connStaging = null;
    private DataSource dataSourceStaging =  null;

    private String dbType;

    public StagingProxy(DataSource dataSourceStaging) throws SQLException, BrmoException {
        this.dataSourceStaging = dataSourceStaging;
        setDbType();
    }

    public void closeStagingProxy() {
        if(getOldBerichtStatement != null) {
            DbUtils.closeQuietly(getOldBerichtStatement);
        }
        DbUtils.closeQuietly(connStaging);
    }

    private Connection getConnection() throws SQLException {
        if (connStaging==null || connStaging.isClosed()) {
            connStaging = dataSourceStaging.getConnection();
        }
        return connStaging;
    }

    private void setDbType() throws SQLException, BrmoException {
        String productName = getConnection().getMetaData().getDatabaseProductName();
        if (productName.contains("PostgreSQL")) {
            dbType = "postgres";
        } else if (productName.contains("Oracle")) {
            dbType = "oracle";
        } else {
            throw new BrmoException("Database niet ondersteund: " + productName);
        }
    }

    public LaadProces getLaadProcesById(Long id) throws SQLException {
        List<LaadProces> processen;

        ResultSetHandler<List<LaadProces>> h
                = new BeanListHandler(LaadProces.class, new StagingRowHandler());

        processen = new QueryRunner().query(getConnection(),
                "SELECT * FROM " + BrmoFramework.LAADPROCES_TABEL + " WHERE id = ?", h, id);

        if (processen != null && processen.size() == 1) {
            return processen.get(0);
        }

        return null;
    }

    public Bericht getBerichtById(Long id) throws SQLException {
        List<Bericht> berichten;
        ResultSetHandler<List<Bericht>> h
                = new BeanListHandler(Bericht.class, new StagingRowHandler());

        berichten = new QueryRunner().query(getConnection(),
                "SELECT * FROM " + BrmoFramework.BERICHT_TABLE + " WHERE id = ?", h, id);

        if (berichten != null && berichten.size() == 1) {
            return berichten.get(0);
        }

        return null;
    }

    public LaadProces getLaadProcesByFileName(String name) throws SQLException {
        List<LaadProces> processen;
        ResultSetHandler<List<LaadProces>> h
                = new BeanListHandler(LaadProces.class, new StagingRowHandler());

        processen = new QueryRunner().query(getConnection(),
                "SELECT * FROM " + BrmoFramework.LAADPROCES_TABEL + " WHERE bestand_naam = ?", h, name);

        if (processen != null && processen.size() == 1) {
            return processen.get(0);
        }

        return null;
    }

    private LaadProces getLaadProcesByNaturalKey(String name, long date) throws SQLException {
        List<LaadProces> processen;
        ResultSetHandler<List<LaadProces>> h
                = new BeanListHandler(LaadProces.class, new StagingRowHandler());

        processen = new QueryRunner().query(getConnection(),
                "SELECT * FROM " + BrmoFramework.LAADPROCES_TABEL + " WHERE bestand_naam = ?"
                        + " AND bestand_datum = ?", h,
                        name,
                        new Timestamp(date));

        if (processen != null && processen.size() == 1) {
            return processen.get(0);
        }

        return null;
    }

    private Bericht getBerichtByNaturalKey(String name, long date) throws SQLException {
        List<Bericht> processen;
        ResultSetHandler<List<Bericht>> h
                = new BeanListHandler(Bericht.class, new StagingRowHandler());

        processen = new QueryRunner().query(getConnection(),
                "SELECT * FROM " + BrmoFramework.BERICHT_TABLE + " WHERE bestand_naam = ?"
                        + " AND bestand_datum = ?", h,
                        name,
                        new Timestamp(date));

        if (processen != null && processen.size() == 1) {
            return processen.get(0);
        }

        return null;
    }

    public void setBerichtenJobByStatus(Bericht.STATUS status, String jobId) throws SQLException {
        new QueryRunner().update(getConnection(), "update " + BrmoFramework.BERICHT_TABLE + " set status = ?, job_id = ? where status = ?",
                Bericht.STATUS.RSGB_WAITING.toString(), jobId, status.toString());
    }

    public void setBerichtenJobForUpdate(String jobId, String soort) throws SQLException {
        new QueryRunner().update(getConnection(), "update " + BrmoFramework.BERICHT_TABLE + " set job_id = ? where status = ? and soort = ?",
                jobId, Bericht.STATUS.RSGB_OK.toString(), soort);
    }

    public void setBerichtenJobByIds(long[] ids, String jobId) throws SQLException {
        StringBuilder q = new StringBuilder("update " + BrmoFramework.BERICHT_TABLE + " set status = ?, job_id = ? where id in (");
        for (int i = 0; i < ids.length; i++) {
            if(i != 0) {
                q.append(",");
            }
            q.append(ids[i]);
        }
        q.append(")");
        new QueryRunner().update(getConnection(), q.toString(), Bericht.STATUS.RSGB_WAITING.toString(), jobId);
    }

    public void setBerichtenJobByLaadprocessen(long[] laadprocesIds, String jobId) throws SQLException {

        StringBuilder q = new StringBuilder("update " + BrmoFramework.BERICHT_TABLE + " set status = ?, job_id = ? where laadprocesid in (");
        for (int i = 0; i < laadprocesIds.length; i++) {
            if(i != 0) {
                q.append(",");
            }
            q.append(laadprocesIds[i]);
        }
        q.append(") and status = ?");
        new QueryRunner().update(getConnection(), q.toString(), Bericht.STATUS.RSGB_WAITING.toString(), jobId, Bericht.STATUS.STAGING_OK.toString());
    }

    public long getBerichtenCountByJob(String jobId) throws SQLException {
        Object o = new QueryRunner().query(getConnection(),
                "select count(*) from " + BrmoFramework.BERICHT_TABLE + " where job_id = ?",
                new ScalarHandler(),
                jobId);

        return o instanceof BigDecimal ? ((BigDecimal)o).longValue() : (Long)o;
    }

    public void handleBerichtenByJob(final String jobId, long total, final BerichtenHandler handler, final boolean enablePipeline, int transformPipelineCapacity, final boolean pmdKnownBroken) throws Exception {
        Split split = SimonManager.getStopwatch("b3p.rsgb.job").start();
        final String dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        Split jobSplit = SimonManager.getStopwatch("b3p.rsgb.job." + dateTime).start();
        ((RsgbProxy)handler).setSimonNamePrefix("b3p.rsgb.job." + dateTime + ".");
        final RowProcessor processor = new StagingRowHandler();

        final ProcessDbXmlPipeline processDbXmlPipeline = new ProcessDbXmlPipeline(handler, transformPipelineCapacity, "b3p.rsgb.job." + dateTime + ".pipeline");
        final UpdateResultPipeline updateResultPipeline = new UpdateResultPipeline(handler, transformPipelineCapacity, "b3p.rsgb.job." + dateTime + ".pipeline");
        if(enablePipeline) {
            processDbXmlPipeline.start();
            updateResultPipeline.start();
        }
        int offset = 0;
        int batch = 250;
        final MutableInt processed = new MutableInt(0);
        boolean abort = false;
        try {
            do {
                log.debug(String.format("Ophalen berichten batch voor job %s, offset %d, limit %d", jobId, offset, batch));
                String sql = "select * from " + BrmoFramework.BERICHT_TABLE + " where job_id = ? order by " + BerichtenSorter.SQL_ORDER_BY;

                if(dbType.contains("oracle")) {
                    sql = buildPaginationSqlOracle(sql, offset, batch);
                } else {
                    sql = sql + " limit " + batch + " offset " + offset;
                }
                log.debug("SQL voor ophalen berichten batch: " + sql);

                processed.setValue(0);
                final Split getBerichten = SimonManager.getStopwatch("b3p.rsgb.job." + dateTime + ".staging.berichten.getbatch").start();
                Exception e = new QueryRunner(pmdKnownBroken).query(getConnection(), sql, new ResultSetHandler<Exception>() {
                    @Override
                    public Exception handle(ResultSet rs) throws SQLException {
                        getBerichten.stop();
                        final Split processResultSet = SimonManager.getStopwatch("b3p.rsgb.job." + dateTime + ".staging.berichten.processresultset").start();

                        while(rs.next()) {
                            try {
                                Bericht bericht = processor.toBean(rs, Bericht.class);
                                final BerichtWorkUnit workUnit = new BerichtWorkUnit(bericht);

                                handler.updateBerichtProcessing(bericht);

                                if(enablePipeline) {

                                    List<TableData> tableData = handler.transformToTableData(bericht);
                                    if(tableData == null) {
                                        // Exception during transform

                                        // updateProcessingResult() is aangeroepen, geen updateResultPipeline
                                        continue;
                                    }
                                    workUnit.setTableData(tableData);
                                    workUnit.setTypeOfWork(BerichtTypeOfWork.PROCESS_DBXML);
                                    Split pipelinePut = SimonManager.getStopwatch("b3p.rsgb.job." + dateTime + ".pipeline.processdbxml.put").start();
                                    try {
                                        workUnit.setRunAfterWork(new Runnable() {
                                            @Override
                                            public void run() {
                                                // Executed in processDbXmlPipeline thread

                                                workUnit.setTypeOfWork(BerichtTypeOfWork.UPDATE_RESULT);
                                                Split pipelinePut = SimonManager.getStopwatch("b3p.rsgb.job." + dateTime + ".pipeline.updateresult.put").start();
                                                try {
                                                    updateResultPipeline.getQueue().put(workUnit);
                                                } catch(InterruptedException e) {
                                                    log.error("Interrupted", e);
                                                }
                                                pipelinePut.stop();
                                            }
                                        });
                                        processDbXmlPipeline.getQueue().put(workUnit);
                                    } catch(InterruptedException e) {
                                        log.error("Interrupted", e);
                                    }
                                    pipelinePut.stop();
                                } else {
                                    Split berichtSplit = SimonManager.getStopwatch("b3p.rsgb.job." + dateTime + ".bericht").start();
                                    handler.handle(bericht, null, true);
                                    berichtSplit.stop();
                                }
                            } catch(Exception e) {
                                return e;
                            }
                            processed.increment();
                        }
                        processResultSet.stop();
                        return null;
                    }
                }, jobId);

                offset += processed.intValue();

                // If handler threw exception processing row, rethrow it
                if(e != null) {
                    throw e;
                }
            } while(processed.intValue() > 0 && offset < total);
            if(offset < total) {
                log.warn(String.format("Minder berichten verwerkt (%d) dan verwacht (%d)!", offset, total));
            }
        } catch(Exception t) {
            abort = true;
            throw t;
        } finally {
            if(enablePipeline) {
                if(abort) {
                    // Let threads stop by themselves, don't join
                    processDbXmlPipeline.setAbortFlag();
                    updateResultPipeline.setAbortFlag();
                } else {
                    processDbXmlPipeline.stopWhenQueueEmpty();
                    try {
                        processDbXmlPipeline.join();
                    } catch(InterruptedException e) {
                    }
                    updateResultPipeline.stopWhenQueueEmpty();
                    try {
                        updateResultPipeline.join();
                    } catch(InterruptedException e) {
                    }
                }
            }
        }
        jobSplit.stop();
        split.stop();
    }

/*
    public List<Bericht> getBerichtenByStatus(Bericht.STATUS status) throws SQLException {
        List<Bericht> berichten;
        ResultSetHandler<List<Bericht>> hb = new BeanListHandler(Bericht.class, new StagingRowHandler());
        berichten = new QueryRunner().query(getConnection(), "select * from " + BrmoFramework.BERICHT_TABLE + " where status = ?", hb, status.toString());
        Collections.sort(berichten ,new BerichtenSorter());
        return berichten;
    }

    public List<Bericht> getBerichtenByLaadProcesId(Long id) throws SQLException {
        List<Bericht> berichten;
        ResultSetHandler<List<Bericht>> hb = new BeanListHandler(Bericht.class, new StagingRowHandler());
        berichten = new QueryRunner().query(getConnection(), "select * from " + BrmoFramework.BERICHT_TABLE + " where laadprocesid = ?", hb, id);
        Collections.sort(berichten ,new BerichtenSorter());
        return berichten;
    }
*/

    public void updateBerichtenDbXml(List<Bericht> berichten, RsgbTransformer transformer) throws SQLException, SAXException, IOException, TransformerException  {
        for (Bericht ber : berichten) {
            Split split = SimonManager.getStopwatch("b3p.staging.bericht.dbxml.transform").start();
            String dbxml = transformer.transformToDbXml(ber);
            ber.setDbXml(dbxml);
            //updateBericht(ber);
            split.stop();
        }
    }

    private PreparedStatement getOldBerichtStatement;

    public Bericht getOldBericht(Bericht nieuwBericht) throws SQLException {
        // TODO use JPA entities

        Split split = SimonManager.getStopwatch("b3p.staging.bericht.getold").start();

        Bericht bericht = null;
        ResultSetHandler<List<Bericht>> h
                = new BeanListHandler(Bericht.class, new StagingRowHandler());

        if(getOldBerichtStatement == null) {
            String sql = null;

            if (dbType.contains("postgres")) {
                sql = "SELECT * FROM " + BrmoFramework.BERICHT_TABLE + " WHERE"
                    + " object_ref = ? AND status = ?"
                    + " ORDER BY datum desc limit 1";
            }

            if (dbType.contains("oracle")) {
                sql = "SELECT * FROM " + BrmoFramework.BERICHT_TABLE + " WHERE"
                    + " object_ref = ? AND status = ? "
                    + " ORDER BY datum desc";
            }

            getOldBerichtStatement = getConnection().prepareStatement(sql);
        } else {
            getOldBerichtStatement.clearParameters();
        }

        getOldBerichtStatement.setString(1, nieuwBericht.getObjectRef());
        getOldBerichtStatement.setString(2, Bericht.STATUS.RSGB_OK.toString());

        ResultSet rs = getOldBerichtStatement.executeQuery();
        List<Bericht> list = h.handle(rs);
        rs.close();

        if(!list.isEmpty()) {
            // XXX voor Oracle ROWNUM over subquery is efficienter, nu worden
            // alle vorige berichten opgehaald ipv alleen de laatste
            bericht = list.get(0);
        }

        split.stop();
        return bericht;
    }

    public void updateBericht(Bericht b) throws SQLException {
        // TODO use JPA entities

        Split split = SimonManager.getStopwatch("b3p.staging.bericht.update").start();

        new QueryRunner().update(getConnection(),
                "UPDATE " + BrmoFramework.BERICHT_TABLE + " set object_ref = ?,"
                + " datum = ?, volgordenummer = ?, soort = ?, opmerking = ?,"
                + " status = ?, status_datum = ?, br_xml = ?,"
                + " br_orgineel_xml = ?, db_xml = ?, xsl_version = ?"
                + " WHERE id = ?",
                b.getObjectRef(),
                new Timestamp(b.getDatum().getTime()), b.getVolgordeNummer(),
                b.getSoort(), b.getOpmerking(), b.getStatus().toString(),
                new Timestamp(b.getStatusDatum().getTime()), b.getBrXml(),
                b.getBrOrgineelXml(), b.getDbXml(), b.getXslVersion(),
                b.getId());
        split.stop();
    }

    public void updateBerichtProcessing(Bericht b) throws SQLException {
        // TODO use JPA entities

        Split split = SimonManager.getStopwatch("b3p.staging.bericht.updateprocessing").start();

        new QueryRunner().update(getConnection(),
                "UPDATE " + BrmoFramework.BERICHT_TABLE + " set opmerking = ?,"
                + " status = ?, status_datum = ?, db_xml = ?"
                + " WHERE id = ?",
                b.getOpmerking(), b.getStatus().toString(),
                new Timestamp(b.getStatusDatum().getTime()), b.getDbXml(),
                b.getId());
        split.stop();
    }

    public void deleteByLaadProcesId(Long id) throws SQLException {
        new QueryRunner().update(getConnection(), "DELETE FROM " + BrmoFramework.BERICHT_TABLE
                + " WHERE laadprocesid = ?", id);
        new QueryRunner().update(getConnection(), "DELETE FROM " + BrmoFramework.LAADPROCES_TABEL
                + " WHERE id = ?", id);
    }

    public List<LaadProces> getLaadProcessen() throws SQLException {
        List<LaadProces> list;
        ResultSetHandler<List<LaadProces>> h = new BeanListHandler(LaadProces.class, new StagingRowHandler());
        list = new QueryRunner().query(getConnection(), "select * from " + BrmoFramework.LAADPROCES_TABEL, h);
        return list;
    }

    public List<Bericht> getBerichten() throws SQLException {
        List<Bericht> berichten;
        ResultSetHandler<List<Bericht>> h = new BeanListHandler(Bericht.class, new StagingRowHandler());
        berichten = new QueryRunner().query(getConnection(), "select * from " + BrmoFramework.BERICHT_TABLE, h);
        return berichten;
    }

    /**
     * Laadt het bestand uit de stream in de database.
     *
     * @param stream input
     * @param type type registratie, bijv. {@value BrmoFramework.BR_BRK}
     * @param fileName naam van het bestand (ter identificatie)
     * @param listener progress listener
     * @throws Exception
     */
    public void loadBr(InputStream stream, String type, String fileName, ProgressUpdateListener listener) throws Exception {

        CountingInputStream cis = new CountingInputStream(stream);

        BrmoXMLReader brmoXMLReader = null;
        if (type.equals(BrmoFramework.BR_BRK)) {
            brmoXMLReader = new BrkSnapshotXMLReader(cis);
        } else if (type.equals(BrmoFramework.BR_BAG)) {
            brmoXMLReader = new BagXMLReader(cis);
        } else {
            throw new UnsupportedOperationException("Ongeldige basisregistratie: " + type);
        }

        if (brmoXMLReader.getBestandsDatum()==null) {
            throw new BrmoException("Header van bestand bevat geen datum, verkeerd formaat?");
        }

        LaadProces lp = new LaadProces();
        lp.setBestandNaam(fileName);
        lp.setBestandDatum(brmoXMLReader.getBestandsDatum());
        lp.setSoort(type);
        lp.setGebied(brmoXMLReader.getGebied());
        lp.setStatus(LaadProces.STATUS.STAGING_OK);
        lp.setStatusDatum(new Date());

        if (laadProcesExists(lp)) {
            throw new BrmoDuplicaatLaadprocesException("Laadproces al gerund voor bestand "+ fileName);
        }
        lp = writeLaadProces(lp);
        boolean isBerichtGeschreven = false;

        if(!brmoXMLReader.hasNext()) {
            throw new BrmoLeegBestandException("Leeg bestand, geen berichten gevonden in "+ fileName);
        }
        int berichten = 0;
        while (brmoXMLReader.hasNext()) {
            Bericht b = null;
            try {
                b = brmoXMLReader.next();
                b.setLaadProcesId(lp.getId().intValue());
                b.setStatus(Bericht.STATUS.STAGING_OK);
                b.setStatusDatum(new Date());
                b.setSoort(type);

                if (!berichtExists(b)) {
                    writeBericht(b);
                    isBerichtGeschreven = true;
                }

                if (listener != null) {
                    listener.progress(cis.getByteCount());
                }
                berichten++;
            } catch (Exception e) {
                log.error("Bericht mislukt vanwege " + e.getLocalizedMessage());
                if(listener != null){
                    listener.exception(e);
                }
            }
        }
        if(listener != null) {
            listener.total(berichten);
        }
        if (!isBerichtGeschreven) {
            this.updateLaadProcesStatus(lp, LaadProces.STATUS.STAGING_DUPLICAAT);
        }
    }

    /**
     * bepaal of bericht bestaat aan de hand van laadprocesid, object_ref,
     * datum en volgordenummer.
     *
     * @param b
     * @return
     * @throws SQLException
     */
    public boolean berichtExists(Bericht b) throws SQLException {
        Object o = new QueryRunner().query(getConnection(),
                "SELECT 1 FROM " + BrmoFramework.BERICHT_TABLE + " WHERE"
                // + " laadprocesid = ? AND"
                + " object_ref = ? AND datum = ?"
                + " AND volgordenummer = ?", new ScalarHandler(),
                // b.getLaadProcesId(),
                b.getObjectRef(),
                new Timestamp(b.getDatum().getTime()),
                b.getVolgordeNummer());

        return o != null;
    }

    public void writeBericht(Bericht b) throws SQLException {
        new QueryRunner().update(getConnection(),
                "INSERT INTO " + BrmoFramework.BERICHT_TABLE + "(laadprocesid, object_ref,"
                + " datum, volgordenummer, soort, opmerking, status,"
                + " status_datum, br_xml, br_orgineel_xml, db_xml,"
                + " xsl_version)"
                + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                b.getLaadProcesId(), b.getObjectRef(),
                new Timestamp(b.getDatum().getTime()), b.getVolgordeNummer(),
                b.getSoort(), b.getOpmerking(), b.getStatus().toString(),
                new Timestamp(b.getStatusDatum().getTime()), b.getBrXml(),
                b.getBrOrgineelXml(), b.getDbXml(), b.getXslVersion());
     }

    /**
     * Bepaal aan de hand van bestandsnaam en bestandsdatum van het laadproces
     * of dit al bestaat.
     *
     * @param lp
     * @return
     * @throws SQLException
     */
    private boolean laadProcesExists(LaadProces lp) throws SQLException {
        Object o = new QueryRunner().query(getConnection(),
                "select 1 from laadproces where bestand_naam = ? and"
                + " bestand_datum = ?", new ScalarHandler(),
                lp.getBestandNaam(),
                new Timestamp(lp.getBestandDatum().getTime()));

        return o != null;
    }

    private LaadProces writeLaadProces(LaadProces lp) throws SQLException {
        if (lp == null) {
            return null;
        }

        new QueryRunner().update(getConnection(),
                "INSERT INTO " + BrmoFramework.LAADPROCES_TABEL + "(bestand_naam,"
                + "bestand_datum, soort, gebied, opmerking, status,"
                + "status_datum, contact_email) VALUES"
                + " (?,?,?,?,?,?,?,?)",
                lp.getBestandNaam(),
                new Timestamp(lp.getBestandDatum().getTime()),
                lp.getSoort(),
                lp.getGebied(),
                lp.getOpmerking(),
                lp.getStatus().toString(),
                new Timestamp(lp.getStatusDatum().getTime()),
                lp.getContactEmail());

        return getLaadProcesByNaturalKey(lp.getBestandNaam(), lp.getBestandDatum().getTime());
    }

    private void updateLaadProcesStatus(LaadProces lp, LaadProces.STATUS status) throws SQLException{
        new QueryRunner().update(getConnection(), "update " + BrmoFramework.LAADPROCES_TABEL + " set status = ? where id = ?",
                        status.toString(), lp.getId());
    }

    public void emptyStagingDb() throws SQLException {
        new QueryRunner().update(getConnection(), "DELETE FROM " + BrmoFramework.BERICHT_TABLE);
        new QueryRunner().update(getConnection(), "DELETE FROM " + BrmoFramework.LAADPROCES_TABEL);
    }

    public List<Bericht> getBerichten(int page, int start, int limit, String sort,
            String dir, String filterSoort, String filterStatus) throws SQLException {

        List<String> params = new ArrayList();
        String sql = "SELECT * FROM " + BrmoFramework.BERICHT_TABLE + buildFilterSql(page, start, limit, sort, dir,
                filterSoort, filterStatus, false, params);

        if (dbType.contains("oracle") && (start > 0 || limit > 0)) {
            sql = buildPaginationSqlOracle(sql, start, limit);
        }

        return (List<Bericht>)new QueryRunner().query(getConnection(), sql, new BeanListHandler(Bericht.class, new StagingRowHandler()), params.toArray());
    }

    /*
     * Check http://www.oracle.com/technetwork/issue-archive/2006/06-sep/o56asktom-086197.html
     * why just WHERE ROWNUM > x AND ROWNUM < x does not work.
     * Using subquery with optimizer.
    */
    private String buildPaginationSqlOracle(String sql, int offset, int limit) {
        StringBuilder builder = new StringBuilder();

        builder.append("select * from (");
        builder.append("select /*+ FIRST_ROWS(n) */");
        builder.append(" a.*, ROWNUM rnum from (");

        builder.append(sql);

        builder.append(" ) a where ROWNUM <=");

        builder.append(offset + limit);

        builder.append(" )");

        builder.append(" where rnum  > ");
        builder.append(offset);

        return builder.toString();
    }

    public long getCountBerichten(String sort, String dir, String filterSoort, String filterStatus) throws SQLException {

        List<String> params = new ArrayList();
        String sql = "SELECT count(*) FROM " + BrmoFramework.BERICHT_TABLE + buildFilterSql(0, 0, 0, null, null, filterSoort,
                filterStatus, true, params);

        Object o = new QueryRunner().query(getConnection(), sql, new ScalarHandler(), params.toArray());

        return o instanceof BigDecimal ? ((BigDecimal)o).longValue() : (Long)o;
    }

    private String buildFilterSql(int page, int start, int limit, String sort,
            String dir, String filterSoort, String filterStatus,
            boolean useForCount, List<String> params) {

        StringBuilder builder = new StringBuilder();

        List<String> conjunctions = new ArrayList();

        if(!StringUtils.isBlank(filterSoort)) {
            String[] soorten = filterSoort.split(",");
            params.addAll(Arrays.asList(soorten));
            conjunctions.add("soort in (" + StringUtils.repeat("?", ", ", soorten.length) + ")");
        }

        if(!StringUtils.isBlank(filterStatus)) {
            String[] statussen = filterStatus.split(",");
            params.addAll(Arrays.asList(statussen));
            conjunctions.add("status in (" + StringUtils.repeat("?", ", ", statussen.length) + ")");
        }

        // build where part
        if(!conjunctions.isEmpty()) {
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

        // build paging part
        if (!useForCount && start > 0 || limit > 0) {

            if (dbType.contains("postgres")) {
                builder.append(" LIMIT ");
                builder.append(limit);
                builder.append(" OFFSET ");
                builder.append(start);
            }
        }

        return builder.toString();
    }

    public long getCountLaadProces(String sort, String dir, String filterSoort, String filterStatus) throws SQLException {

        List<String> params = new ArrayList();
        String sql = "SELECT count(*) FROM " + BrmoFramework.LAADPROCES_TABEL + buildFilterSql(0, 0, 0, null, null, filterSoort,
              filterStatus, true, params);

        Object o = new QueryRunner().query(getConnection(), sql, new ScalarHandler(), params.toArray());
        return o instanceof BigDecimal ? ((BigDecimal)o).longValue() : (Long)o;
    }

    public List<LaadProces> getLaadprocessen(int page, int start, int limit, String sort,
            String dir, String filterSoort, String filterStatus) throws SQLException {

        List<String> params = new ArrayList();
        String sql = "SELECT * FROM " + BrmoFramework.LAADPROCES_TABEL + buildFilterSql(
                page, start, limit, sort, dir,
                filterSoort, filterStatus, false,params);

        if (dbType.contains("oracle") && (start > 0 || limit > 0)) {
            sql = buildPaginationSqlOracle(sql, start, limit);
        }

        return (List<LaadProces>)new QueryRunner().query(getConnection(), sql, new BeanListHandler(LaadProces.class, new StagingRowHandler()), params.toArray());
    }
}
