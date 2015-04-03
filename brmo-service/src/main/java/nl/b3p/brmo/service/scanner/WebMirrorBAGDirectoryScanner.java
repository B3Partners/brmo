/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.xml.BagMutatieXMLReader;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.Bericht;
import nl.b3p.brmo.persistence.staging.LaadProces;
import nl.b3p.brmo.persistence.staging.WebMirrorBAGScannerProces;
import org.apache.commons.io.IOUtils;
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
        File archiefDirectory = null;
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

            if(log.isDebugEnabled()){
                items = 10;
            }
            // for each item get item if item unknown
            String berichtUrl, bestandsnaam;
            int progress = 0, aantalGeladen = 0, filterAlVerwerkt = 0;
            for (int i = 0; i < items; i++) {
                // berichtUrl = (nodeList.item(i).getFirstChild().getNodeValue());
                berichtUrl = links.get(i).attr("abs:href");
                bestandsnaam = links.get(i).text();

                log.debug(berichtUrl);
                log.debug(bestandsnaam);

                if (isBestandAlGeladen(berichtUrl)) {
                    filterAlVerwerkt++;
                } else {
                    // store bericht
                    laadBestand(berichtUrl, bestandsnaam);
                    aantalGeladen++;
                }
                listener.progress(++progress);
            }

            msg = String.format("Klaar met run op %tc.", Calendar.getInstance());
            listener.updateStatus(msg);
            listener.addLog(msg);
            config.addLogLine(msg);
            config.updateSamenvattingEnLogfile("TODO");
            config.setStatus(WAITING);

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
        } finally {
            config.setLastrun(new Date());
            Stripersist.getEntityManager().flush();
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

    private void laadBestand(String url, String naam) throws MalformedURLException, IOException {
        String msg = "Downloaden " + url;
        listener.updateStatus(msg);
        listener.addLog(msg);
        config.addLogLine(msg);

        URLConnection connection = new URL(url).openConnection();
        InputStream input = (InputStream) connection.getContent();

        LaadProces lp = new LaadProces();
        lp.setBestand_naam(url);
        lp.setSoort("bak");
        lp.setStatus(LaadProces.STATUS.STAGING_OK);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        lp.setOpmerking("WebMirror download van " + url + " op " + sdf.format(new Date()));
        lp.setAutomatischProces(Stripersist.getEntityManager().find(AutomatischProces.class, config.getId()));

        // TODO lp.setBestand_datum(null);

        Bericht b = new Bericht();
        b.setLaadprocesid(lp);
        b.setDatum(lp.getBestand_datum());
        b.setSoort("bag");
        b.setStatus(Bericht.STATUS.STAGING_OK);
        b.setStatus_datum(new Date());
        ZipInputStream zip = new ZipInputStream(input);
        ZipEntry entry = zip.getNextEntry();

        BagMutatieXMLReader bagreader = new BagMutatieXMLReader();

        while(entry != null && !entry.getName().toLowerCase().endsWith(".xml")) {
                        log.warn("Overslaan zip entry geen XML: " + entry.getName());
                        entry = zip.getNextEntry();
                    }
                    if(entry == null) {
                        throw new BrmoException("Geen geschikt XML bestand gevonden in zip bestand " + fileName);
                    }
                    log.info("Lezen XML bestand uit zip: " + entry.getName());
                    stagingProxy.loadBr(zip, type, fileName, null);
    }


        while (entry != null && !entry.getName().toLowerCase().endsWith(".xml")) {
            msg = "Overslaan zip entry geen XML: " + entry.getName();
            listener.addLog(msg);
            config.addLogLine(msg);
            entry = zip.getNextEntry();
        }
        if (entry == null) {
            msg = "Geen geschikt XML bestand gevonden in zip bestand!";
            listener.addLog(msg);
            config.addLogLine(msg);
            return;
        }
        b.setBr_xml(IOUtils.toString(zip, "UTF-8"));

        if (isArchiving) {
            // store/archive item locally
            // TODO saveToFile();
        }

        Stripersist.getEntityManager().persist(lp);
        Stripersist.getEntityManager().persist(b);
        Stripersist.getEntityManager().merge(this.config);
        Stripersist.getEntityManager().getTransaction().commit();
    }
}
