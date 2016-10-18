package nl.b3p.brmo.loader;

import com.vividsolutions.jts.io.ParseException;
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
import java.util.SortedSet;
import java.util.TreeSet;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.jdbc.ColumnMetadata;
import nl.b3p.brmo.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.brmo.loader.jdbc.GeometryJdbcConverterFactory;
import nl.b3p.brmo.loader.jdbc.OracleJdbcConverter;
import nl.b3p.brmo.loader.updates.UpdateProcess;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.DataComfortXMLReader;
import nl.b3p.brmo.loader.util.TableData;
import nl.b3p.brmo.loader.util.TableRow;
import nl.b3p.brmo.loader.util.RsgbTransformer;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.w3c.dom.Node;

/**
 *
 * @author Boy de Wit
 */
public class RsgbProxy implements Runnable, BerichtenHandler {

    private static final Log log = LogFactory.getLog(RsgbProxy.class);

    private final boolean alsoExecuteSql = true;

    private final ProgressUpdateListener listener;

    private int processed;

    public enum BerichtSelectMode {

        BY_STATUS, BY_IDS, BY_LAADPROCES, FOR_UPDATE, RETRY_WAITING
    }

    private final BerichtSelectMode mode;

    private final UpdateProcess updateProcess;

    /**
     * De status voor BY_STATUS.
     */
    private Bericht.STATUS status;

    /**
     * De bericht ID's voor BY_IDS.
     */
    private long[] berichtIds;

    /**
     * Het ID van het laadproces vor BY_LAADPROCES.
     */
    private long[] laadprocesIds;

    private enum STATEMENT_TYPE {

        INSERT, UPDATE, SELECT, DELETE
    }

    private DatabaseMetaData dbMetadata = null;
    /* Map van lowercase tabelnaam naar originele case tabelnaam */
    private Map<String, String> tables = new HashMap();
    private Map<String, SortedSet<ColumnMetadata>> tableColumns = new HashMap();

    private Connection connRsgb = null;
    private GeometryJdbcConverter geomToJdbc = null;
    private Savepoint recentSavepoint = null;
    private String herkomstMetadata = null;

    private DataSource dataSourceRsgb = null;
    private StagingProxy stagingProxy = null;

    private boolean enablePipeline = false;
    private int pipelineCapacity = 25;

    private boolean orderBerichten = true;
    private String errorState = null;

    private Map<String, RsgbTransformer> rsgbTransformers = new HashMap();

    private String simonNamePrefix = "b3p.rsgb.";

    private final DataComfortXMLReader dbXmlReader = new DataComfortXMLReader();

    // Gebruik andere instantie voor lezen oude berichten, gebeurt gelijktijdig
    // in andere thread als lezen van dbxml van berichten om te verwerken
    private final DataComfortXMLReader oldDbXmlReader = new DataComfortXMLReader();

    public RsgbProxy(DataSource dataSourceRsgb, StagingProxy stagingProxy, Bericht.STATUS status, ProgressUpdateListener listener) {
        this.stagingProxy = stagingProxy;
        this.dataSourceRsgb = dataSourceRsgb;

        mode = BerichtSelectMode.BY_STATUS;
        this.status = status;
        this.listener = listener;
        this.updateProcess = null;
    }

    /**
     *
     * @param dataSourceRsgb
     * @param stagingProxy
     * @param mode
     * @param listener
     */
    public RsgbProxy(DataSource dataSourceRsgb, StagingProxy stagingProxy, BerichtSelectMode mode, long[] ids, ProgressUpdateListener listener) {
        this.stagingProxy = stagingProxy;
        this.dataSourceRsgb = dataSourceRsgb;

        this.mode = mode;
        if (mode == BerichtSelectMode.BY_LAADPROCES) {
            this.laadprocesIds = ids;
        } else if (mode == BerichtSelectMode.BY_IDS) {
            this.berichtIds = ids;
        }
        this.listener = listener;
        this.updateProcess = null;
    }

    public RsgbProxy(DataSource dataSourceRsgb, StagingProxy stagingProxy, UpdateProcess updateProcess, ProgressUpdateListener listener) {
        this.stagingProxy = stagingProxy;
        this.dataSourceRsgb = dataSourceRsgb;

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

    public void setOrderBerichten(boolean orderBerichten) {
        this.orderBerichten = orderBerichten;
    }

    public void setErrorState(String errorState) {
        this.errorState = errorState;
    }

    public void init() throws SQLException {
        connRsgb = dataSourceRsgb.getConnection();

        geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(connRsgb);

        ResultSet tablesRs = null;
        try {
            dbMetadata = connRsgb.getMetaData();

            tablesRs = dbMetadata.getTables(null, geomToJdbc.getSchema(), "%", new String[]{"TABLE"});
            while (tablesRs.next()) {
                tables.put(tablesRs.getString("TABLE_NAME").toLowerCase(), tablesRs.getString("TABLE_NAME"));
            }
        } catch (SQLException sqlEx) {
            throw new SQLException("Fout bij ophalen tabellen: ", sqlEx);
        } finally {
            if (tablesRs!=null && !tablesRs.isClosed()) {
                tablesRs.close();
            }
        }
    }

    private final Object LOCK = new Object();
    private final Map<String, PreparedStatement> checkRowExistsStatements = new HashMap();
    private final Map<String, PreparedStatement> insertSqlPreparedStatements = new HashMap();
    private final Map<String, PreparedStatement> updateSqlPreparedStatements = new HashMap();
    private PreparedStatement insertMetadataStatement;

    private PreparedStatement checkAndGetPreparedStatement(Map<String, PreparedStatement> m, String name) throws SQLException {
        PreparedStatement stm = m.get(name);
        if (stm == null || stm.isClosed()) {
            return null;
        }
        stm.clearParameters();
        return stm;
    }

    public void checkAndCloseStatement(PreparedStatement stmt) {
//        if (geomToJdbc instanceof OracleJdbcConverter) {
//            DbUtils.closeQuietly(stmt);
//            return;
//        }
        // do not close now, but later in batch
    }

    public void checkAndAddStatement(Map<String, PreparedStatement> m, String tableName, PreparedStatement stmt) {
//        if (geomToJdbc instanceof OracleJdbcConverter) {
//            // do never add
//            return;
//        }
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
        if (insertMetadataStatement != null) {
            DbUtils.closeQuietly(insertMetadataStatement);
        }
        insertMetadataStatement = null;
        DbUtils.closeQuietly(connRsgb);
        connRsgb = null;
    }

    @Override
    public void renewConnection() throws SQLException {
        //TODO check of dit echt werkt zoals bedoeld
        synchronized (LOCK) {
            synchronized (connRsgb) {
                close();
                init();
            }
        }
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
            if (total>0) {
                stagingProxy.handleBerichtenByJob(total, this, enablePipeline, pipelineCapacity, orderBerichten);
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
        //verwijder al verwerkte berichten uit afgebroken job
        stagingProxy.cleanJob();
        //zijn er nog berichten over uit de vorige job
        long count = stagingProxy.getCountJob();
        if (count > 0 && !mode.equals(BerichtSelectMode.RETRY_WAITING)) {
            throw new BrmoException("Vorige transformatie is afgebroken,"
                    + " verwerk eerst die berichten (zie job-tabel)!");
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
                return 0l;
        }
    }

    @Override
    public void updateProcessingResult(Bericht ber) {
        if (updateProcess != null) {
            return;
        }
        ber.setStatusDatum(new Date());
        try {
            stagingProxy.updateBericht(ber);
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
            loadLog.append(String.format("%s: transformeren %s bericht object ref %s, met id %s, berichtdatum %s\n", dateTimeFormat.format(new Date()), ber.getSoort(), ber.getObjectRef(), ber.getId(), dateFormat.format(ber.getDatum())));

            RsgbTransformer transformer = getTransformer(ber.getSoort());

            long startTime = System.currentTimeMillis();
            stagingProxy.updateBerichtenDbXml(Collections.singletonList(ber), transformer);

            Split split = SimonManager.getStopwatch("b3p.staging.bericht.dbxml.read").start();
            StringReader nReader = new StringReader(ber.getDbXml());
            List<TableData> data = dbXmlReader.readDataXML(new StreamSource(nReader));
            split.stop();

            loadLog.append(String.format("Transformeren naar RSGB database-xml en lezen resultaat: %4.1fs\n\n", (System.currentTimeMillis() - startTime) / 1000.0));
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
            List<TableData> data = dbXmlReader.readDataXML(new DOMSource(dbxml));
            return data;
        } catch (Exception e) {
            log.error("Fout bij transformeren bericht #" + ber.getId() + " voor update", e);
            throw new BrmoException("Fout bij transformeren bericht #" + ber.getId() + " voor update: " + e.getClass() + ": " + e.getMessage());
        }
    }

    public void updateBerichtException(Bericht ber, Throwable e) throws BrmoException {
        ber.setStatus(Bericht.STATUS.RSGB_NOK);
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw, true));
        ber.setOpmerking(ber.getOpmerking() + "\nFout: " + sw.toString());
        if (orderBerichten) {
            if (errorState!=null && errorState.equalsIgnoreCase("ignore")) {
                //omdat we te vaak moeten stoppen, later soort exception erbij
                //betrekken.
            } else {
                // volgorde belangrijk dus stoppen
                throw new BrmoException("Mutatieverwerking en foutief bericht, dus stoppen!");
            }
        }
    }

    @Override
    public void updateBerichtProcessing(Bericht ber) throws Exception {
        if (updateProcess != null) {
            return;
        }
        //via job tabel, niet langer status aanpassen
        return;
//        ber.setStatus(Bericht.STATUS.RSGB_PROCESSING);
//        ber.setOpmerking("");
//        ber.setStatusDatum(new Date());
//        ber.setDbXml(null);
//        stagingProxy.updateBerichtProcessing(ber);
    }

    @Override
    public void handle(Bericht ber, List<TableData> pretransformedTableData, boolean updateResult) throws BrmoException {
        // lock on connection, omdat parent loop periodiek connectie wil verversen.
        synchronized (LOCK) {
        synchronized (connRsgb) {

            if (updateProcess != null) {
                update(ber, pretransformedTableData);
                return;
            }

            Bericht.STATUS newStatus = Bericht.STATUS.RSGB_OK;

            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss:SSS");
//        dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
//        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            log.debug(String.format("RSGB verwerking van %s bericht met id %s, object_ref %s", ber.getSoort(), ber.getId(), ber.getObjectRef()));
            StringBuilder loadLog;
            if (pretransformedTableData == null) {
                loadLog = new StringBuilder();
            } else {
                loadLog = new StringBuilder(ber.getOpmerking());
            }
            loadLog.append(String.format("%s: start verwerking %s bericht object ref %s, met id %s, berichtdatum %s\n", dateTimeFormat.format(new Date()), ber.getSoort(), ber.getObjectRef(), ber.getId(), dateFormat.format(ber.getDatum())));

            setHerkomstMetadata(ber.getSoort());

            long startTime = 0;
            try {

                if (connRsgb.getAutoCommit()) {
                    connRsgb.setAutoCommit(false);
                }

                List<TableData> newList;
                if (pretransformedTableData != null) {
                    loadLog.append("Bericht was al getransformeerd in pipeline\n");
                    newList = pretransformedTableData;
                } else {
                    loadLog.append("Transformeren naar RSGB database-xml en lezen resultaat... ");
                    startTime = System.currentTimeMillis();
                    newList = transformToTableData(ber);
                    loadLog.append(String.format("%4.1fs\n", (System.currentTimeMillis() - startTime) / 1000.0));
                }
                // nieuwe starttijd voor laden in RSGB
                startTime = System.currentTimeMillis();

                // per bericht kijken of er een oud bericht is
                Bericht oud = null;
                if (orderBerichten) {
                    oud = stagingProxy.getOldBericht(ber, loadLog);
                } else {
                    //als geen orderberichten dan wordt stand ingelezen
                    //en indien volgordenummer >=0 (geen stand) dan afbreken
                    if (ber.getVolgordeNummer() >= 0) {
                        throw new BrmoException("Standverwerking, maar bericht is mutatie: niet verwerken!");
                    }
                    loadLog.append("Standverwerking, gegevens worden alleen toegevoegd als ze nog niet bestaan!\n");
                }

                // oud bericht
                if (oud != null) {
                    loadLog.append(String.format("Eerder bericht (id: %d) voor zelfde object gevonden van datum %s, volgnummer %d,status %s op %s\n",
                            oud.getId(),
                            dateFormat.format(oud.getDatum()),
                            oud.getVolgordeNummer(),
                            oud.getStatus().toString(),
                            dateTimeFormat.format(oud.getStatusDatum())));

                    if (ber.getDatum().equals(oud.getDatum()) && ber.getVolgordeNummer().equals(oud.getVolgordeNummer())) {
                        if (ber.getId() == oud.getId()) {
                            //dit kan voorkomen bij herstart van job terwijl bericht nog in pipeline van andere job zat
                            //niets doen, log overnemen.
                            loadLog.append(oud.getOpmerking());
                        } else {
                            loadLog.append("Datum en volgordenummer van nieuw bericht hetzelfde als de oude, negeer dit bericht!\n");
                            boolean dbXmlEquals = ber.getDbXml().equals(oud.getDbXml());
                            if (!dbXmlEquals) {
                                String s = String.format("Bericht %d met zelfde datum en volgnummer als eerder verwerkt bericht %d heeft andere db xml! Object ref %s",
                                        ber.getId(), oud.getId(), ber.getObjectRef());
                                log.warn(s);
                                loadLog.append("Waarschuwing: ").append(s).append("\n");
                            }
                        }
                    } else {

                        // Check of eerder bericht toevallig niet nieuwere datum
                        if (oud.getDatum().after(ber.getDatum())) {
                            newStatus = Bericht.STATUS.RSGB_OUTDATED;
                            loadLog.append("Bericht bevat oudere data dan eerder verwerkt bericht, status RSGB_OUTDATED\n");
                        } else {
                            StringReader oReader = new StringReader(oud.getDbXml());
                            List<TableData> oudList = oldDbXmlReader.readDataXML(new StreamSource(oReader));

                            parseNewData(oudList, newList, dateFormat.format(oud.getDatum()), loadLog);
                            parseOldData(oudList, newList, dateFormat.format(ber.getDatum()), dateFormat.format(oud.getDatum()), loadLog);
                        }
                    }
                } else {
                    parseNewData(null, newList, null, loadLog);
                }

                Split commit = SimonManager.getStopwatch(simonNamePrefix + "commit").start();
                connRsgb.commit();
                commit.stop();
                ber.setStatus(newStatus);

                this.processed++;
                if (listener != null) {
                    listener.progress(this.processed);
                }

            } catch (Throwable ex) {
                log.error("Fout bij verwerking bericht met id " + ber.getId(), ex);
                try {
                    connRsgb.rollback();
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
                loadLog.append(String.format("%s: einde verwerking, duur: %4.1fs", dateTimeFormat.format(new Date()), (System.currentTimeMillis() - startTime) / 1000.0));
                ber.setOpmerking(loadLog.toString());

                if (updateResult) {
                    updateProcessingResult(ber);
                }

                if (recentSavepoint != null) {
                    // Set savepoint to null, as it is automatically released after commit the transaction.
                    recentSavepoint = null;
                }
            }
        }
        }
    }

    public void update(Bericht ber, List<TableData> pretransformedTableData) throws BrmoException {

        try {

            if (connRsgb.getAutoCommit()) {
                connRsgb.setAutoCommit(false);
            }

            List<TableData> newList;
            if (pretransformedTableData != null) {
                newList = pretransformedTableData;
            } else {
                newList = transformUpdateTableData(ber);
            }

            for (TableData newData : newList) {
                for (TableRow row : newData.getRows()) {
                    createUpdateSql(row, new StringBuilder());
                }
            }

            connRsgb.commit();

            this.processed++;
            if (listener != null) {
                listener.progress(this.processed);
            }

        } catch (Throwable ex) {
            log.error("Fout bij updaten bericht met id " + ber.getId(), ex);
            try {
                connRsgb.rollback();
            } catch (SQLException e) {
                log.debug("Rollback exception", e);
            }
        }
    }

    private RsgbTransformer getTransformer(String brType) throws TransformerConfigurationException, ParserConfigurationException {
        RsgbTransformer t = rsgbTransformers.get(brType);
        if (t == null) {
            if (brType.equals(BrmoFramework.BR_BRK)) {
                t = new RsgbTransformer(BrmoFramework.XSL_BRK);
            } else if (brType.equals(BrmoFramework.BR_BAG)) {
                t = new RsgbTransformer(BrmoFramework.XSL_BAG);
            } else if (brType.equals(BrmoFramework.BR_NHR)) {
                t = new RsgbTransformer(BrmoFramework.XSL_NHR);
//            } else if (brType.equals(BrmoFramework.BR_BGTLIGHT)) {
//                t = new BGTLightRsgbTransformer(this.stagingProxy);
            } else {
                throw new IllegalArgumentException("Onbekende basisregistratie: " + brType);
            }
            rsgbTransformers.put(brType, t);
        }
        return t;
    }

    private StringBuilder parseNewData(List<TableData> oldList,
            List<TableData> newList, String oldDate,
            StringBuilder loadLog) throws SQLException, ParseException, BrmoException {
        Split split = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata").start();

        // parse new db xml
        loadLog.append("\nVerwerk nieuw bericht");
        if (newList == null || newList.isEmpty()) {
            if (oldList == null || oldList.isEmpty()) {
                throw new BrmoException("Bericht verwijdert object waarvoor geen eerder bericht gevonden is!");
            }
            split.stop();
            return loadLog;
        }

        String lastExistingBrondocumentId = null;
        for (TableData newData : newList) {
            if (!newData.isComfortData() && !newData.isDeleteData()) {
                Split splitAuthentic = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.authentic").start();
                // auth data
                loadLog.append("\n");
                for (TableRow row : newData.getRows()) {

                    // Controleer op al bestaand stukdeel
                    if (lastExistingBrondocumentId != null && "brondocument".equalsIgnoreCase(row.getTable())
                            && lastExistingBrondocumentId.equals(row.getColumnValue("tabel_identificatie"))) {
                        loadLog.append("\nOverslaan stukdeel voor stuk ").append(lastExistingBrondocumentId);
                        SimonManager.getCounter(simonNamePrefix + "parsenewdata.authentic.skipstukdeel").increase();
                        continue;
                    } else {
                        lastExistingBrondocumentId = null;
                    }

                    Split split2 = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.authentic.getpk").start();
                    List<String> pkColumns = getPrimaryKeys(row.getTable());
                    split2.stop();

                    boolean existsInOldList = doesRowExist(row, pkColumns, oldList);
                    split2 = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.authentic.rowexistsindb").start();
                    // er kan een record bestaan zonder dat er
                    // een oud bericht aan ten grondslag ligt.
                    // dan maar update maar zonder historie vastlegging.
                    // oud record kan niet worden terugherleid.
                    //TODO: dure check, kan het anders?
                    boolean doInsert = !rowExistsInDb(row, loadLog);
                    split2.stop();

                    // insert hoofdtabel
                    if (doInsert) {
                        if (existsInOldList) {
                            // insert in hoofdtabel, terwijl er wel oud bericht beschikbaar is
                            // dit lijkt op een fout situatie, wat te doen?
                            loadLog.append("\nOud bericht bestaat, maar staat niet (meer) in database, daarom toevoeging van object aan tabel: ");
                        } else {
                            // normale insert in hoofdtabel
                            loadLog.append("\nNormale toevoeging van object aan tabel: ");
                        }
                        loadLog.append(row.getTable());
                        split2 = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.authentic.insert").start();
                        createInsertSql(row, false, loadLog);
                        split2.stop();
                    } else {
                        // XXX kan momenteel niet werken; herkomst_metadata heeft alleen
                        // superclass records.
                        // Logisch ook niet mogelijk dat comfort data opeens authentiek wordt
                        // momenteel (pas bij Basisregistratie Personen / NHR)

                        //split2 = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.authentic.isalreadyinmetadata").start();
                        //boolean inMetaDataTable = isAlreadyInMetadata(row, loadLog);
                        //split2.stop();
                        // wis metadata en update hoofdtabel
                        //if (inMetaDataTable) {
                        //    loadLog.append("\nwis uit metadata tabel (upgrade van comfort naar authentiek). ");
                        //    split2 = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.authentic.deletefrommetadata").start();
                        //    deleteFromMetadata(row, loadLog);
                        //    split2.stop();
                        //}
                        if ("brondocument".equalsIgnoreCase(row.getTable())) {
                            // Nooit stukdeel wat al bestaat updaten, en ook alle
                            // volgende stukdelen niet

                            lastExistingBrondocumentId = row.getColumnValue("tabel_identificatie");
                            loadLog.append("\nRij voor stukdeel ").append(lastExistingBrondocumentId).append(" bestaat al, geen update");
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

                            split2 = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.authentic.archive").start();
                            createInsertSql(oldRow, true, loadLog);
                            split2.stop();

                            loadLog.append("\nUpdate object in tabel: ");
                        } else {
                            loadLog.append("\nUpdate object (zonder vastlegging historie) in tabel: ");
                        }
                        loadLog.append(row.getTable());
                        split2 = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.authentic.update." + row.getTable()).start();
                        createUpdateSql(row, loadLog);
                        split2.stop();
                     }
                }
                splitAuthentic.stop();
                
            } else if (!newData.isDeleteData()) {
                Split splitComfort = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.comfort").start();

                // TODO: select herkomst_br/datum uit herkomst_metadata voor tabel,kolom,waarde
                // indien niet bestaat: nieuwe comfort data, alles unconditional insert
                // indien wel bestaat en herkomst_br/datum identiek: gegevens uit stand al geinsert, negeer
                // indien wel bestaat en herkomst_br/datum anders: update en insert nieuwe herkomst
                // comfort data
                String subclass = newData.getRows().get(newData.getRows().size() - 1).getTable();
                String tabel = newData.getComfortSearchTable();
                String kolom = newData.getComfortSearchColumn();
                String waarde = newData.getComfortSearchValue();
                String datum = newData.getComfortSnapshotDate();

                loadLog.append(String.format("\n\nComfort data voor %s (superclass: %s, %s=%s), controleer aanwezigheid in superclass tabel",
                        subclass, tabel, kolom, waarde));

                Split split2 = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.comfort.exists").start();
                boolean comfortFound = rowExistsInDb(newData.getRows().get(0), loadLog);
                split2.stop();

                for (TableRow row : newData.getRows()) {
                    // zoek obv natural key op in rsgb
                    if (comfortFound) {
                        split2 = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.comfort.update").start();
                        createUpdateSql(row, loadLog);
                        split2.stop();
                    } else {
                        // insert all comfort records into hoofdtabel
                        split2 = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.comfort.insert").start();
                        createInsertSql(row, false, loadLog);
                        split2.stop();
                    }
                }

                // Voeg herkomst van metadata toe, alleen voor superclass tabel!
                split2 = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.comfort.metadata").start();
                createInsertMetadataSql(tabel, kolom, waarde, datum, loadLog);
                split2.stop();

                splitComfort.stop();
                loadLog.append("\n");
                
            } else if (newData.isDeleteData() && (oldList == null || oldList.isEmpty())){
                //only use delete in newData if there is no old record (this is more complete)
                Split splitDelete = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.delete").start();
                loadLog.append("\n\nWissen van object zonder oud record.");
                for (TableRow row : newData.getRows()) {
                    // zoek obv natural key op in rsgb
                    if (rowExistsInDb(row, loadLog)) {
                        Split split2 = SimonManager.getStopwatch(simonNamePrefix + "parsenewdata.delete.update").start();
                        createDeleteSql(row, loadLog);
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

    private StringBuilder parseOldData(List<TableData> oldList, List<TableData> newList, String newDate, String oldDate, StringBuilder loadLog) throws SQLException, ParseException {

        List<TableRow> rowsToDelete = new ArrayList();
        // als heel object verwijderd moet worden omdat newList een verwijderbericht is
        boolean deleteAll = !newList.isEmpty() && newList.get(0).isDeleteData();

        // parse old db xml
        loadLog.append("\nControleren te verwijderen gegevens vorig bericht...\n");
        for (TableData oldData : oldList) {
            if (oldData.isComfortData()) {
                // comfort block ? do nothing
            } else {
                for (TableRow oldRow : oldData.getRows()) {
                    List<String> pkColumns = getPrimaryKeys(oldRow.getTable());
                    boolean exists = doesRowExist(oldRow, pkColumns, newList);
                    if (deleteAll || !exists) {
                        loadLog.append("Vervallen record te verwijderen: ").append(oldRow.toString(pkColumns)).append("\n");

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
                createDeleteSql(rowToDelete, loadLog);
                split.stop();

                updateValueInTableRow(rowToDelete, rowToDelete.getColumnDatumEindeGeldigheid(), newDate);

                split = SimonManager.getStopwatch(simonNamePrefix + "parseolddata.archive").start();
                rowToDelete.setIgnoreDuplicates(true);
                createInsertSql(rowToDelete, true, loadLog);
                split.stop();
            }
        }

        return loadLog;
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
        //TODO, mag later weer weg
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

    private TableRow getMatchingRowFromTableData(TableRow row, List<String> pkColumns, List<TableData> testList) {
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

    private void createInsertSql(TableRow row, boolean useArchiveTable, StringBuilder loadLog) throws SQLException, ParseException {

        StringBuilder sql = new StringBuilder("insert into ");

        String tableName;
        if (!useArchiveTable) {
            tableName = row.getTable();
        } else {
            tableName = row.getTable() + "_archief";
        }

        sql.append(tableName);

        SortedSet<ColumnMetadata> tableColumnMetadata = null;
        tableColumnMetadata = getTableColumnMetadata(tableName);

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
        List params = new ArrayList();

        // TODO maak sql op basis van alle columns en gebruik null values
        // zodat insert voor elke tabel hetzelfde, reuse preparedstatement
        Iterator<String> valuesIt = row.getValues().iterator();
        for (Iterator<String> it = row.getColumns().iterator(); it.hasNext();) {
            String column = it.next();
            String stringValue = valuesIt.next();

            if (row.isAlleenArchiefColumn(column) && !useArchiveTable) {
                continue;
            }

            ColumnMetadata cm = findColumnMetadata(tableColumnMetadata, column);
            Object param = getValueAsObject(tableName, column, stringValue, cm);
            params.add(param);

            sql.append(cm.getName());

            String insertValuePlaceholder = "?";
            if (cm.getTypeName().equals(geomToJdbc.getGeomTypeName())) {
                //waarde altijd als WKT aanbieden en placeholder per db type
                insertValuePlaceholder = geomToJdbc.createPSGeometryPlaceholder();
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
            if (geomToJdbc instanceof OracleJdbcConverter) {
                sql.append(" from dual");
            }
            sql.append(" where not exists (select 1 from ");
            sql.append(tableName);
            sql.append(" where ");

            boolean first = true;
            for (String column : getPrimaryKeys(tableName)) {
                if (first) {
                    first = false;
                } else {
                    sql.append(" and ");
                }
                sql.append(column);
                sql.append(" = ? ");

                String val = getValueFromTableRow(row, column);
                Object obj = getValueAsObject(tableName, column, val, null);
                params.add(obj);
            }
            sql.append(")");
        }

        PreparedStatement stm = checkAndGetPreparedStatement(insertSqlPreparedStatements, sql.toString());
        if (stm == null) {
            SimonManager.getCounter("b3p.rsgb.insertsql.preparestatement").increase();
            stm = connRsgb.prepareStatement(sql.toString());
            checkAndAddStatement(insertSqlPreparedStatements, sql.toString(), stm);
        } else {
            SimonManager.getCounter("b3p.rsgb.insertsql.reusestatement").increase();
        }
        loadLog.append("\nSQL: ");
        loadLog.append(sql);
        loadLog.append(", params (");
        loadLog.append(params.toString());
        loadLog.append(")");

        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            stm.setObject(i + 1, param);
        }

        int count = -1;
        try {
            count = stm.executeUpdate();
        } catch (SQLException e) {
            String message = e.getMessage();
            if (geomToJdbc.isDuplicateKeyViolationMessage(message) && row.isIgnoreDuplicates()) {
                if (recentSavepoint != null) {
                    log.debug("Ignoring duplicate key violation by rolling back to savepoint with id " + recentSavepoint.getSavepointId());
                    connRsgb.rollback(recentSavepoint);
                }
            } else {
                throw e;
            }
        } finally {
            checkAndCloseStatement(stm);
        }

        loadLog.append("\nAantal toegevoegde records: " + count);
    }

    private void createUpdateSql(TableRow row, StringBuilder loadLog) throws SQLException, ParseException {
        //doSavePoint(row);

        StringBuilder sql = new StringBuilder("update ");

        String tableName = row.getTable();

        sql.append(tableName);

        SortedSet<ColumnMetadata> tableColumnMetadata = null;
        tableColumnMetadata = getTableColumnMetadata(tableName);

        if (tableColumnMetadata == null) {
                throw new IllegalStateException("Kan tabel metadata niet vinden voor tabel " + tableName);
        }

        sql.append(" set ");

        List params = new ArrayList();

        // XXX does set previously set columns to NULL!
        // need statement for all columns from column metadata, not only
        // columns in TableRow
        Iterator<String> valuesIt = row.getValues().iterator();
        for (Iterator<String> it = row.getColumns().iterator(); it.hasNext();) {
            String column = it.next();
            String stringValue = valuesIt.next();

            if (row.isAlleenArchiefColumn(column)) {
                continue;
            }

            ColumnMetadata cm = findColumnMetadata(tableColumnMetadata, column);
            Object param = getValueAsObject(tableName, column, stringValue, cm);
            params.add(param);

            sql.append(cm.getName());
            sql.append(" = ");
            String insertValuePlaceholder = "?";
            if (cm.getTypeName().equals(geomToJdbc.getGeomTypeName())) {
                //waarde altijd als WKT aanbieden en placeholder per db type
                insertValuePlaceholder = geomToJdbc.createPSGeometryPlaceholder();
            }
            sql.append(insertValuePlaceholder);

            if (it.hasNext()) {
                sql.append(", ");
            }
        }

        // Where clause using pk columns
        sql.append(" where ");
        boolean first = true;
        for (String column : getPrimaryKeys(tableName)) {
            if (first) {
                first = false;
            } else {
                sql.append(" and ");
            }
            sql.append(column);
            sql.append(" = ? ");

            String val = getValueFromTableRow(row, column);
            Object obj = getValueAsObject(tableName, column, val, null);
            params.add(obj);
        }

        PreparedStatement stm = checkAndGetPreparedStatement(updateSqlPreparedStatements, sql.toString());
        if (stm == null) {
            SimonManager.getCounter("b3p.rsgb.updatesql.preparestatement").increase();
            stm = connRsgb.prepareStatement(sql.toString());
            checkAndAddStatement(updateSqlPreparedStatements, sql.toString(), stm);
        } else {
            SimonManager.getCounter("b3p.rsgb.updatesql.reusestatement").increase();
        }
        loadLog.append("\nSQL: ");
        loadLog.append(sql);
        loadLog.append(", params (");
        loadLog.append(params.toString());
        loadLog.append(")");

        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            stm.setObject(i + 1, param);
        }

        int count = -1;
        try {
            count = stm.executeUpdate();
        } finally {
            loadLog.append("\nAantal record updates: " + count);
            checkAndCloseStatement(stm);
        }
    }

    /* Columns id, tabel, kolom, waarde, herkomst_br, datum (toestandsdatum) */
    private boolean createInsertMetadataSql(String tabel, String kolom, String waarde, String datum, StringBuilder loadLog) throws SQLException, ParseException {

        if (insertMetadataStatement == null || insertMetadataStatement.isClosed()) {
            StringBuilder sql = new StringBuilder("insert into herkomst_metadata (tabel, kolom, waarde, herkomst_br, datum) ")
                    .append("select ?, ?, ?, ?, ? ")
                    .append(geomToJdbc instanceof OracleJdbcConverter ? "from dual" : "")
                    .append(" where not exists (")
                    .append("  select 1 from herkomst_metadata where tabel = ? and kolom = ? and waarde = ? and herkomst_br = ? and datum = ?")
                    .append(")");

            insertMetadataStatement = connRsgb.prepareStatement(sql.toString());
        } else {
            insertMetadataStatement.clearParameters();
        }

        Date date = null;
        Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(datum);
        if (calendar != null) {
            Calendar cal = (Calendar) calendar;
            date = new java.sql.Date(cal.getTimeInMillis());
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

        loadLog.append(String.format("\nConditioneel toevoegen herkomst metadata tabel=%s, kolom=%s, waarde=%s, herkomst_br=%s, datum=%s",
                tabel, kolom, waarde, getHerkomstMetadata(), datum));

        int count = insertMetadataStatement.executeUpdate();
        loadLog.append(": record geinsert: ").append(count > 0 ? "ja" : "nee (rij bestond al)");
        return count > 0;
    }

    private boolean rowExistsInDb(TableRow row, StringBuilder loadLog) throws SQLException, ParseException {

        String tableName = row.getTable();
        List<String> pkColumns = getPrimaryKeys(tableName);

        List params = new ArrayList();
        for (String column : pkColumns) {
            String val = getValueFromTableRow(row, column);
            Object obj = getValueAsObject(row.getTable(), column, val, null);
            params.add(obj);
        }

        loadLog.append("\nControleer ").append(tableName).append(" op primary key: ").append(params.toString());

        PreparedStatement stm = checkAndGetPreparedStatement(checkRowExistsStatements, tableName);

        if (stm == null) {
            StringBuilder sql = new StringBuilder("select 1 from ")
                    .append(tableName);

            boolean first = true;
            for (String column : pkColumns) {
                if (first) {
                    sql.append(" where ").append(column).append(" = ?");
                    first = false;
                } else {
                    sql.append(" and ").append(column).append(" = ?");
                }
            }

            stm = connRsgb.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
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
            if(rs != null) {
                rs.close();
                checkAndCloseStatement(stm); // ???
            }
        }
        loadLog.append(", rij bestaat: ").append(exists ? "ja" : "nee");

        return exists;
    }

    private final Map<String, PreparedStatement> getTableRowStatements = new HashMap();

    /**
     *
     * @param row
     * @param loadLog
     * @return
     * @throws SQLException
     * @throws ParseException
     *
     * @deprecated Momenteel niet in gebruik. Veel updates vinden nu niet plaats
     * nu brondocumenten al een specifieke optimalisatie hebben.
     */
    private TableRow getTableRowFromDb(TableRow row, StringBuilder loadLog) throws SQLException, ParseException {

        String tableName = row.getTable();
        List<String> pkColumns = getPrimaryKeys(tableName);

        List params = new ArrayList();
        for (String column : pkColumns) {
            String val = getValueFromTableRow(row, column);
            Object obj = getValueAsObject(row.getTable(), column, val, null);
            params.add(obj);
        }

        loadLog.append("\nControleer ").append(tableName).append(" op primary key: ").append(params.toString());

        PreparedStatement stm = checkAndGetPreparedStatement(checkRowExistsStatements, tableName);

        if (stm == null) {
            StringBuilder sql = new StringBuilder("select * from ")
                    .append(tableName);

            boolean first = true;
            for (String column : pkColumns) {
                if (first) {
                    sql.append(" where ").append(column).append(" = ?");
                    first = false;
                } else {
                    sql.append(" and ").append(column).append(" = ?");
                }
            }

            stm = connRsgb.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            checkAndAddStatement(checkRowExistsStatements, tableName, stm);
        }

        int i = 1;
        for (Object p : params) {
            stm.setObject(i++, p);
        }
        ResultSet rs = null;
        boolean exists = false;
        TableRow existing = null;
        try {
            rs = stm.executeQuery();
            exists = rs.next();
            if (exists) {
                existing = new TableRow();
                existing.setTable(tableName);
                for (ColumnMetadata columnMd : getTableColumnMetadata(row.getTable())) {
                    existing.getColumns().add(columnMd.getName());
                    existing.getValues().add(getValueAsString(columnMd, rs.getObject(columnMd.getName())));
                }
            }
        } finally {
            if (rs!=null) rs.close();
            checkAndCloseStatement(stm);
        }

        loadLog.append(", rij bestaat: ").append(exists ? "ja" : "nee");

        return existing;
    }

    public boolean isAlreadyInMetadata(TableRow row, StringBuilder loadLog)
            throws SQLException, ParseException, BrmoException {

        if (connRsgb == null || connRsgb.isClosed()) {
            // if used not-threaded, init() required
            throw new BrmoException("No connection found");
        }

        StringBuilder sql = new StringBuilder("select 1 from ");

        String metadataTable = "herkomst_metadata";
        sql.append(metadataTable);

        String tableName = row.getTable();

        sql.append(" WHERE tabel = ?");

        List params = new ArrayList();

        params.add(tableName);

        List<String> pkColumns = getPrimaryKeys(tableName);
        String waarde = null;
        for (String column : pkColumns) {
            sql.append(" AND kolom = ?");
            params.add(column);

            waarde = getValueFromTableRow(row, column);
        }

        if (waarde != null) {
            sql.append(" AND waarde = ?");
            params.add(waarde);
        }

        PreparedStatement stm = getPreparedStatement(row, sql.toString(), params, null, loadLog);
        ResultSet results = stm.executeQuery();
        int count = getRowCount(results);
        DbUtils.closeQuietly(results);

        return count > 0;
    }

    private boolean deleteFromMetadata(TableRow row, StringBuilder loadLog) throws SQLException, ParseException {
        StringBuilder sql = new StringBuilder("delete from ");

        String metadataTable = "herkomst_metadata";
        sql.append(metadataTable);

        String tableName = row.getTable();

        sql.append(" WHERE tabel = ?");

        List params = new ArrayList();

        params.add(tableName);

        List<String> pkColumns = getPrimaryKeys(tableName);
        String waarde = null;
        for (String column : pkColumns) {
            sql.append(" AND kolom = ?");
            params.add(column);

            waarde = getValueFromTableRow(row, column);
        }

        if (waarde != null) {
            sql.append(" AND waarde = ?");
            params.add(waarde);
        }

        PreparedStatement stm = getPreparedStatement(row, sql.toString(), params, null, loadLog);

        int count = 1; // all ok
        if (alsoExecuteSql) {
            count = stm.executeUpdate();
        }

        return count > 0;
    }

    private boolean createDeleteSql(TableRow row, StringBuilder loadLog) throws SQLException, ParseException {
        doSavePoint(row);

        StringBuilder sql = new StringBuilder("delete from ");

        String tableName = row.getTable();
        sql.append(tableName);

        SortedSet<ColumnMetadata> tableColumnMetadata = null;
        tableColumnMetadata = getTableColumnMetadata(tableName);

        // Where clause using pk columns
        List<String> pkColumns = getPrimaryKeys(tableName);

        int i = 0;
        for (String column : pkColumns) {
            if (i < 1) {
                sql.append(" WHERE " + column + " = ?");
            } else {
                sql.append(" AND " + column + " = ?");
            }
            i++;
        }

        PreparedStatement stm = getPreparedStatement(row, sql.toString(), null, pkColumns, loadLog);;

        int count = 1; // all ok
        if (alsoExecuteSql) {
            count = stm.executeUpdate();
        }

        return count >0;
    }

    private final Map<String, List<String>> primaryKeyCache = new HashMap();

    private List<String> getPrimaryKeys(String tableName) throws SQLException {

        List<String> pks = primaryKeyCache.get(tableName);
        if (pks != null) {
            return pks;
        }

        pks = new ArrayList();

        String origName = tables.get(tableName);

        if(origName == null) {
            throw new IllegalArgumentException("Tabel bestaat niet: " + tableName);
        }
        
        ResultSet set = dbMetadata.getPrimaryKeys(null, geomToJdbc.getSchema(), origName);
        while (set.next()) {
            String column = set.getString("COLUMN_NAME");
            pks.add(column.toLowerCase());
        }

        set.close();
        primaryKeyCache.put(tableName, pks);

        return pks;
    }

    private SortedSet<ColumnMetadata> getTableColumnMetadata(String table)
            throws SQLException {

        table = table.toLowerCase();

        if (!tables.containsKey(table)) {
            return null;
        }

        SortedSet<ColumnMetadata> columnMetadata = (SortedSet<ColumnMetadata>) tableColumns.get(table);
        if (columnMetadata == null) {
            columnMetadata = new TreeSet();
            tableColumns.put(table, columnMetadata);

            ResultSet columnsRs = dbMetadata.getColumns(null, geomToJdbc.getSchema(), tables.get(table), "%");
            while (columnsRs.next()) {
                ColumnMetadata cm = new ColumnMetadata(columnsRs);
                columnMetadata.add(cm);
            }

            columnsRs.close();
        }

        return columnMetadata;
    }

    private ColumnMetadata findColumnMetadata(SortedSet<ColumnMetadata> tableColumnMetadata,
            String columnName) {

        if (tableColumnMetadata == null || tableColumnMetadata.size() < 1) {
            return null;
        }

        for (ColumnMetadata cm : tableColumnMetadata) {
            if (cm.getName().toLowerCase().equals(columnName.toLowerCase())) {
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

    private void doSavePoint(TableRow row) throws SQLException {
        if (!alsoExecuteSql) {
            return;
        }
        if (geomToJdbc.useSavepoints()) {
            if (row.isIgnoreDuplicates()) {
                if (recentSavepoint == null) {
                    recentSavepoint = connRsgb.setSavepoint();
                    log.debug("Created savepoint with id: " + recentSavepoint.getSavepointId());
                } else {
                    log.debug("No need for new savepoint, previous insert caused rollback to recent savepoint with id " + recentSavepoint.getSavepointId());
                }
            } else if (recentSavepoint != null) {
                log.debug("About to insert non-recoverable row, discarding savepoint with id " + recentSavepoint.getSavepointId());
                connRsgb.releaseSavepoint(recentSavepoint);
                recentSavepoint = null;
            }
        }
    }

    private PreparedStatement getPreparedStatement(TableRow row, String sql, List params, List<String> pkColumns, StringBuilder loadLog) throws SQLException, ParseException {
        loadLog.append("\nSQL: ");
        loadLog.append(sql);
        loadLog.append(", params (");
        loadLog.append(params == null ? "" : params.toString());
        loadLog.append(")");
        loadLog.append(", duplikaat negeren (");
        loadLog.append(row.isIgnoreDuplicates() ? "ja" : "nee");
        loadLog.append(")");
        PreparedStatement stmt = null;
        if (sql.contains("select")) {
            stmt = connRsgb.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } else {
            stmt = connRsgb.prepareStatement(sql);
        }
        int counter = 0;
        if (params != null && params.size() > 0) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                stmt.setObject(i + 1, param);
                counter = i + 1;
            }
        }
        if (pkColumns != null && pkColumns.size() > 0) {
            loadLog.append(", pkeys (");
            for (String column : pkColumns) {
                loadLog.append("[");
                loadLog.append(column);
                loadLog.append("=");
                String val = getValueFromTableRow(row, column);
                Object obj = getValueAsObject(row.getTable(), column, val, null);
                loadLog.append(obj == null ? "" : obj.toString());
                loadLog.append("] ");
                stmt.setObject(counter + 1, obj);
                counter++;
            }
            loadLog.append(")");
        }
        return stmt;
    }

    private Object getValueAsObject(String tableName, String column, String value, ColumnMetadata cm) throws SQLException, ParseException {

        if (cm == null) {
            SortedSet<ColumnMetadata> tableColumnMetadata = null;
            tableColumnMetadata = getTableColumnMetadata(tableName);
            cm = findColumnMetadata(tableColumnMetadata, column);
        }

        Object param = null;
        if (cm == null) {
            throw new IllegalArgumentException("Column not found: " + column + " in table " + tableName);
        } else if (cm.getTypeName().equals(geomToJdbc.getGeomTypeName())) {
            param = geomToJdbc.convertToNativeGeometryObject(value);
        } else if (value != null) {
            param = GeometryJdbcConverter.convertToSQLObject(value, cm, tableName, column);
        }

        return param;
    }

    /**
     * Momenteel niet in gebruik
     * ({@link #getTableRowFromDb(nl.b3p.brmo.loader.util.TableRow, java.lang.StringBuilder)}),
     * werkt ook nog niet met geometrie.
     *
     * @see #getTableRowFromDb(nl.b3p.brmo.loader.util.TableRow,
     * java.lang.StringBuilder)
     * @deprecated
     */
    private String getValueAsString(ColumnMetadata cm, Object object) throws SQLException {
        String value = null;
        if (object != null) {
            switch (cm.getDataType()) {
                case Types.DECIMAL:
                case Types.NUMERIC:
                case Types.INTEGER:
                case Types.CHAR:
                case Types.VARCHAR:
                    value = object.toString();
                    break;
                case Types.OTHER:
// het enige dat deze code mogelijk zou doen is een fout opwerpen wat zinloos is omdat er geen niets met `value` wordt gedaan
// related to #94
//                    if (cm.getTypeName().equals("SDO_GEOMETRY") || cm.getTypeName().equals("geometry")) {
//                        // XXX need to convert geometry back to WKT...
//                        throw new UnsupportedOperationException();
//                    } else {
//                        throw new IllegalStateException(String.format("Column \"%s\" (value to convert \"%s\") type other but not geometry!", cm.getName(), object));
//                    }
                case Types.STRUCT:
                // oracle 12 kan een struct voor geometrie geven
                case Types.DATE:
                case Types.TIMESTAMP:
                    //Calendar cal = new GregorianCalendar();
                    //cal.setTime(new Date(((java.sql.Date)object).getTime()));
                    // XXX format calendar
                    //value = format(cal);
                    break;
                default:
                    throw new UnsupportedOperationException(String.format("Data type %s (#%d) of column \"%s\" not supported", cm.getTypeName(), cm.getDataType(), cm.getName()));
            }
        }
        return value;
    }

    private int getRowCount(ResultSet rs) throws SQLException {
        int size = 0;
        try {
            if (rs != null && rs.next()) {
                size = rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw new SQLException("Fout ophalen row count: ", ex);
        }

        return size;
    }
}
