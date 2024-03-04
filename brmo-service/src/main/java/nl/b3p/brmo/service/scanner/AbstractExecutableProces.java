/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.io.File;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.*;
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

  /** maximale lengte waarna log wordt ingekort, moet een veelvoud van 10 zijn. */
  protected static final int OLD_LOG_LENGTH = 3000;

  volatile boolean active = false;

  /**
   * ProcesExecutable factory.
   *
   * @param config the type of {@code ProcesExecutable} to create.
   * @return an instance of the specified type
   */
  public static ProcesExecutable getProces(AutomatischProces config) {
    ProcessingImple imple = ProcessingImple.valueOf(config.getClass().getSimpleName());
    switch (imple) {
      case BAG2MutatieProces:
        return new BAG2MutatieProcesRunner((BAG2MutatieProces) config);
      case BRK2ScannerProces:
        return new BRK2DirectoryScanner((BRK2ScannerProces) config);
      case BRKScannerProces:
        return new BRKDirectoryScanner((BRKScannerProces) config);
      case MailRapportageProces:
        return new MailRapportage((MailRapportageProces) config);
      case GDS2OphaalProces:
        return new GDS2OphalenProces((GDS2OphaalProces) config);
      case BerichtTransformatieProces:
        return new BerichtTransformatieUitvoeren((BerichtTransformatieProces) config);
      case BerichtDoorstuurProces:
        return new BerichtDoorsturenProces((BerichtDoorstuurProces) config);
      case LaadprocesTransformatieProces:
        return new LaadprocesTransformatieUitvoeren((LaadprocesTransformatieProces) config);
      case MaterializedViewRefresh:
        return new MaterializedViewRefreshUitvoeren((MaterializedViewRefresh) config);
      case BerichtstatusRapportProces:
        return new BerichtstatusRapport((BerichtstatusRapportProces) config);
      case LaadprocesStatusRapportProces:
        return new LaadprocesStatusRapport((LaadprocesStatusRapportProces) config);
      case TopNLScannerProces:
        return new TopNLDirectoryScanner((TopNLScannerProces) config);
      case AfgifteNummerScannerProces:
        return new AfgifteNummerScanner((AfgifteNummerScannerProces) config);
      case BGTLoaderProces:
        return new BGTLoader((BGTLoaderProces) config);
      default:
        throw new IllegalArgumentException(imple.name() + " is is geen ondersteund proces...");
    }
  }

  @Override
  public boolean isRunning() {
    return this.active;
  }

  @Override
  public void stop() {
    this.active = false;
  }

  @Override
  public void run() {
    while (active) {
      try {
        this.execute();
        // TODO
        Thread.sleep(5000);
      } catch (InterruptedException | BrmoException e) {
        log.error(e.getMessage(), e);
      }
    }
  }

  /**
   * bepaal of het bestand een duplicaat is op basis van de bestandsnaam en soort.
   *
   * <p>De flow in
   *
   * <pre>
   * loadFromFile(bericht)
   * 	→ stagingProxy.loadBr(InputStream stream, String type, String fileName,...)
   * 	→ snapshot reader van de input stream parsed het bericht in een BrkSnapshotXMLReader of Brk2SnapshotXMLReader die bericht voor bericht uitgelezen kunnen worden
   * 	→ bepaal of laadproces bestaat stagingProxy.laadProcesExists(filenaam/datum)
   * 	→ laadproces in database maken stagingProxy.writeLaadProces(bestand_naam/bestand_datum/soort/gebied/opmerking/status/status_datum/contact_email)
   * 	→ uitlezen xml bericht als
   * 	→ !stagingProxy.berichtExists(laadprocesid/object_ref/datum/volgordenummer)
   * 	→ stagingProxy.writeBericht(b)
   * </pre>
   *
   * @param input een input bestand
   * @param soort het type registratie, bijvoorbeeld {@value
   *     nl.b3p.brmo.loader.BrmoFramework#BR_BRK2}
   * @return {@code true} als het bestand een duplicaat betreft, anders {@code false}
   */
  protected boolean isDuplicaatLaadProces(File input, String soort) {
    log.debug(
        "Controle voor duplicaat laadproces, soort: '" + soort + "', bestand: " + input.getName());
    final String name = getBestandsNaam(input);
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

  /**
   * bepaal bestandnaam.
   *
   * @param f het bestand
   * @return de naam van het bestand tbv oa. duplicaat controle
   * @see #isDuplicaatLaadProces(java.io.File, java.lang.String)
   */
  protected String getBestandsNaam(File f) {
    return f.getAbsolutePath();
  }

  protected Date getBestandsDatum(File f) {
    return new Date(f.lastModified());
  }
}
