/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.Transient;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.TopNLScannerProces;
import nl.b3p.brmo.persistence.staging.ClobElement;
import nl.b3p.brmo.persistence.staging.LaadProces;
import static nl.b3p.topnl.TopNLType.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Directory scanner for topNL 10/50/100/250 files.
 *
 * @author Meine Toonen
 */
public class TopNLDirectoryScanner extends AbstractExecutableProces {

    private static final Log LOG = LogFactory.getLog(TopNLDirectoryScanner.class);

    private final TopNLScannerProces config;
    private final int defaultCommitPageSize = 1000;
    @Transient
    private ProgressUpdateListener listener;
    
    @Transient
    private Integer filterAlVerwerkt = 0;
    @Transient
    private Integer aantalGeladen = 0;
    @Transient
    private Integer progress = 0;

    public static final String[] subdirectoryNames = {TOP10NL.getType(), TOP50NL.getType(), TOP100NL.getType(), TOP250NL.getType()};

    public TopNLDirectoryScanner(TopNLScannerProces config) {
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
        StringBuilder sb = new StringBuilder(AutomatischProces.LOG_NEWLINE);
        String oldLog = config.getLogfile();
        if (oldLog != null) {
            if (oldLog.length() > OLD_LOG_LENGTH) {
                sb.append(oldLog.substring(oldLog.length() - OLD_LOG_LENGTH / 10));
            } else {
                sb.append(oldLog);
            }
        }

        String msg = String.format("De TopNL scanner met ID %d is gestart op %tc.", config.getId(), Calendar.getInstance());
        LOG.info(msg);
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

        config.setLogfile(sb.toString());
        int total = 0;
        Map<String, List<File>> filesPerDir = new HashMap<>();
        FilenameFilter ff = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".gml");
            }
        };
        for (String topNLDir : subdirectoryNames) {
            File subdir = new File(scanDirectory, topNLDir);
           // File files[] = subdir.listFiles();
            List<File> fs = getFilesFromDirectory(subdir, ff);
            total += fs.size();
            filesPerDir.put(topNLDir, fs);
        }
        
        listener.total(total);
        for (String topNLDir : subdirectoryNames) {
            List<File> files = filesPerDir.get(topNLDir);
            Collections.sort(files);
            
            processTopNLDirectory(files, scanDirectory, topNLDir);
        }
        
        Stripersist.getEntityManager().flush();
        Stripersist.getEntityManager().getTransaction().commit();
        Stripersist.getEntityManager().clear();
    }
    
    private List<File> getFilesFromDirectory(File dir, FilenameFilter ff){
        List<File> files = new ArrayList<>();
        File[] fs = dir.listFiles(ff);
        files.addAll(Arrays.asList(fs));
        
        File[] dirs = dir.listFiles();
        for (File d : dirs) {
            if(d.isDirectory()){
                files.addAll(getFilesFromDirectory(d, ff));
            }
        }
        
        return files;
    }

    /**
     * verwerk een bestandenlijst.
     *
     * @param files array met xml bestanden
     *
     * @param scanDirectory
     * @param archiefDirectory
     */
    private void processTopNLDirectory(List<File> files, File scanDirectory, String soort ) {
        StringBuilder sb = new StringBuilder(AutomatischProces.LOG_NEWLINE + config.getLogfile());
        String msg;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        int commitPageSize = this.getCommitPageSize();

        msg = String.format("Laden van TopNL type %s.", soort);
        LOG.info(msg);
        
        listener.addLog(msg);
        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }
            msg = String.format("Bestand %s is gevonden in %s.", f, scanDirectory);
            LOG.info(msg);
            listener.addLog(msg);
            sb.append(AutomatischProces.LOG_NEWLINE).append(msg).append(AutomatischProces.LOG_NEWLINE);
           if (this.isDuplicaatLaadProces(f, soort)) {
                msg = String.format("  Bestand %s is een duplicaat en wordt overgeslagen.", f);
                listener.addLog(msg);
                LOG.info(msg);
                sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                filterAlVerwerkt++;
            } else {
                LaadProces lp = new LaadProces();
                lp.setBestand_naam(getBestandsNaam(f));
                lp.setBestand_datum(getBestandsDatum(f));
                lp.setSoort(soort);
                lp.setStatus(LaadProces.STATUS.STAGING_OK);
                lp.setOpmerking(String.format("Type %s bestand geladen van %s op %s",soort, f.getAbsolutePath(), sdf.format(new Date())));
                lp.setAutomatischProces(Stripersist.getEntityManager().find(AutomatischProces.class, config.getId()));
                Stripersist.getEntityManager().persist(lp);
                Stripersist.getEntityManager().merge(this.config);

                aantalGeladen++;
                msg = String.format("  Bestand %s is geladen en heeft status: %s. En is van soort: %s", f, lp.getStatus(), soort);
                LOG.info(msg);
                this.listener.addLog(msg);
                sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                if (aantalGeladen % commitPageSize == 0) {
                    LOG.debug("Tussentijds opslaan van berichten, 'commitPageSize' is bereikt");
                    Stripersist.getEntityManager().flush();
                    Stripersist.getEntityManager().getTransaction().commit();
                    Stripersist.getEntityManager().clear();
                }
            }
            listener.progress( ++progress);
        }
        msg = String.format("Klaar met run op %tc", Calendar.getInstance());
        LOG.info(msg);
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

        LOG.debug("Instellen van commit page size op: " + commitPageSize);
        return commitPageSize;
    }
}
