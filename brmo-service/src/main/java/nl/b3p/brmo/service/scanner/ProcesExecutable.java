/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import nl.b3p.brmo.loader.util.BrmoException;

/**
 * Specifies ProcesExecutable functionaliteit.
 *
 * @author Mark Prins
 */
public interface ProcesExecutable extends Runnable {

  /** */
  enum ProcessingImple {
    // klassen uit nl.b3p.brmo.persistence.staging package
    BAG2MutatieProces,
    BRK2ScannerProces,
    MailRapportageProces,
    GDS2OphaalProces,
    BerichtTransformatieProces,
    BerichtDoorstuurProces,
    BGTLoaderProces,
    MaterializedViewRefresh,
    BerichtstatusRapportProces,
    LaadprocesStatusRapportProces,
    AfgifteNummerScannerProces,
    KVKMutatieserviceProces,
  }

  /**
   * Voert deze taak eenmalig uit.
   *
   * @exception BrmoException als er een fout optreed in het uitvoeren van het proces.
   */
  void execute() throws BrmoException;

  /**
   * Voert de taak uit en rapporteert de voortgang.
   *
   * @param listener voortgangs listener
   */
  void execute(ProgressUpdateListener listener);

  /** probeert het lopende proces te stoppen. */
  void stop();

  /**
   * @return true als deze processor runt.
   */
  boolean isRunning();
}
