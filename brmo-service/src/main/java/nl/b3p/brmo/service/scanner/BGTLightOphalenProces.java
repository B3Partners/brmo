/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import javax.persistence.Transient;
import nl.b3p.brmo.bgt.util.PDOKBGTLightUtil;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.BGTLightOphaalProces;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.engine.jdbc.StreamUtils;
import org.stripesstuff.stripersist.Stripersist;

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
        this.listener = listener;
        config.setStatus(PROCESSING);
        config.setLastrun(new Date());
        Stripersist.getEntityManager().merge(config);
        Stripersist.getEntityManager().flush();

        StringBuilder sb = new StringBuilder(AutomatischProces.LOG_NEWLINE);
        String oldLog = config.getLogfile();
        if (oldLog != null) {
            if (oldLog.length() > OLD_LOG_LENGTH) {
                sb.append(oldLog.substring(oldLog.length() - OLD_LOG_LENGTH / 10));
            } else {
                sb.append(oldLog);
            }
        }

        long totaal = 0, aantalGeladen = 0;

        String msg = String.format("Het BGT GML Light ophalen proces met ID %d is gestart op %tc.", config.getId(), Calendar.getInstance());
        LOG.info(msg);
        listener.addLog(msg);
        sb.append(msg).append(AutomatischProces.LOG_NEWLINE);

        if (isArchivingOK()) {
            config.setLogfile(sb.toString());
            Set<Integer> ids = config.getGridIds();
            if (ids == null || ids.isEmpty()) {
                msg = "Ophalen 'tileinfo.json' van pdok website en bepalen grid cellen";
                listener.updateStatus(msg);
                String ophaalgebiedWKT = config.getOphaalgebied();
                msg = "Ophaalgebied: " + ophaalgebiedWKT;
                sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                listener.addLog(msg);
                LOG.info(msg);
                String tileInfoJsonUrl = config.getTileInfoUrl();
                msg = "BGT tileinfo url: " + tileInfoJsonUrl;
                sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                LOG.info(msg);
                ids = PDOKBGTLightUtil.calculateGridIds(ophaalgebiedWKT, tileInfoJsonUrl);
                msg = "De berekende lijst met op te halen grid cellen is: " + ids;
            } else {
                msg = "De berekende lijst met op te halen grid cellen is: " + ids;
            }
            listener.total(ids.size());
            sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
            LOG.info(msg);
            listener.addLog(msg);
            listener.updateStatus(msg);

            String bUrl = config.getOphaalUrl();
            LOG.info("BGT GML light ophaal basis url: " + bUrl);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String enddate = sdf.format(new Date());
            bUrl = bUrl.replace("ENDDATE", enddate);
            String naam;
            String sUrl;

            BrmoFramework brmo = null;
            for (Integer id : ids) {
                // download naar directory
                // code38451_aggrlevel0-20160429.zip
                //"https://www.pdok.nl/download/service/extract.zip?extractset=gmllight&tiles=%7B%22layers%22%3A%5B%7B%22aggregateLevel%22%3A0%2C%22codes%22%3A%5B38451%5D%7D%5D%7D&excludedtypes=plaatsbepalingspunt&history=true&enddate=29-4-2016"
                sUrl = bUrl.replace("GRID_ID", id.toString());
                msg = "Ophalen/downloaden bestand voor grid id: " + id + " aggr. nivo 0";
                listener.updateStatus(msg);
                LOG.info(msg);
                listener.addLog(msg);
                sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                naam = id + "_0-" + enddate + ".zip";
                File gmlZip = getBestand(sUrl, naam, sb);

                if (gmlZip != null) {
                    msg = "Laden bestand: " + naam;
                    listener.updateStatus(msg);
                    LOG.info(msg);
                    listener.addLog(msg);
                    sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                    try {
                        brmo = new BrmoFramework(ConfigUtil.getDataSourceStaging(), null);
                        brmo.loadFromFile(BrmoFramework.BR_BGTLIGHT, gmlZip.getAbsolutePath(), null);
                        aantalGeladen++;
                    } catch (BrmoException ex) {
                        msg = "GML Bestand kon niet worden geladen.";
                        sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                        LOG.error(msg, ex);
                        config.setSamenvatting("Er is een fout opgetreden, details staan in de logs.");
                        listener.exception(ex);
                    } finally {
                        if (brmo != null) {
                            brmo.closeBrmoFramework();
                        }
                    }
                }
                listener.progress(++totaal);
            }

            msg = "Er zijn " + aantalGeladen + " bestanden opgehaald en geladen.";
            sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
            listener.addLog("\n\n**** resultaat ****\n");
            listener.addLog(msg);
            config.setStatus(WAITING);
            config.setSamenvatting(msg);
        } else {
            msg = "FOUT: Download bestanden kunnen niet worden opgeslagen/weggeschreven.";
            LOG.error(msg);
            sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
            listener.updateStatus(msg);
            listener.addLog(msg);
            config.setStatus(ERROR);
            config.setSamenvatting(msg);
        }

        msg = String.format("Het BGT GML Light ophalen proces met ID %d is afgerond op %tc", config.getId(), Calendar.getInstance());

        LOG.info(msg);
        listener.updateStatus(msg);
        listener.addLog("\n" + msg);
        sb.append(msg).append(AutomatischProces.LOG_NEWLINE);

        config.setLogfile(sb.toString());

        Stripersist.getEntityManager().merge(config);
        Stripersist.getEntityManager().flush();
        Stripersist.getEntityManager().getTransaction().commit();
        Stripersist.getEntityManager().clear();
    }

    /**
     * download bestand.
     *
     * @param sUrl op te halen url
     * @param naam van de zipfile
     * @throws MalformedURLException als de url niet geldig is
     * @throws IOException als er geen verbinding kan worden gemaakt of het
     * bestand niet opgeslagen kan worden
     */
    private File getBestand(String sUrl, String naam, StringBuilder log) {
        File out = new File(this.config.getArchiefDirectory(), naam);

        if (isDuplicaatLaadProces(out, BrmoFramework.BR_BGTLIGHT)) {
            String msg = "Duplicaat bestand " + naam + " wordt niet (opnieuw) opgehaald of geladen.";
            LOG.info(msg);
            log.append(msg).append(AutomatischProces.LOG_NEWLINE);
            listener.addLog(msg);
            return null;
        }

        try {
            LOG.debug("ophalen bestand: " + sUrl);
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
            FileOutputStream archiveFile = new FileOutputStream(out);
            StreamUtils.copy(data, archiveFile);
        } catch (IOException ex) {
            LOG.error("Bestand kon niet worden opgehaald of opgeslagen.", ex);
            log.append("Bestand kon niet worden opgehaald of opgeslagen: ").append(ex.getLocalizedMessage()).append(AutomatischProces.LOG_NEWLINE);
            config.setSamenvatting("Er is een fout opgetreden, details staan in de logs.");
            config.setStatus(ERROR);
            listener.exception(ex);
        }
        return out;
    }

    /**
     * Bepaal of de zipfile kan worden opgeslagen in de geconfigureerde
     * directory. Als de directory niet bestaat wordt geprobeerd deze aan te
     * maken.
     *
     * @return true als we de zipfile lokaal kunnen opslaan.
     */
    private boolean isArchivingOK() {
        LOG.debug("Controle archief directory.");
        final String aDir = this.config.getArchiefDirectory();
        boolean isArchiving = (aDir != null);
        if (isArchiving) {
            final File archiefDirectory = new File(aDir);
            try {
                archiefDirectory.mkdirs();
                if (!archiefDirectory.isDirectory() || !archiefDirectory.canWrite()) {
                    config.setStatus(ERROR);
                    String msg = String.format("FOUT: De archief directory '%s' is geen beschrijfbare directory, zipfiles kunnen niet worden niet opgeslagen.", archiefDirectory);
                    LOG.error(msg);
                    listener.addLog(msg);
                    config.addLogLine(msg);
                    isArchiving = false;
                }
            } catch (SecurityException e) {
                String msg = String.format("SecurityException voor archief directory '%s', zipfiles kunnen niet worden niet opgeslagen.", archiefDirectory);
                listener.addLog(msg);
                config.addLogLine(msg);
                LOG.error(msg, e);
                isArchiving = false;
                listener.exception(e);
            }
        }
        LOG.debug("Archief directory ingesteld op " + aDir);
        return isArchiving;
    }
}
