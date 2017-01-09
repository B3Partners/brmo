/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoDuplicaatLaadprocesException;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.WebMirrorBAGScannerProces;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Proces dat een web directory uitleest voor BAG mutaties in zipfiles.
 *
 * @author mprins
 */
public class WebMirrorBAGDirectoryScanner extends AbstractExecutableProces {

    private static final Log log = LogFactory.getLog(WebMirrorBAGDirectoryScanner.class);

    /**
     * proces data.
     */
    private final WebMirrorBAGScannerProces config;

    private ProgressUpdateListener listener;

    private BrmoFramework brmo = null;

    private int progress = 0, aantalZipGeladen = 0, aantalXMLGeladen = 0, filterXMLAlVerwerkt = 0;

    public WebMirrorBAGDirectoryScanner(WebMirrorBAGScannerProces config) {
        this.config = config;
        try {
            brmo = new BrmoFramework(ConfigUtil.getDataSourceStaging(), null);
        } catch (BrmoException ex) {
            log.fatal("Initialisatie van BRMO framework is mislukt", ex);
        }
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
                log.error(t);
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

        String msg = String.format("Initialiseren... %tc", new Date());
        listener.updateStatus(msg);
        listener.addLog(msg);
        config.addLogLine(msg);
        config.setStatus(AutomatischProces.ProcessingStatus.PROCESSING);
        Stripersist.getEntityManager().flush();

        try {
            String url = this.config.getScanDirectory();
            msg = String.format("Ophalen van de lijst van links van %s.", url);
            listener.addLog(msg);
            config.addLogLine(msg);
            Document doc = Jsoup.connect(url).timeout(5000).followRedirects(true).ignoreHttpErrors(false).get();
            msg = "De lijst met download links is succesvol opgehaald.";
            listener.updateStatus(msg);
            listener.addLog(msg);
            config.addLogLine(msg);

            String expression = this.config.getConfig().get("csspath").getValue();
            Elements links = doc.select(expression);

            // verwijder een eventuele parent directory link uit de set links
            URI uri = new URI(url);
            URI parent = uri.getPath().endsWith("/") ? uri.resolve("..") : uri.resolve(".");
            if (parent.toString().equalsIgnoreCase(links.first().attr("abs:href"))) {
                msg = String.format("Overslaan van (parent) link %s", parent);
                listener.updateStatus(msg);
                listener.addLog(msg);
                config.addLogLine(msg);
                log.info(msg);
                links.remove(links.first());
            }
            int items = links.size();
            listener.total(items);

            String berichtUrl, bestandsnaam;
            progress = 0;
            filterXMLAlVerwerkt = 0;
            aantalZipGeladen = 0;
            aantalXMLGeladen = 0;
            for (int i = 0; i < items; i++) {
                berichtUrl = links.get(i).attr("abs:href");
                bestandsnaam = links.get(i).text();
                laadBestand(berichtUrl, bestandsnaam);
                aantalZipGeladen++;
                listener.progress(++progress);
            }

            msg = String.format("Klaar met run op %tc.", Calendar.getInstance());
            listener.updateStatus(msg);
            listener.addLog(msg);
            listener.addLog("\n**** resultaat ****\n");
            listener.addLog("Aantal url's gedownloaded: " + aantalZipGeladen);
            listener.addLog("Aantal xml's geladen: " + aantalXMLGeladen);
            listener.addLog("Aantal xml's die al waren geladen: " + filterXMLAlVerwerkt);

            config.updateSamenvattingEnLogfile(msg
                    + "\nAantal url's gedownloaded: " + aantalZipGeladen
                    + "\nAantal xml's geladen: " + aantalXMLGeladen
                    + "\nAantal xml's die al waren geladen: " + filterXMLAlVerwerkt
            );
            config.setStatus(WAITING);
            this.config.setLastrun(new Date());
            Stripersist.getEntityManager().merge(this.config);
            Stripersist.getEntityManager().getTransaction().commit();
        } catch (MalformedURLException | SocketTimeoutException ex) {
            log.error(ex);
            config.addLogLine(ex.getLocalizedMessage());
            config.setStatus(ERROR);
            listener.exception(ex);
        } catch (HttpStatusException ex) {
            msg = String.format("Er is een fout opgetreden bij het uitlezen van de url %s. Status code %s",
                    ex.getUrl(), ex.getStatusCode());
            log.error(msg);
            config.addLogLine(msg);
            config.setStatus(ERROR);
            listener.exception(ex);
        } catch (IOException ex) {
            log.error(ex);
            config.addLogLine(ex.getLocalizedMessage());
            config.setStatus(ERROR);
            listener.exception(ex);
        } catch (Exception ex) {
            log.error(ex);
            config.addLogLine(ex.getLocalizedMessage());
            listener.exception(ex);
            String m = "Fout bij inladen van berichten: " + ExceptionUtils.getMessage(ex);
            if (ex.getCause() != null) {
                m += ", oorzaak: " + ExceptionUtils.getRootCauseMessage(ex);
            }
            log.error(m, ex);
            this.config.updateSamenvattingEnLogfile(m);
            config.setStatus(ERROR);
        } finally {
            config.setLastrun(new Date());
            Stripersist.getEntityManager().merge(this.config);
            if (Stripersist.getEntityManager().getTransaction().getRollbackOnly()) {
                // XXX bij rollback only wordt status niet naar ERROR gezet vanwege
                // rollback, zou in aparte transactie moeten
                Stripersist.getEntityManager().getTransaction().rollback();
            } else {
                Stripersist.getEntityManager().merge(this.config);
                Stripersist.getEntityManager().getTransaction().commit();
            }
        }
    }

    /**
     * Laadt de berichten uit de xml documenten in een zipfile.
     *
     * @param sUrl te downloaden url van zip file
     * @param naam te gebruiken naam voor zip file
     * @throws Exception if any
     */
    private void laadBestand(String sUrl, final String naam) throws Exception {
        String msg = "Downloaden " + sUrl;
        listener.updateStatus(msg);
        listener.addLog(msg);
        log.info(msg);

        final URL url = new URL(sUrl);
        URLConnection conn = url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(false);
        conn.setUseCaches(false);
        conn.setDefaultUseCaches(false);
        conn.setConnectTimeout(30 * 1000);
        conn.setReadTimeout(60 * 1000);
        conn.connect();

        InputStream zipData = conn.getInputStream();
        if (isArchivingOK()) {
            FileOutputStream archiveFile = new FileOutputStream(
                    new File(this.config.getArchiefDirectory(), naam));
            zipData = new TeeInputStream(zipData, archiveFile, true);
        }

        ZipInputStream zis = new ZipInputStream(zipData);
        ZipEntry entry = zis.getNextEntry();
        if (entry == null) {
            msg = String.format("  Geen geschikt bestand gevonden in download %s.", sUrl);
            listener.addLog(msg);
            return;
        }

        while (entry != null) {
            if (!entry.getName().toLowerCase().endsWith(".xml")) {
                msg = "Overslaan zip entry geen XML: " + entry.getName();
                listener.updateStatus(msg);
                listener.addLog(msg);
                log.info(msg);
            } else {
                msg = String.format("  Lezen XML bestand: %s uit zip", entry.getName());
                listener.updateStatus(msg);
                listener.addLog(msg);
                log.info(msg);
                final String bestandNaam = naam + "/" + entry.getName();

                try {
                    brmo.loadFromStream(BrmoFramework.BR_BAG, new CloseShieldInputStream(zis), bestandNaam,
                            new nl.b3p.brmo.loader.ProgressUpdateListener() {
                                @Override
                                public void total(long total) {
                                    listener.addLog(total + " berichten gelezen uit " + bestandNaam);
                                    log.info(total + " berichten gelezen uit " + bestandNaam);
                                }

                                @Override
                                public void progress(long progress) {
                                }

                                @Override
                        public void exception(Throwable t) {
                            listener.exception(t);
                        }
                    });
                    this.aantalXMLGeladen++;
                } catch (BrmoDuplicaatLaadprocesException dup) {
                    this.filterXMLAlVerwerkt++;
                    msg = dup.getLocalizedMessage();
                    listener.updateStatus(msg);
                    listener.addLog(msg);
                    log.info(msg);
                } catch (BrmoLeegBestandException ex) {
                    this.aantalXMLGeladen++;
                    msg = ex.getLocalizedMessage();
                    listener.updateStatus(msg);
                    listener.addLog(msg);
                    log.info(msg);
                }
            }
            entry = zis.getNextEntry();
        }
        IOUtils.closeQuietly(zis);
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
                log.error(msg, e);
                isArchiving = false;
            }
        }
        log.debug("Archief directory ingesteld op " + aDir);
        return isArchiving;
    }
}
