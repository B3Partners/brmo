/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.brmo.service.scanner;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoDuplicaatLaadprocesException;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.BAGScannerProces;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class BAGDirectoryScanner extends AbstractExecutableProces {

    private static final Log log = LogFactory.getLog(BAGDirectoryScanner.class);

    private BAGScannerProces config;

    public BAGDirectoryScanner(BAGScannerProces config) {
        this.config = config;
    }

    /**
     * @deprecated deze methode gebruikt nog de
     * BrmoFramework(ConfigUtil.getDataSourceStaging(), null) methode, maar moet
     * over op JPA natuurlijk
     *
     * @throws BrmoException als de directory niet lees/blader/schrijfbaar is
     */
    @Override
    public void execute() throws BrmoException {
        switch (config.getStatus()) {
            case NULL:
            case ONBEKEND:
            case WAITING:
                StringBuilder sb = new StringBuilder();
                config.setStatus(PROCESSING);
                String msg = String.format("De BAG scanner met ID %d is gestart op %tc.", config.getId(), Calendar.getInstance());
                log.info(msg);
                sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                this.active = true;

                // validatie van de directories, kunnen we lezen/bladeren en evt. schrijven?
                final File scanDirectory = new File(this.config.getScanDirectory());
                if (!scanDirectory.isDirectory() || !scanDirectory.canExecute() || !scanDirectory.canWrite()) {
                    config.setStatus(ERROR);
                    this.active = false;
                    throw new BrmoException(String.format("De scan directory '%s' is geen executable directory", scanDirectory));
                }
                final String aDir = this.config.getArchiefDirectory();
                final boolean isArchiving = (aDir != null);
                File archiefDirectory = null;
                if (isArchiving) {
                    archiefDirectory = new File(aDir);
                    archiefDirectory.mkdirs();
                    if (!archiefDirectory.isDirectory() || !archiefDirectory.canWrite()) {
                        config.setStatus(ERROR);
                        this.active = false;
                        throw new BrmoException(String.format("De archief directory '%s' is geen beschrijfbare directory", archiefDirectory));
                    }
                }

                File files[] = scanDirectory.listFiles();
                for (File f : files) {
                    if (f.isDirectory()) {
                        continue;
                    }
                    msg = String.format("Bestand %s is gevonden in %s.", f, scanDirectory);
                    log.info(msg);
                    sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                    if (this.isDuplicaatLaadProces(f, BrmoFramework.BR_BAG)) {
                        msg = String.format("Bestand %s is een duplicaat en wordt overgeslagen.", f);
                        log.info(msg);
                        sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                    } else {
                        try {
                            // 1: laadt in staging.
                            // TODO gebruik JPA
                            BrmoFramework brmo = new BrmoFramework(ConfigUtil.getDataSourceStaging(), null);
                            brmo.loadFromFile(BrmoFramework.BR_BAG, f.getAbsolutePath());
                            msg = String.format("Bestand %s is geladen.", f);
                            log.info(msg);
                            sb.append(msg).append(AutomatischProces.LOG_NEWLINE);

                            if (isArchiving) {
                                // 2: verplaats naar archief (NB mogelijk platform afhankelijk)
                                f.renameTo(new File(archiefDirectory, f.getName()));
                                msg = String.format("Bestand %s is naar archief %s verplaatst.", f, archiefDirectory);
                                log.info(msg);
                                sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
                            }
                        } catch (BrmoDuplicaatLaadprocesException duplicaat) {
                            log.info(duplicaat.getLocalizedMessage());
                        }
                    }
                }
                msg = String.format("Klaar met run op %tc", Calendar.getInstance());
                log.info(msg);
                sb.append(msg);
                this.active = false;
                config.setStatus(WAITING);
                config.updateSamenvattingEnLogfile(sb.toString());
                config.setLastrun(new Date());
                break;
            default:
                log.info(String.format("De BAG scanner met ID %d is niet gestart vanwege de status %s.", config.getId(), config.getStatus()));
        }
    }

}
