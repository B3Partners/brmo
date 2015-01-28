/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import nl.b3p.brmo.persistence.staging.BAGScannerProces;
import nl.b3p.brmo.persistence.staging.BRKScannerProces;
import nl.b3p.brmo.persistence.staging.MailRapportageProces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract to make sure comparisons are done right.
 *
 * @author Mark Prins
 */
public abstract class AbstractExecutableProces implements ProcesExecutable {

    private static final Log log = LogFactory.getLog(AbstractExecutableProces.class);
    volatile boolean active = true;



    /**
     * bepaal de duplicaat code voor een input bestand.
     *
     * @todo berekening van duplicaatcode
     * @param input een inpit bestand
     * @return de duplicaat code voor het bestand
     * @throws BrmoException als er een fout optreed in het bepalen van de code,
     * bijv. openen bestand of ...
     */
    public static Integer duplicaatCode(File input) throws BrmoException {
        try {
            FileInputStream i = new FileInputStream(input);
            byte[] b = new byte[1024];

            int n = i.read(b);
            i.close();
            String s = new String(b, 0, n);
            // bereken duplicaatcode
            StringBuilder hashcode = new StringBuilder();
            hashcode.append(s);

            return hashcode.hashCode();

        } catch (FileNotFoundException fnfe) {
            throw new BrmoException("Bestand niet gevonden.", fnfe);
        } catch (IOException ex) {
            throw new BrmoException("I/O fout voor bestand.", ex);
        }
    }

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
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            } catch (BrmoException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }
}
