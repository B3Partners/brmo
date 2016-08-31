/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.brmo.service.scanner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.Transient;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoDuplicaatLaadprocesException;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.BAGScannerProces;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author mprins
 */
public class BAGDirectoryScanner extends AbstractExecutableProces {

    private static final Log LOG = LogFactory.getLog(BAGDirectoryScanner.class);

    private final BAGScannerProces config;

    @Transient
    private ProgressUpdateListener listener;

    public BAGDirectoryScanner(BAGScannerProces config) {
        this.config = config;
    }

    /**
     *
     * @throws BrmoException als de directory niet lees/blader/schrijfbaar is
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
                sb.append(oldLog).append(AutomatischProces.LOG_NEWLINE);
            }
        }

        config.setStatus(PROCESSING);
        String msg = String.format("De BAG scanner met ID %d is gestart op %tc.", config.getId(), Calendar.getInstance());
        LOG.info(msg);
        listener.updateStatus(msg);
        listener.addLog(msg);
        sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
        this.active = true;

        // validatie van de directories, kunnen we lezen/bladeren en evt. schrijven?
        final File scanDirectory = new File(this.config.getScanDirectory());
        if (!scanDirectory.isDirectory() || !scanDirectory.canExecute()) {
            config.setStatus(ERROR);
            config.setSamenvatting("Er is een fout opgetreden, details staan in de logs");
            this.active = false;
            msg = String.format("De scan directory '%s' is geen executable directory", scanDirectory);
            LOG.info(msg);
            listener.updateStatus(msg);
            listener.addLog(msg);
            this.listener.exception(new BrmoException(msg));
            return;
        }
        final String aDir = this.config.getArchiefDirectory();
        final boolean isArchiving = (aDir != null);
        File archiefDirectory = null;
        if (isArchiving) {
            archiefDirectory = new File(aDir);
            archiefDirectory.mkdirs();
            if (!archiefDirectory.isDirectory() || !archiefDirectory.canWrite()) {
                config.setStatus(ERROR);
                config.setSamenvatting("Er is een fout opgetreden, details staan in de logs");
                this.active = false;
                msg = String.format("De archief directory '%s' is geen beschrijfbare directory", archiefDirectory);
                LOG.error(msg);
                listener.updateStatus(msg);
                listener.addLog(msg);
                this.listener.exception(new BrmoException(msg));
                return;
            }
            if (!scanDirectory.canWrite()) {
                config.setStatus(ERROR);
                config.setSamenvatting("Er is een fout opgetreden, details staan in de logs");
                this.active = false;
                msg = String.format(String.format("De scan directory '%s' is geen beschrijfbare directory", scanDirectory));
                LOG.error(msg);
                listener.updateStatus(msg);
                listener.addLog(msg);
                this.listener.exception(new BrmoException(msg));
                return;
            }
        }

        File files[] = scanDirectory.listFiles();
        Arrays.sort(files, NameFileComparator.NAME_COMPARATOR);

        int filterAlVerwerkt = 0;
        int aantalGeladen = 0;
        int progress = 0;

        if (files.length < 1) {
            msg = String.format("Geen bestanden gevonden in scandirectory: %s.", scanDirectory);
            LOG.info(msg);
            sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
            listener.updateStatus(msg);
            listener.addLog(msg);
        }

        listener.total(files.length);
        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }
            msg = String.format("Bestand %s is gevonden in %s.", f, scanDirectory);
            LOG.info(msg);
            sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
            listener.updateStatus(msg);
            listener.addLog(msg);
            BrmoFramework brmo = null;
            try {
                if (this.isDuplicaatLaadProces(f, BrmoFramework.BR_BAG)) {
                    msg = String.format("Bestand %s is een duplicaat en wordt overgeslagen.", f);
                    LOG.info(msg);
                    sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                    listener.updateStatus(msg);
                    listener.addLog(msg);
                    filterAlVerwerkt++;
                } else {
                    brmo = new BrmoFramework(ConfigUtil.getDataSourceStaging(), null);
                    brmo.loadFromFile(BrmoFramework.BR_BAG, getBestandsNaam(f));
                    msg = String.format("Bestand %s is geladen.", f);
                    LOG.info(msg);
                    sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                    listener.updateStatus(msg);
                    listener.addLog(msg);
                    aantalGeladen++;
                }
            } catch (BrmoDuplicaatLaadprocesException duplicaat) {
                LOG.info(duplicaat.getLocalizedMessage());
                sb.append(duplicaat.getLocalizedMessage()).append(AutomatischProces.LOG_NEWLINE);
                filterAlVerwerkt++;
            } catch (BrmoLeegBestandException leegEx) {
                // log message maar ga door met verwerking, om een "leeg" bestand bericht/laadproces over te slaan
                LOG.warn(leegEx.getLocalizedMessage());
                sb.append(leegEx.getLocalizedMessage()).append(AutomatischProces.LOG_NEWLINE);
            } catch (BrmoException ex) {
                LOG.error(ex.getLocalizedMessage());
                sb.append(ex.getLocalizedMessage()).append(AutomatischProces.LOG_NEWLINE);
            } finally {
                if (brmo != null) {
                    brmo.closeBrmoFramework();
                }
            }

            if (isArchiving) {
                // verplaats naar archief
                try {
                    FileUtils.copyFileToDirectory(f, archiefDirectory);
                    boolean succes = FileUtils.deleteQuietly(f);
                    if (succes) {
                        msg = String.format("  Bestand %s is naar archief %s verplaatst.", f, archiefDirectory);
                    } else {
                        msg = String.format("  Bestand %s is naar archief %s verplaatst, maar origineel kon niet worden verwijderd.", f, archiefDirectory);
                    }
                } catch (IOException e) {
                    msg = String.format("  Bestand %s is NIET naar archief %s verplaatst, oorzaak: (%s).", f, archiefDirectory, e.getLocalizedMessage());
                    LOG.error(msg);
                    listener.updateStatus(msg);
                    listener.addLog(msg);
                }
                LOG.info(msg);
                listener.updateStatus(msg);
                listener.addLog(msg);
                sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
            }
            listener.progress(++progress);
        }

        listener.addLog("\n\n**** resultaat ****");
        listener.addLog("Aantal bestanden die al waren geladen: " + filterAlVerwerkt);
        listener.addLog("Aantal bestanden geladen: " + aantalGeladen + "\n");

        msg = "Aantal bestanden die al waren verwerkt: " + filterAlVerwerkt + ", aantal bestanden geladen: " + aantalGeladen;
        sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
        LOG.info(msg);
        config.setSamenvatting(msg);

        msg = String.format("De BAG scanner met ID %d is afgerond op %tc.", config.getId(), Calendar.getInstance());
        LOG.info(msg);
        listener.updateStatus(msg);
        listener.addLog(msg);
        sb.append(msg).append(AutomatischProces.LOG_NEWLINE);

        this.active = false;
        config.setStatus(WAITING);
        config.setLogfile(sb.toString());

        Stripersist.getEntityManager().merge(config);
    }
}
