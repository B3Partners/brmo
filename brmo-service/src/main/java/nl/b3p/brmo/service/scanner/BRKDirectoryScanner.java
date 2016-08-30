/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.Transient;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.BrkBericht;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.xml.BrkSnapshotXMLReader;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.BRKScannerProces;
import nl.b3p.brmo.persistence.staging.Bericht;
import nl.b3p.brmo.persistence.staging.ClobElement;
import nl.b3p.brmo.persistence.staging.LaadProces;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Directory scanner for BRK berichten in xml formaat.
 *
 * @author Mark Prins
 */
public class BRKDirectoryScanner extends AbstractExecutableProces {

    private static final Log log = LogFactory.getLog(BRKDirectoryScanner.class);

    private final BRKScannerProces config;

    private final int defaultCommitPageSize = 1000;

    @Transient
    private ProgressUpdateListener listener;

    public BRKDirectoryScanner(BRKScannerProces config) {
        this.config = config;
    }

    /**
     * Leest de xml berichten uit de directory in in de database, indien
     * geconfigureerd worden bestanden gearchiveerd.
     *
     * @throws BrmoException als de directory niet lees/blader/schrijfbaar is
     *
     */
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
        config.setStatus(PROCESSING);
        StringBuilder sb = new StringBuilder(AutomatischProces.LOG_NEWLINE);
        String oldLog = config.getLogfile();
        if (oldLog!=null) {
            if (oldLog.length() > OLD_LOG_LENGTH) {
                sb.append(oldLog.substring(oldLog.length() - OLD_LOG_LENGTH / 10));
            } else {
                sb.append(oldLog);
            }
        }

        String msg = String.format("De BRK scanner met ID %d is gestart op %tc.", config.getId(), Calendar.getInstance());
        log.info(msg);
        listener.addLog(msg);
        sb.append(msg);

        // validatie van de directories, kunnen we lezen/bladeren en evt. schrijven?
        final File scanDirectory = new File(this.config.getScanDirectory());
        if (!scanDirectory.isDirectory() || !scanDirectory.canExecute()) {
            config.setStatus(ERROR);
            msg = String.format("De scan directory '%s' is geen executable directory", scanDirectory);
            config.setLogfile(msg);
            config.setSamenvatting("Er is een fout opgetreden, details staan in de logs");
            this.listener.exception(new BrmoException(msg));
            return;
        }
        // validatie archief directory
        final String aDir = this.config.getArchiefDirectory();
        File archiefDirectory = null;
        if (aDir != null) {
            archiefDirectory = new File(aDir);
            archiefDirectory.mkdirs();
            if (!archiefDirectory.isDirectory() || !archiefDirectory.canWrite()) {
                archiefDirectory = null;
                config.setStatus(ERROR);
                msg = String.format("De archief directory '%s' is geen beschrijfbare directory, er worden geen bestanden gearchiveerd.",
                        archiefDirectory);
                sb.append(msg);
                config.setSamenvatting("Er is een fout opgetreden, details staan in de logs");
                log.error(msg);
                this.listener.exception(new BrmoException(msg));
            }
            if (!scanDirectory.canWrite()) {
                archiefDirectory = null;
                config.setStatus(ERROR);
                msg = String.format("De scan directory '%s' is geen beschrijfbare directory, er worden geen bestanden gearchiveerd.",
                        scanDirectory);
                sb.append(msg);
                config.setSamenvatting("Er is een fout opgetreden, details staan in de logs");
                log.error(msg);
                this.listener.exception(new BrmoException(msg));
            }
        }
        config.setLogfile(sb.toString());

        File files[] = scanDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".xml");
            }
        });
        Arrays.sort(files, NameFileComparator.NAME_COMPARATOR);

        processXMLFiles(files, scanDirectory, archiefDirectory);

        Stripersist.getEntityManager().flush();
        Stripersist.getEntityManager().getTransaction().commit();
        Stripersist.getEntityManager().clear();
    }

    /**
     * verwerk een bestandenlijst.
     *
     * @param files array met xml bestanden
     *
     * @param scanDirectory
     * @param archiefDirectory
     */
    private void processXMLFiles(File[] files, File scanDirectory, File archiefDirectory) {
        StringBuilder sb = new StringBuilder(AutomatischProces.LOG_NEWLINE + config.getLogfile());
        String msg;
        int filterAlVerwerkt = 0;
        int aantalGeladen = 0;
        int progress = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        int commitPageSize = this.getCommitPageSize();

        listener.total(files.length);
        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }
            msg = String.format("Bestand %s is gevonden in %s.", f, scanDirectory);
            log.info(msg);
            listener.addLog(msg);
            sb.append(AutomatischProces.LOG_NEWLINE).append(msg).append(AutomatischProces.LOG_NEWLINE);
            if (this.isDuplicaatLaadProces(f, BrmoFramework.BR_BRK)) {
                msg = String.format("  Bestand %s is een duplicaat en wordt overgeslagen.", f);
                listener.addLog(msg);
                log.info(msg);
                sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                filterAlVerwerkt++;
            } else {
                try {
                    LaadProces lp = new LaadProces();
                    lp.setBestand_naam(getBestandsNaam(f));
                    lp.setBestand_datum(getBestandsDatum(f));
                    lp.setSoort(BrmoFramework.BR_BRK);
                    lp.setStatus(LaadProces.STATUS.STAGING_OK);
                    lp.setOpmerking(String.format("Bestand geladen van %s op %s", f.getAbsolutePath(), sdf.format(new Date())));
                    lp.setAutomatischProces(Stripersist.getEntityManager().find(AutomatischProces.class, config.getId()));

                    Bericht b = new Bericht();
                    b.setLaadprocesid(lp);
                    b.setDatum(lp.getBestand_datum());
                    b.setSoort(BrmoFramework.BR_BRK);
                    b.setStatus_datum(new Date());
                    String fileContent = FileUtils.readFileToString(f, "UTF-8");
                    fileContent = fileContent.replaceAll("\u0000.*", "");
                    b.setBr_orgineel_xml(fileContent);

                    try {
                        BrkSnapshotXMLReader reader = new BrkSnapshotXMLReader(
                                new ByteArrayInputStream(b.getBr_orgineel_xml().getBytes("UTF-8")));
                        BrkBericht bericht = reader.next();

                        if (bericht.getDatum() != null) {
                            b.setDatum(bericht.getDatum());
                        }
                        b.setBr_xml(bericht.getBrXml());
                        b.setVolgordenummer(bericht.getVolgordeNummer());

                        //Als objectRef niet opgehaald kan worden,dan kan het
                        //  bericht niet verwerkt worden.
                        String objectRef = bericht.getObjectRef();
                        if (objectRef != null && !objectRef.isEmpty()) {
                            b.setObject_ref(bericht.getObjectRef());
                            b.setStatus(Bericht.STATUS.STAGING_OK);
                            b.setOpmerking("Klaar voor verwerking.");
                        } else {
                            b.setStatus(Bericht.STATUS.STAGING_NOK);
                            b.setOpmerking("Object Ref niet gevonden in bericht-xml, neem contact op met leverancier.");
                        }
                    } catch (Exception e) {
                        b.setStatus(Bericht.STATUS.STAGING_NOK);
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        msg = String.format("Fout bij parsen BRK bericht (bestand %s): %s ", f, sw.toString());
                        b.setOpmerking(msg);
                        log.warn(msg);
                    }

                    Stripersist.getEntityManager().persist(lp);
                    Stripersist.getEntityManager().persist(b);
                    Stripersist.getEntityManager().merge(this.config);

                    aantalGeladen++;
                    msg = String.format("  Bestand %s is geladen en heeft status: %s. (Leverde bericht id: %s, met status: %s.)", f, lp.getStatus(), b.getId(), b.getStatus());
                    log.info(msg);
                    this.listener.addLog(msg);
                    sb.append(msg).append(AutomatischProces.LOG_NEWLINE);

                    if (aantalGeladen % commitPageSize == 0) {
                        log.info("Tussentijds opslaan van berichten, 'commitPageSize' is bereikt");
                        Stripersist.getEntityManager().flush();
                        Stripersist.getEntityManager().getTransaction().commit();
                        Stripersist.getEntityManager().clear();
                    }

                } catch (IOException io) {
                    // mogelijk bij openen en lezen xml bestand
                    log.error(io.getLocalizedMessage());
                    sb.append(io.getLocalizedMessage()).append(AutomatischProces.LOG_NEWLINE);
                    listener.exception(io);
                }
            }
            listener.progress(++progress);

            if (archiefDirectory != null) {
                // verplaats naar archief
                try {
                    FileUtils.copyFileToDirectory(f, archiefDirectory);
                    boolean succes = FileUtils.deleteQuietly(f);
                    if (succes) {
                        msg = String.format("  Bestand %s is naar archief %s verplaatst.", f, archiefDirectory);
                    } else {
                        msg = String.format("  Bestand %s is naar archief %s verplaatst, maar origineel kon niet verwijderd worden.", f, archiefDirectory); 
                    }
                } catch (IOException e) {
                    msg = String.format("  Bestand %s is NIET naar archief %s verplaatst, oorzaak: (%s).", f, archiefDirectory, e.getLocalizedMessage());
                    log.error(msg);
                }
            }
            log.info(msg);
            this.listener.addLog(msg);
            sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
        }
        msg = String.format("Klaar met run op %tc", Calendar.getInstance());
        log.info(msg);
        listener.updateStatus(msg);
        listener.addLog(msg);
        sb.append(msg);

        listener.addLog("\n\n**** resultaat ****");
        listener.addLog("\nAantal bestanden die al waren geladen: " + filterAlVerwerkt);
        listener.addLog("\nAantal bestanden geladen: " + aantalGeladen + "\n");

        config.setStatus(WAITING);
        config.setLogfile(sb.toString());
        config.setLastrun(new Date());
        config.updateSamenvattingEnLogfile("Aantal bestanden die al waren verwerkt: "
                + filterAlVerwerkt + AutomatischProces.LOG_NEWLINE
                + "Aantal bestanden geladen: " + aantalGeladen + AutomatischProces.LOG_NEWLINE);
        Stripersist.getEntityManager().merge(config);
    }

    private int getCommitPageSize() {
        int commitPageSize;
        try {
            String s = ClobElement.nullSafeGet(config.getConfig().get("commitPageSize"));
            commitPageSize = Integer.parseInt(s);
            if (commitPageSize < 1 || commitPageSize > defaultCommitPageSize) {
                commitPageSize = defaultCommitPageSize;
            }
        } catch (NumberFormatException nfe) {
            commitPageSize = defaultCommitPageSize;
        }

        log.debug("Instellen van commit page size op: " + commitPageSize);
        return commitPageSize;
    }
}
