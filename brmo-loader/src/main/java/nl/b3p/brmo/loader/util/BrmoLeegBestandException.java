/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.loader.util;

/**
 * Specifieke exception voor een leeg bestand met berichten.
 *
 * @author mprins
 */
public class BrmoLeegBestandException extends BrmoException {

    public BrmoLeegBestandException() {
    }

    public BrmoLeegBestandException(String message) {
        super(message);
    }

    public BrmoLeegBestandException(Throwable cause) {
        super(cause);
    }

    public BrmoLeegBestandException(String message, Throwable cause) {
        super(message, cause);
    }

}
