package nl.b3p.brmo.loader.util;

/**
 *
 * @author Chris
 */
public class BrmoException extends Exception {

    public BrmoException() {
    }

    public BrmoException(String message) {
        super(message);
    }

    public BrmoException(Throwable cause) {
        super(cause);
    }

    public BrmoException(String message, Throwable cause) {
        super(message, cause);
    }

}
