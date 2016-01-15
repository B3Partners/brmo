package nl.b3p.brmo.service.scanner;

import java.util.Date;
import java.util.List;
import javax.persistence.Transient;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import nl.b3p.brmo.persistence.staging.Bericht;
import nl.b3p.brmo.persistence.staging.BerichtDoorstuurProces;
import nl.b3p.brmo.persistence.staging.ClobElement;
import nl.b3p.brmo.persistence.staging.GDS2OphaalProces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Matthijs Laan
 */
public class BerichtDoorsturenProces extends AbstractExecutableProces {

    private static final Log log = LogFactory.getLog(BerichtDoorsturenProces.class);

    private final BerichtDoorstuurProces config;

    private final int defaultCommitPageSize = 1000;

    @Transient
    private ProgressUpdateListener l;

    BerichtDoorsturenProces(BerichtDoorstuurProces config) {
        this.config = config;
    }

    @Override
    public void execute() throws BrmoException {
        this.execute(new ProgressUpdateListener() {
            /* doet bijna niks listener */
            @Override
            public void total(long total) {
                config.addLogLine("Totaal aantal opgehaalde berichten: " + total);
            }

            @Override
            public void progress(long progress) {
            }

            @Override
            public void exception(Throwable t) {
                config.addLogLine("FOUT:" + t.getLocalizedMessage());
                log.error(t);
            }

            @Override
            public void updateStatus(String status) {
            }

            @Override
            public void addLog(String log) {
                config.addLogLine(log);
            }
        });
    }

    @Override
    public void execute(ProgressUpdateListener listener) {
        this.l = listener;
        try {
            l.updateStatus("Laden berichten...");
            this.config.setStatus(AutomatischProces.ProcessingStatus.PROCESSING);

            int commitPageSize = this.getCommitPageSize();

            String id = ClobElement.nullSafeGet(config.getConfig().get("gds2_ophaalproces_id"));
            GDS2OphaalProces proces = Stripersist.getEntityManager().find(GDS2OphaalProces.class, Long.parseLong(id));

            List<Long> berichtIDs = Stripersist.getEntityManager().createQuery("select b.id from Bericht b join b.laadprocesid l where b.status in ('STAGING_OK', 'STAGING_NOK') and l.automatischProces = :proces")
                    .setParameter("proces", proces)
                    .getResultList();

            if (berichtIDs.isEmpty()) {
                this.config.setSamenvatting("Geen berichten om door te sturen");
            } else {
                this.l.total(berichtIDs.size());
                log.info(String.format("Er zijn %s berichten om door te sturen.", berichtIDs.size()));

                String url = ClobElement.nullSafeGet(proces.getConfig().get("delivery_endpoint"));

                if (url == null) {
                    this.config.setSamenvatting("GDS2 ophaal proces heeft geen afleveringsendpoint");
                    log.warn("GDS2 ophaal proces heeft geen afleveringsendpoint.");
                } else {
                    int doorgestuurd = 0, fouten = 0, verwerkt = 0;
                    for (Long pkid : berichtIDs) {
                        Bericht b = Stripersist.getEntityManager().find(Bericht.class, pkid);
                        if (GDS2OphalenProces.doorsturenBericht(proces, l, b, url)) {
                            doorgestuurd++;
                            this.l.progress(doorgestuurd);
                        } else {
                            fouten++;
                        }
                        Stripersist.getEntityManager().merge(b);
                        verwerkt++;
                        if (verwerkt % commitPageSize == 0) {
                            log.info(String.format("Tussentijds opslaan van berichten, 'commitPageSize' (%s) is bereikt, totaal aantal verwerkt : %s",
                                    commitPageSize, verwerkt));
                            Stripersist.getEntityManager().flush();
                            Stripersist.getEntityManager().getTransaction().commit();
                            Stripersist.getEntityManager().clear();
                        }
                    }
                    this.config.setSamenvatting("Berichten doorgestuurd: " + doorgestuurd + ", fouten: " + fouten);
                }
            }

            l.updateStatus(this.config.getSamenvatting());

            this.config.setStatus(AutomatischProces.ProcessingStatus.WAITING);
            this.config.setLastrun(new Date());
        } catch (Exception e) {
            this.config.setLastrun(new Date());
            this.config.setSamenvatting("Er is een fout opgetreden, details staan in de logs");
            this.config.setStatus(AutomatischProces.ProcessingStatus.ERROR);
            l.exception(e);
        } finally {
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
