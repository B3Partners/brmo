/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.BRKScannerProces;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Directory scanner for BRK berichten.
 *
 * @author Mark Prins
 */
public class BRKDirectoryScanner extends AbstractExecutableProces {

    private static final Log log = LogFactory.getLog(BRKDirectoryScanner.class);

    BRKScannerProces config;

    public BRKDirectoryScanner(BRKScannerProces config) {
        this.config = config;
    }

    @Override
    public void execute() throws BrmoException {
        switch (config.getStatus()) {
            case NULL:
            case ONBEKEND:
            case WAITING:
                EntityManager em = Stripersist.getEntityManager();
                StringBuilder sb = new StringBuilder();
                String msg = String.format("aanvang run op %tc", Calendar.getInstance());
                sb.append(msg);
                config.setStatus(PROCESSING);
                this.active = true;

                // validatie van de directories, kunnen we lezen en schrijven
                final File scanDirectory = new File(this.config.getScanDirectory());
                if (!scanDirectory.isDirectory() || !scanDirectory.canExecute() || !scanDirectory.canWrite()) {
                    config.setStatus(ERROR);
                    this.active = false;
                    throw new BrmoException(String.format("De scan directory '%s' is geen executable directory", scanDirectory));
                }
                final File archiefDirectory = new File(this.config.getArchiefDirectory());
                archiefDirectory.mkdirs();
                if (!archiefDirectory.isDirectory() || !archiefDirectory.canWrite()) {
                    config.setStatus(ERROR);
                    this.active = false;
                    throw new BrmoException(String.format("De archief directory '%s' is geen beschrijfbare directory", archiefDirectory));
                }

                File files[] = scanDirectory.listFiles();

                for (File f : files) {
                    if (f.isDirectory()) {
                        continue;
                    }
                    msg = String.format("Bestand %s is gevonden in %s.", f, scanDirectory);
                    log.info(msg);
                    sb.append(msg);

                    // controleer of geen duplicaat...
                    Integer hash = duplicaatCode(f);

// TODO check in database of deze hash voorkomt
                    final boolean isDuplicaat = false;

                    String qOrcl = "SELECT FROM Bericht from dual WHERE standard_hash(br_orgineel_xml,'MD5')=" + hash;
                    //boolean notfound = (em.createQuery(qOrcl).getResultList().size() < 1);
                    boolean notfound=true;
                    if (notfound) {
                        // 1: laadt in staging
                        // TODO  brmo.loadFromFile(BrmoFramework.BR_BRK, f.getAbsolutePath());
                        msg = String.format("Bestand %s is geladen.", f);
                        log.info(msg);
                        sb.append(msg);
                        // 2: verplaats naar archief (NB mogelijk platform afhankelijk)
                        f.renameTo(new File(archiefDirectory, f.getName()));
                        msg = String.format("Bestand %s is naar archief verplaatst.", f);
                        log.info(msg);
                        sb.append(msg);
                    } else {
                        msg = String.format("Bestand %s is een duplicaat.", f);
                        log.info(msg);
                        sb.append(msg);
                    }
                }

                msg = String.format("Klaar met run op %tc", Calendar.getInstance());
                sb.append(msg);

                this.active = false;
                config.setStatus(WAITING);
                config.setSamenvatting(sb.toString());
                config.setLastrun(new Date());
                em.persist(config);
                break;
            default:
                log.debug(String.format("De BRK scanner met id %d is niet gestart vanwege de status %s.", config.getId(), config.getStatus()));
        }
    }

    public BRKScannerProces getConfig() {
        return config;
    }
}
