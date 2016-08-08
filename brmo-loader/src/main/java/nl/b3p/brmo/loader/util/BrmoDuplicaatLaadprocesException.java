/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.loader.util;

/**
 * Specifieke exception voor dubbel laden van een laadproces.
 *
 * @author mprins
 */
public class BrmoDuplicaatLaadprocesException extends BrmoException {

    public BrmoDuplicaatLaadprocesException() {
    }

    public BrmoDuplicaatLaadprocesException(String message) {
        super(message);
    }

    public BrmoDuplicaatLaadprocesException(Throwable cause) {
        super(cause);
    }

    public BrmoDuplicaatLaadprocesException(String message, Throwable cause) {
        super(message, cause);
    }
}
