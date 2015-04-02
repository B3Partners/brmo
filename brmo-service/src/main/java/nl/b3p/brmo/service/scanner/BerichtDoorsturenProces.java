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


            String id = ClobElement.nullSafeGet(config.getConfig().get("gds2_ophaalproces_id"));
            GDS2OphaalProces proces = Stripersist.getEntityManager().find(GDS2OphaalProces.class, Long.parseLong(id));

            List berichten = Stripersist.getEntityManager().createQuery("select b from Bericht b join b.laadprocesid l where b.status in ('STAGING_OK', 'STAGING_NOK') and l.automatischProces = :proces")
                    .setParameter("proces", proces)
                    .getResultList();

            if(berichten.isEmpty()) {
                this.config.setSamenvatting("Geen berichten om door te sturen");
            } else {
                this.l.total(berichten.size());

                String url = ClobElement.nullSafeGet(proces.getConfig().get("delivery_endpoint"));

                if(url == null) {
                    this.config.setSamenvatting("GDS2 ophaal proces heeft geen afleveringsendpoint");
                } else {
                    int doorgestuurd = 0, fouten = 0;
                    for(Object o: berichten) {
                        Bericht b = (Bericht)o;
                        if(GDS2OphalenProces.doorsturenBericht(proces, l, (Bericht)b, url)) {
                            doorgestuurd++;
                        } else {
                            fouten++;
                        }
                        Stripersist.getEntityManager().merge(b);
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
            if(Stripersist.getEntityManager().getTransaction().getRollbackOnly()) {
                // XXX bij rollback only wordt status niet naar ERROR gezet vanwege
                // rollback, zou in aparte transactie moeten
                Stripersist.getEntityManager().getTransaction().rollback();
            } else {
                Stripersist.getEntityManager().merge(this.config);
                Stripersist.getEntityManager().getTransaction().commit();
            }
        }
    }
}
