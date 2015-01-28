package nl.b3p.brmo.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.RsgbTransformer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * BRMO framework
 *
 * @author Boy de Wit
 */
public class BrmoFramework {

    private static final Log log = LogFactory.getLog(BrmoFramework.class);

    public static final String BR_BAG = "bag";
    public static final String BR_BRK = "brk";

    public static final String XSL_BRK = "/xsl/brk-snapshot-to-rsgb-xml.xsl";
    public static final String XSL_BAG = "/xsl/bag-mutatie-to-rsgb-xml.xsl";

    public static final String LAADPROCES_TABEL = "laadproces";
    public static final String BERICHT_TABLE = "bericht";

    private StagingProxy stagingProxy = null;
    private DataSource dataSourceRsgb = null;

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
        Thread t = new Thread(rsgbProxy);
        t.start();
        return t;
    }

    public Thread toRsgb(Long laadProcesId) throws BrmoException  {
        return toRsgb(laadProcesId, null);
    }

    public Thread toRsgb(Long laadProcesId, ProgressUpdateListener listener) throws BrmoException  {
        RsgbProxy rsgbProxy = new RsgbProxy(dataSourceRsgb, stagingProxy, laadProcesId, listener);
        Thread t = new Thread(rsgbProxy);
        t.start();
        return t;
    }

    public Thread toRsgb(long[] ids) throws BrmoException  {
        return toRsgb(ids, null);
    }

    public Thread toRsgb(long[] ids, ProgressUpdateListener listener) throws BrmoException  {
        RsgbProxy rsgbProxy = new RsgbProxy(dataSourceRsgb, stagingProxy, ids, listener);
        Thread t = new Thread(rsgbProxy);
        t.start();
        return t;
    }
/*
    // XXX methode wordt niet gebruikt
    public void toDbXml(Long id) throws BrmoException  {
        try {
            LaadProces lp = stagingProxy.getLaadProcesById(id);
            String brType = lp.getSoort();

            List<Bericht> berichten = stagingProxy.getBerichtenByLaadProcesId(id);

            RsgbTransformer transformer = getTransformer(brType);

            stagingProxy.updateBerichtenDbXml(berichten, transformer);

        } catch (Exception ex) {
            throw new BrmoException(ex);
        }
    }
*/
    public void delete(Long id) throws BrmoException{
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

    public void loadFromFile(String type, String fileName) throws BrmoException {
        try {
            loadFromFile(type, fileName, null);
        } catch(Exception e) {
            throw new BrmoException("Fout bij loaden basisregistratie gegevens", e);
        }
    }

    public void loadFromFile(String type, String fileName, final ProgressUpdateListener listener) throws BrmoException {
        try {
            if(fileName.toLowerCase().endsWith(".zip")) {
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
                    while(entry != null && !entry.getName().toLowerCase().endsWith(".xml")) {
                        log.warn("Overslaan zip entry geen XML: " + entry.getName());
                        entry = zip.getNextEntry();
                    }
                    if(entry == null) {
                        throw new BrmoException("Geen geschikt XML bestand gevonden in zip bestand " + fileName);
                    }
                    log.info("Lezen XML bestand uit zip: " + entry.getName());
                    stagingProxy.loadBr(zip, type, fileName, null);
                } catch(Exception e) {
                    if(e instanceof BrmoException) {
                        throw e;
                    } else {
                        throw new BrmoException(e);
                    }
                } finally {
                    if(zip != null) {
                        zip.close();
                    }
                }
            } else {
                File f = new File(fileName);
                if(listener != null) {
                    listener.total(f.length());
                }
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(fileName);
                    stagingProxy.loadBr(fis, type, fileName, listener);
                } finally {
                    IOUtils.closeQuietly(fis);
                }
            }
        } catch(Exception e) {
            throw new BrmoException("Fout bij loaden basisregistratie gegevens", e);
        }
    }

    public void loadFromStream(String type, InputStream stream, String fileName) throws BrmoException {
        try {
            stagingProxy.loadBr(stream, type, fileName, null);
        } catch(Exception e) {
            throw new BrmoException("Fout bij loaden basisregistratie gegevens", e);
        }
    }

    public void loadFromStream(String type, InputStream stream, String fileName, ProgressUpdateListener listener) throws BrmoException {
        try {
            stagingProxy.loadBr(stream, type, fileName, listener);
        } catch(Exception e) {
            throw new BrmoException("Fout bij loaden basisregistratie gegevens", e);
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
/*
    public List<Bericht> getBerichtenByLaadProcesId(long id) throws BrmoException {
        try {
            return stagingProxy.getBerichtenByLaadProcesId(id);
        } catch (SQLException ex) {
            throw new BrmoException(ex);
        }
    }
*/
    public Bericht getBerichtById(long id) throws BrmoException {
        try {
            return stagingProxy.getBerichtById(id);
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

    /* XXX methode alleen gebruikt in test
    public boolean rowInRsgbMetadata(TableRow row) throws BrmoException {
        try {
            StringBuilder dummy = new StringBuilder();
            RsgbProxy rsgbProxy = new RsgbProxy(dataSourceRsgb, stagingProxy);
            rsgbProxy.init();
            return rsgbProxy.isAlreadyInMetadata(row, dummy);
        } catch (Exception ex) {
            throw new BrmoException(ex);
        }
    }*/

    private RsgbTransformer getTransformer(String brType) throws BrmoException {
        try {
            RsgbTransformer transformer = null;
            if (brType.equalsIgnoreCase(BrmoFramework.BR_BRK)) {
                transformer = new RsgbTransformer(BrmoFramework.XSL_BRK);
            } else if (brType.equalsIgnoreCase(BrmoFramework.BR_BAG)) {
                transformer = new RsgbTransformer(BrmoFramework.XSL_BAG);
            }
            return transformer;
        } catch (Exception ex) {
            throw new BrmoException(ex);
        }

    }
}
