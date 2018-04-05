package nl.b3p.brmo.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.updates.UpdateProcess;
import nl.b3p.brmo.loader.util.BGTLightRsgbTransformer;
import nl.b3p.brmo.loader.util.BrmoDuplicaatLaadprocesException;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import nl.b3p.brmo.loader.util.TopNLRsgbTransformer;
import nl.b3p.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.loader.jdbc.GeometryJdbcConverterFactory;
import nl.b3p.topnl.TopNLType;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.io.IOUtils;
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

    public static final String BR_BAG = "bag";
    public static final String BR_BRK = "brk";
    public static final String BR_NHR = "nhr";
    public static final String BR_BGTLIGHT = "bgtlight";
    public static final String BR_TOPNL = "topnl";
    public static final String BR_BRP = "brp";
    public static final String BR_GBAV = "gbav";

    public static final String XSL_BRK = "/xsl/brk-snapshot-to-rsgb-xml.xsl";
    public static final String XSL_BAG = "/xsl/bag-mutatie-to-rsgb-xml.xsl";
    public static final String XSL_NHR = "/xsl/nhr-to-rsgb-xml-2.5.xsl";
    public static final String XSL_BRP = "/xsl/brp-to-rsgb-xml.xsl";
    public static final String XSL_GBAV = "/xsl/gbav-to-rsgb-xml.xsl";

    public static final String LAADPROCES_TABEL = "laadproces";
    public static final String BERICHT_TABLE = "bericht";
    public static final String JOB_TABLE = "job";
    public static final String BRMO_METADATA_TABEL = "brmo_metadata";

    private StagingProxy stagingProxy = null;
    private DataSource dataSourceRsgb = null;
    private DataSource dataSourceRsgbBgt = null;
    private DataSource dataSourceStaging = null;
    private DataSource dataSourceTopNL = null;

    private boolean enablePipeline = false;
    private Integer pipelineCapacity;
    private boolean renewConnectionAfterCommit = false;
    private boolean orderBerichten = true;
    private String errorState = null;

    public BrmoFramework(DataSource dataSourceStaging, DataSource dataSourceRsgb) throws BrmoException {
        if (dataSourceStaging != null) {
            try {
                stagingProxy = new StagingProxy(dataSourceStaging);
            } catch (SQLException ex) {
                throw new BrmoException(ex);
            }
        }
        this.dataSourceRsgb = dataSourceRsgb;
    }

    public BrmoFramework(DataSource dataSourceStaging, DataSource dataSourceRsgb, DataSource dataSourceRsgbBgt) throws BrmoException {
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
    }

    public BrmoFramework(DataSource dataSourceStaging, DataSource dataSourceRsgb, DataSource dataSourceRsgbBgt, DataSource dataSourceTopNL) throws BrmoException {
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
        this.dataSourceTopNL = dataSourceTopNL;
    }

    public void setDataSourceRsgbBgt(DataSource dataSourceRsgbBgt) {
        this.dataSourceRsgbBgt = dataSourceRsgbBgt;
    }

    public void setDataSourceTopNL (DataSource dataSourceTopNL) {
        this.dataSourceTopNL = dataSourceTopNL;
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

    private String getVersion(DataSource dataSource) throws SQLException {
        String sql = "SELECT waarde FROM " + BrmoFramework.BRMO_METADATA_TABEL + " WHERE naam = 'brmoversie'";
        final Connection c = dataSource.getConnection();
        GeometryJdbcConverter geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(c);
        Object o = new QueryRunner(geomToJdbc.isPmdKnownBroken()).query(c, sql, new ScalarHandler());
        DbUtils.closeQuietly(c);
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
        if (stagingProxy!=null) {
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
        return toRsgb((ProgressUpdateListener)null);
    }

    public Thread toRsgb(ProgressUpdateListener listener) throws BrmoException {
        return toRsgb(Bericht.STATUS.STAGING_OK, listener);
    }

    public Thread toRsgb(Bericht.STATUS status, ProgressUpdateListener listener) throws BrmoException {
        RsgbProxy rsgbProxy = new RsgbProxy(dataSourceRsgb, stagingProxy, status, listener);
        rsgbProxy.setEnablePipeline(enablePipeline);
        if(pipelineCapacity != null) {
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
     *
     * @param mode geeft aan wat ids zijn (laadprocessen of berichten)
     * @param ids array van ids
     * @param listener voortgangs listener
     * @return running transformatie thread
     * @throws BrmoException if any
     */
    public Thread toRsgb(RsgbProxy.BerichtSelectMode mode, long[] ids, ProgressUpdateListener listener) throws BrmoException {
        String soort = "";
        if (mode == RsgbProxy.BerichtSelectMode.BY_LAADPROCES) {
            try {
                soort = stagingProxy.getLaadProcesById(ids[0]).getSoort();
            } catch (SQLException ex) {
                throw new BrmoException(ex);
            }
        }

        Runnable worker;
        if (soort.equalsIgnoreCase(BR_BGTLIGHT)) {
            // filter soort, als bgt dan als proces verwerken, niet als berichtenset
            worker = new BGTLightRsgbTransformer(dataSourceRsgbBgt, stagingProxy, ids, listener);
            ((BGTLightRsgbTransformer) worker).setLoadingUpdate(this.orderBerichten);
        } else if(TopNLType.isTopNLType(soort)){
            try{
                worker = new TopNLRsgbTransformer(dataSourceTopNL, stagingProxy, ids, listener);
            } catch (JAXBException | SQLException ex) {
                throw new BrmoException("Probleem met topparser initialiseren: ", ex);
            }
        }else{
            worker = new RsgbProxy(dataSourceRsgb, stagingProxy, mode, ids, listener);
            ((RsgbProxy) worker).setEnablePipeline(enablePipeline);
            if (pipelineCapacity != null) {
                ((RsgbProxy) worker).setPipelineCapacity(pipelineCapacity);
            }
            ((RsgbProxy) worker).setRenewConnectionAfterCommit(renewConnectionAfterCommit);
            ((RsgbProxy) worker).setOrderBerichten(orderBerichten);
            ((RsgbProxy) worker).setErrorState(errorState);
        }
        Thread t = new Thread(worker);
        t.start();
        return t;
    }

    public Thread toRsgb(UpdateProcess updateProcess, ProgressUpdateListener listener) throws BrmoException  {
        RsgbProxy rsgbProxy = new RsgbProxy(dataSourceRsgb, stagingProxy, updateProcess, listener);
        rsgbProxy.setEnablePipeline(enablePipeline);
        if(pipelineCapacity != null) {
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
     * @see #loadFromFile(java.lang.String, java.lang.String,
     * nl.b3p.brmo.loader.ProgressUpdateListener)
     * @param type basis registratie type
     * @param fileName bestand
     * @throws BrmoException als er een fout optreed tijdens verwerking
     */
    public void loadFromFile(String type, String fileName) throws BrmoException {
        try {
            loadFromFile(type, fileName, null);
        } catch(Exception e) {
            if (e instanceof BrmoException) {
                throw (BrmoException)e;
            } else {
                throw new BrmoException("Fout bij loaden basisregistratie gegevens", e);
            }
        }
    }

    /**
     * NB na gebruik zelf de database verbinding sluiten / opruimen met {@link #closeBrmoFramework()}
     *
     * @param type basis registratie type
     * @param fileName bestand
     * @param listener voortgangsmonitor
     * @throws BrmoException als er een fout optreed tijdens verwerking
     */
    public void loadFromFile(String type, String fileName, final ProgressUpdateListener listener) throws BrmoException {
        try {
            if (fileName.toLowerCase().endsWith(".zip") && !type.equalsIgnoreCase(BR_BGTLIGHT)) {
                log.info("Openen ZIP bestand " + fileName);
                ZipInputStream zip = null;
                try {
                    File f = new File(fileName);
                    if(listener != null) {
                        listener.total(f.length());
                    }
                    CountingInputStream zipCis = new CountingInputStream(new FileInputStream(f)) {
                        @Override
                        protected void afterRead(int n) {
                            super.afterRead(n);
                            if(listener != null) {
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
                            stagingProxy.loadBr(new CloseShieldInputStream(zip), type, fileName + "/" + entry.getName(), null, null);
                        }
                        entry = zip.getNextEntry();
                    }
                } finally {
                    if(zip != null) {
                        zip.close();
                    }
                }
                log.info("Klaar met ZIP bestand " + fileName);
            } else {
                File f = new File(fileName);
                if(listener != null) {
                    listener.total(f.length());
                }
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(fileName);
                    stagingProxy.loadBr(fis, type, fileName, null, listener);
                } finally {
                    IOUtils.closeQuietly(fis);
                }
            }
        } catch(Exception e) {
            if (e instanceof BrmoException) {
                throw (BrmoException)e;
            } else {
                throw new BrmoException("Fout bij loaden basisregistratie gegevens", e);
            }
        }
    }

    public void loadFromStream(String type, InputStream stream, String fileName) throws BrmoException {
        try {
            stagingProxy.loadBr(stream, type, fileName, null, null);
        } catch(Exception e) {
            throw new BrmoException("Fout bij loaden basisregistratie gegevens", e);
        }
    }

    public void loadFromStream(String type, InputStream stream, String fileName, Date d) throws BrmoException {
        try {
            stagingProxy.loadBr(stream, type, fileName, d, null);
        } catch(Exception e) {
            throw new BrmoException("Fout bij loaden basisregistratie gegevens", e);
        }
    }

    /**
     *
     * @param type type registratie, bijv. {@value BrmoFramework#BR_BRK}
     * @param stream datastream
     * @param fileName te gebruiken bestandsnaam om laadproces te identificeren
     * @param listener mag {@code null} zijn
     * @throws BrmoException als er een algemene fout optreed
     * @throws BrmoDuplicaatLaadprocesException als het "bestand"
     * {@code fileName} al geladen is
     * @throws BrmoLeegBestandException als het "bestand" {@code fileName} leeg
     * is
     */
    public void loadFromStream(String type, InputStream stream, String fileName, ProgressUpdateListener listener)
            throws BrmoException, BrmoDuplicaatLaadprocesException, BrmoLeegBestandException {
        try {
            stagingProxy.loadBr(stream, type, fileName, null, listener);
        } catch (Exception e) {
            if (e instanceof BrmoDuplicaatLaadprocesException) {
                throw (BrmoDuplicaatLaadprocesException)e;
            }
            if (e instanceof BrmoLeegBestandException) {
                throw (BrmoLeegBestandException)e;
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

    public List<Bericht> getBerichten(int page, int start, int limit, String sort,
            String dir, String filterSoort, String filterStatus) throws BrmoException  {

        try {
            return stagingProxy.getBerichten(page, start, limit, sort, dir, filterSoort, filterStatus);
        } catch (SQLException ex) {
            throw new BrmoException(ex);
        }
    }

    public List<LaadProces> getLaadprocessen(int page, int start, int limit, String sort,
            String dir, String filterSoort, String filterStatus) throws BrmoException  {

        try {
            return stagingProxy.getLaadprocessen(page, start, limit, sort, dir, filterSoort, filterStatus);
        } catch (SQLException ex) {
            throw new BrmoException(ex);
        }
    }

    public Long[] getLaadProcessenIds(String sort, String dir, String filterSoort, String filterStatus) throws BrmoException {
        try {
            return stagingProxy.getLaadProcessenIds(sort, dir, filterSoort, filterStatus);
        } catch (SQLException ex) {
            throw new BrmoException(ex);
        }
    }

    public long getCountBerichten(String sort, String dir, String filterSoort, String filterStatus) throws BrmoException {
        try {
            return stagingProxy.getCountBerichten(sort, dir, filterSoort, filterStatus);
        } catch (SQLException ex) {
             throw new BrmoException(ex);
        }
    }

    public long getCountLaadProcessen(String sort, String dir, String filterSoort, String filterStatus) throws BrmoException {
        try {
            return stagingProxy.getCountLaadProces(sort, dir, filterSoort, filterStatus);
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
}
