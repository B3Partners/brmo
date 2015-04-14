/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.persistence.NoResultException;
import nl.b3p.brmo.loader.entity.BagBericht;

import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.xml.BagMutatieXMLReader;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.Bericht;
import nl.b3p.brmo.persistence.staging.LaadProces;
import nl.b3p.brmo.persistence.staging.WebMirrorBAGScannerProces;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class WebMirrorBAGDirectoryScanner extends AbstractExecutableProces {

    private static final Log log = LogFactory.getLog(WebMirrorBAGDirectoryScanner.class);

    /**
     * true als we de bericht lokaal opslaan.
     */
    private boolean isArchiving;

    private File archiefDirectory;

    /**
     * proces data.
     */
    private final WebMirrorBAGScannerProces config;

    private ProgressUpdateListener listener;

    public WebMirrorBAGDirectoryScanner(WebMirrorBAGScannerProces config) {
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
        config.setStatus(AutomatischProces.ProcessingStatus.PROCESSING);
        Stripersist.getEntityManager().flush();

        final String aDir = this.config.getArchiefDirectory();
        isArchiving = (aDir != null);
        if (isArchiving) {
            archiefDirectory = new File(aDir);
            archiefDirectory.mkdirs();
            if (!archiefDirectory.isDirectory() || !archiefDirectory.canWrite()) {
                config.setStatus(ERROR);
                config.addLogLine(String.format("FOUT: De archief directory '%s' is geen beschrijfbare directory", archiefDirectory));
                config.setSamenvatting("Er is een fout opgetreden, details staan in de logs");
            }
        }
        try {
            String expression = this.config.getConfig().get("csspath").getValue();
            // http get page
            String url = this.config.getScanDirectory();

            msg = String.format("Ophalen van de lijst van links van %s.", url);
            listener.addLog(msg);
            config.addLogLine(msg);

            msg = "Ophalen en parsen van de lijst.";
            listener.addLog(msg);
            config.addLogLine(msg);
            Document doc = Jsoup.connect(url).timeout(5000).followRedirects(true).ignoreHttpErrors(false).get();
            msg = "De lijst is succesvol opgehaald.";
            listener.updateStatus(msg);

            Elements links = doc.select(expression);
            int items = links.size();
            listener.total(items);

            // XXX
            // TODO dit is alleen voor ontwikkel om het aantal urls te beperken to een handvol
            if (log.isDebugEnabled()) {
                items = 6;
            }
            // XXX

            String berichtUrl, bestandsnaam;
            int progress = 0, aantalGeladen = 0, filterAlVerwerkt = 0;
            for (int i = 0; i < items; i++) {
                berichtUrl = links.get(i).attr("abs:href");
                bestandsnaam = links.get(i).text();

                if (isBestandAlGeladen(berichtUrl)) {
                    filterAlVerwerkt++;
                    msg = bestandsnaam + " is al verwerkt.";
                    listener.addLog(msg);
                } else {
                    // store bericht
                    laadBestand(berichtUrl, bestandsnaam, archiefDirectory);
                    aantalGeladen++;
                }
                listener.progress(++progress);
            }

            msg = String.format("Klaar met run op %tc.", Calendar.getInstance());
            listener.updateStatus(msg);
            listener.addLog(msg);
            listener.addLog("\n**** resultaat ****\n");
            listener.addLog("Aantal url's die al waren verwerkt: " + filterAlVerwerkt);
            listener.addLog("Aantal url's geladen: " + aantalGeladen + "\n");

            config.updateSamenvattingEnLogfile(msg + "\nAantal url's die al waren verwerkt: "
                    + filterAlVerwerkt + "\nAantal url's geladen: " + aantalGeladen);

            config.setStatus(WAITING);
            this.config.setLastrun(new Date());
            Stripersist.getEntityManager().merge(this.config);
            Stripersist.getEntityManager().getTransaction().commit();
        } catch (MalformedURLException ex) {
            log.error(ex);
            config.addLogLine(ex.getLocalizedMessage());
            listener.exception(ex);
        } catch (HttpStatusException ex) {
            msg = String.format("Er is een fout opgetreden bij het uitlezen van de url %s. Status code %s",
                    ex.getUrl(), ex.getStatusCode());
            log.error(msg);
            config.addLogLine(msg);
            listener.exception(ex);
        } catch (SocketTimeoutException ex) {
            log.error(ex);
            config.addLogLine(ex.getLocalizedMessage());
            listener.exception(ex);
        } catch (IOException ex) {
            log.error(ex);
            config.addLogLine(ex.getLocalizedMessage());
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
            this.config.setStatus(AutomatischProces.ProcessingStatus.ERROR);
        } finally {
            config.setLastrun(new Date());
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

    private boolean isBestandAlGeladen(String naam) {
        try {
            Stripersist.getEntityManager().createQuery("select 1 from LaadProces lp where lp.bestand_naam = :n")
                    .setParameter("n", naam)
                    .getSingleResult();
            log.debug("is al geladen: " + naam);
            return true;
        } catch (NoResultException nre) {
            log.debug("nog niet geladen: " + naam);
            return false;
        }
    }

    /**
     * Laadt de berichten uit de xml documenten in een zipfile.
     *
     * @param sUrl
     * @param naam
     * @param archiefDirectory
     * @throws Exception
     */
    private void laadBestand(String sUrl, String naam, File archiefDirectory) throws Exception {

//        if(log.isDebugEnabled()){
//            if(!sUrl.equalsIgnoreCase("http://mirror.openstreetmap.nl/bag/mutatie/9999MUT02012015-03012015.zip")){
//                return;
//            }
//        }

        String msg = "Downloaden " + sUrl;
        listener.updateStatus(msg);
        listener.addLog(msg);
        config.addLogLine(msg);
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

        byte[] zipFile = IOUtils.toByteArray(conn.getInputStream());

        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipFile));
        ZipEntry entry = zis.getNextEntry();

        if (entry == null) {
            msg = "  Geen geschikt bestand gevonden in download!";
            listener.addLog(msg);
            config.addLogLine(msg);
            return;
        }

        LaadProces lp = new LaadProces();
        lp.setBestand_naam(sUrl);
        lp.setSoort("bag");
        lp.setStatus(LaadProces.STATUS.STAGING_OK);
        lp.setStatus_datum(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        lp.setOpmerking("WebMirror download van " + url + " op " + sdf.format(new Date()));
        lp.setAutomatischProces(Stripersist.getEntityManager().find(AutomatischProces.class, config.getId()));
        // datum uit bestandsnaam halen 9999MUT01012015-02012015.zip
        SimpleDateFormat praseFmt = new SimpleDateFormat("ddMMyyyy");
        String d = naam.substring("9999MUT01012015-".length(), "9999MUT01012015-02012015".length());
        Date date = praseFmt.parse(d);
        lp.setBestand_datum(date);
        Stripersist.getEntityManager().persist(lp);

        while (entry != null) {
            if (!entry.getName().toLowerCase().endsWith(".xml")) {
                msg = "Overslaan zip entry geen XML: " + entry.getName();
                listener.updateStatus(msg);
                listener.addLog(msg);
                config.addLogLine(msg);
                log.info(msg);
            } else {
                msg = String.format("  Lezen XML bestand: %s uit zip", entry.getName());
                listener.updateStatus(msg);
                listener.addLog(msg);
                config.addLogLine(msg);
                log.info(msg);

                // bagreader met een string voeden om voortijdig sluiten van de inputstream te voorkomen
                byte[] xml = IOUtils.toByteArray(zis);
                BagMutatieXMLReader bagreader = new BagMutatieXMLReader(new ByteArrayInputStream(xml));
                if (bagreader.hasNext()) {
                    // het komt voor dat er geen mutaties zijn in de xml
                    while (bagreader.hasNext()) {
                        Bericht b = new Bericht();
                        BagBericht bag = bagreader.next();

                        b.setLaadprocesid(lp);
                        b.setSoort("bag");
                        b.setStatus(Bericht.STATUS.STAGING_OK);
                        b.setStatus_datum(new Date());
                        msg = String.format("Bericht uit bestand %s (zip file: %s)", entry.getName(), naam);
                        b.setOpmerking(msg);
                        listener.updateStatus(msg);

                        b.setBr_xml(bag.getBrXml());
                        b.setVolgordenummer(bag.getVolgordeNummer());
                        b.setObject_ref(bag.getObjectRef());
                        b.setDatum(bag.getDatum());
                        Stripersist.getEntityManager().persist(b);
                    }
                } else {
                    msg = String.format("  Geen mutatie berichten in bestand %s (%d Kb) gevonden.", entry.getName(), entry.getSize()/1024);
                    listener.addLog(msg);
                    config.addLogLine(msg);
                    log.warn(msg);
                }
            }
            entry = zis.getNextEntry();
        }

        if (isArchiving) {
            // TODO store/archive item locally
            File out = new File(archiefDirectory, naam);
            FileUtils.writeByteArrayToFile(out, zipFile);
        }

        IOUtils.closeQuietly(zis);

        Stripersist.getEntityManager().merge(lp);
        Stripersist.getEntityManager().merge(this.config);
        Stripersist.getEntityManager().getTransaction().commit();
    }

}
