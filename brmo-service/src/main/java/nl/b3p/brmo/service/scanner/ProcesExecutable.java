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

    /**
     *
     * @deprecated work around gebrek aan String switch in java 6
     */
    enum ProcessingImple {

        BAGScannerProces, BRKScannerProces, MailRapportageProces;
    }

    /**
     * Voert deze taak eenmalig uit.
     *
     * @exception BrmoException als er een fout optreed in het uitvoeren van het
     * proces.
     */
    void execute() throws BrmoException;

    /**
     * probeert het lopende proces te stoppen.
     */
    void stop();

    /**
     *
     * @return true als deze processor runt.
     */
    boolean isRunning();
}
