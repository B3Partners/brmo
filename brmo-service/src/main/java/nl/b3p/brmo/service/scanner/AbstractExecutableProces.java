/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.io.File;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import nl.b3p.brmo.persistence.staging.BAGScannerProces;
import nl.b3p.brmo.persistence.staging.BRKScannerProces;
import nl.b3p.brmo.persistence.staging.LaadProces;
import nl.b3p.brmo.persistence.staging.MailRapportageProces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Abstract to make sure comparisons are done right.
 *
 * @author Mark Prins
 */
public abstract class AbstractExecutableProces implements ProcesExecutable {

    private static final Log log = LogFactory.getLog(AbstractExecutableProces.class);
    
    volatile boolean active = false;

    /**
     * newline string ({@value LOG_NEWLINE}) voor de logberichten.
     */
    public static final String LOG_NEWLINE = "\n";

    /**
     * ProcesExecutable factory.
     *
     * @param config the type of {@code ProcesExecutable} to create.
     * @return an instance of the specified type
     *
     *
     */
    public static ProcesExecutable getProces(AutomatischProces config) {
        // in java 7 zou deze switch met een string kunnen worden uitgevoerd, nu is er een hulp enum nodig
        ProcessingImple imple = ProcessingImple.valueOf(config.getClass().getSimpleName());
        switch (imple) {
            case BAGScannerProces:
                return new BAGDirectoryScanner((BAGScannerProces) config);
            case BRKScannerProces:
                return new BRKDirectoryScanner((BRKScannerProces) config);
            case MailRapportageProces:
                return new MailRapportage((MailRapportageProces) config);
            default:
                throw new IllegalArgumentException(imple.name() + " is is geen ondersteund proces...");
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isRunning() {
        return this.active;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void stop() {
        this.active = false;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void run() {
        while (active) {
            try {
                this.execute();
                // TODO
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            } catch (BrmoException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    /**
     * bepaal of het bestand een duplicaat is op basis van de bestandsnaam en soort.
     *
     * De flow in
     *
     * loadFromFile(bericht)
     * -> stagingProxy.loadBr(InputStream stream, String type, String fileName,...)
     * -> snapshot reader van de input stream parsed het bericht in een BrkSnapshotXMLReader
     *       of BagMutatieXMLReader die bericht voor bericht uitgelezen kunnen worden
     * -> bepaal of laadproces bestaat stagingProxy.laadProcesExists(filenaam/datum)
     * -> laadproces in database maken stagingProxy.writeLaadProces(bestand_naam/bestand_datum/soort/gebied/opmerking/status/status_datum/contact_email)
     * -> uitlezen xml bericht als
     * -> !stagingProxy.berichtExists(laadprocesid/object_ref/datum/volgordenummer)
     * -> stagingProxy.writeBericht(b)
     *
     * @param input een input bestand
     * @param soort het type registratie, bijvoorbeeld
     * {@code BrmoFramework.BR_BRK}
     *
     * @return {@code true} als het bestand een duplicaat betreft, anders
     * {@code false}
     *
     */
    protected boolean isDuplicaatLaadProces(File input, String soort) {
        final String name = input.getName();
        EntityManager em = Stripersist.getEntityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<LaadProces> criteriaQuery = criteriaBuilder.createQuery(LaadProces.class);
        Root<LaadProces> from = criteriaQuery.from(LaadProces.class);
        CriteriaQuery<LaadProces> select = criteriaQuery.select(from);
        Predicate _bestand_naam = criteriaBuilder.equal(from.get("bestand_naam"), name);
        Predicate _soort = criteriaBuilder.equal(from.get("soort"), soort);
        criteriaQuery.where(criteriaBuilder.and(_bestand_naam, _soort));
        TypedQuery<LaadProces> typedQuery = em.createQuery(select);

        return !typedQuery.getResultList().isEmpty();
    }
}
