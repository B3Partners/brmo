/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import javax.persistence.Transient;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import nl.b3p.brmo.persistence.staging.BGTLightOphaalProces;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.engine.jdbc.StreamUtils;

/**
 * Ophalen van BGT GML Light formaat bestanden.
 *
 * @author mprins
 */
public class BGTLightOphalenProces extends AbstractExecutableProces {

    private static final Log LOG = LogFactory.getLog(BGTLightOphalenProces.class);

    private final BGTLightOphaalProces config;

    @Transient
    private ProgressUpdateListener listener;

    public BGTLightOphalenProces(BGTLightOphaalProces config) {
        this.config = config;
    }

    @Override
    public void execute() throws BrmoException {
        this.execute(new ProgressUpdateListener() {
            @Override
            public void total(long total) {
            }

            @Override
            public void progress(long progress) {
            }

            @Override
            public void exception(Throwable t) {
                LOG.error(t);
            }

            @Override
            public void updateStatus(String status) {
            }

            @Override
            public void addLog(String log) {
            }
        });
    }

    @Override
    public void execute(ProgressUpdateListener listener) {
        if (isArchivingOK()) {
            // haal lijst uit config
            List<Integer> ids = getGridIDs();
            listener.total(ids.size());
            String bUrl = config.getConfig().get("ophaalurl").getValue();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String enddate = sdf.format(new Date());
            bUrl = bUrl.replace("ENDDATE", enddate);
            String naam;
            long count = 0;
            for (Integer id : ids) {
                // download naar directory
                // code38451_aggrlevel0-20160429.zip
                //"https://www.pdok.nl/download/service/extract.zip?extractset=gmllight&tiles=%7B%22layers%22%3A%5B%7B%22aggregateLevel%22%3A0%2C%22codes%22%3A%5B38451%5D%7D%5D%7D&excludedtypes=plaatsbepalingspunt&history=true&enddate=29-4-2016"
                bUrl = bUrl.replace("GRID_ID", id.toString());
                listener.addLog("Ophalen bestand voor grid cel: " + id);
                naam = id + "_0-" + enddate + " .zip";
                File gmlZip = getBestand(bUrl, naam);
                if (gmlZip != null) {
                    listener.addLog("laden bestand: " + naam);
                    laadBestand(gmlZip);
                }
                listener.progress(count++);
            }
        }
    }

    private void laadBestand(File gmlZip) {
        try {
            DataSource ds = ConfigUtil.getDataSourceStaging();
            BrmoFramework brmo = new BrmoFramework(ds, null);
            brmo.loadFromFile(BrmoFramework.BR_BGTLIGHT, gmlZip.getAbsolutePath(), null
            //  new nl.b3p.brmo.loader.ProgressUpdateListener() {
            //                @Override
            //                public void total(long total) {
            //                    listener.total(total);
            //                }
            //                @Override
            //                public void progress(long progress) {
            //                    listener.progress(progress);
            //                }
            //                @Override
            //                public void exception(Throwable t) {
            //                    listener.exception(t);
            //                }
            //            }
            );
        } catch (BrmoException ex) {
            LOG.error("GML Bestand kon niet worden geladen", ex);
            listener.exception(ex);
        }
    }

    /**
     *
     * @return lijst met op te halen grid ids
     */
    private List<Integer> getGridIDs() {
        ArrayList<Integer> ids = new ArrayList();
        String sIds = config.getConfig().get("gridids").getValue();

        StringTokenizer st = new StringTokenizer(sIds, ",;", false);
        while (st.hasMoreTokens()) {
            try {
                ids.add(Integer.parseInt(st.nextToken().trim()));
            } catch (NumberFormatException ignore) {
                LOG.debug("Niet parsable integer", ignore);
                // ignore unparsable
            }
        }
        return ids;
    }

    /**
     *
     * @param sUrl
     * @param naam
     * @throws MalformedURLException als de url niet geldig is
     * @throws IOException als er geen verbinding kan worden gemaakt of het
     * bestand niet opgeslagen kan worden
     */
    private File getBestand(String sUrl, String naam) {
        File out = null;
        try {
            final URL url = new URL(sUrl);
            URLConnection conn = url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(false);
            conn.setUseCaches(false);
            conn.setDefaultUseCaches(false);
            conn.setConnectTimeout(30 * 1000);
            conn.setReadTimeout(60 * 1000);
            conn.connect();


            InputStream data = conn.getInputStream();
            out = new File(this.config.getArchiefDirectory(), naam);
            FileOutputStream archiveFile = new FileOutputStream(out);
            StreamUtils.copy(data, archiveFile);
        } catch (MalformedURLException ex) {
            LOG.error("Bestand kon niet worden opgehaald", ex);
            listener.exception(ex);
        } catch (FileNotFoundException ex) {
            LOG.error("Bestand kon niet worden opgeslagen", ex);
            listener.exception(ex);
        } catch (IOException ex) {
            LOG.error("Bestand kon niet worden opgehaald", ex);
            listener.exception(ex);
        }
        return out;
    }

    /**
     * Bepaal of de zipfile kan worden opgeslagen in de geconfigureerde
     * directory. Als de directory niet bestaat wordt deze aangemaakt.
     *
     * @return true als we de zipfile lokaal kunnen opslaan.
     */
    private boolean isArchivingOK() {
        final String aDir = this.config.getArchiefDirectory();
        boolean isArchiving = (aDir != null);
        if (isArchiving) {
            final File archiefDirectory = new File(aDir);
            try {
                archiefDirectory.mkdirs();
                if (!archiefDirectory.isDirectory() || !archiefDirectory.canWrite()) {
                    config.setStatus(ERROR);
                    String msg = String.format("FOUT: De archief directory '%s' is geen beschrijfbare directory, zipfiles worden niet gearchiveerd.", archiefDirectory);
                    listener.addLog(msg);
                    config.addLogLine(msg);
                    isArchiving = false;
                }
            } catch (SecurityException e) {
                String msg = String.format("SecurityException voor archief directory '%s', zipfiles worden niet gearchiveerd.", archiefDirectory);
                listener.addLog(msg);
                config.addLogLine(msg);
                LOG.error(msg, e);
                isArchiving = false;
            }
        }
        LOG.debug("Archief directory ingesteld op " + aDir);
        return isArchiving;
    }
}
