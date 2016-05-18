package nl.b3p.brmo.soap.eigendom;

public class EigendomMutatieException extends Exception {
    String detail;
    
    public EigendomMutatieException (String message, String detail) {
        super (message);
        this.detail = detail;
    }
    
    public String getDetail () {
        return detail;
    }
}
