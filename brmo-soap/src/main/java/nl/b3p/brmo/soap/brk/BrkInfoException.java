package nl.b3p.brmo.soap.brk;

public class BrkInfoException extends Exception {
    String detail;
    
    public BrkInfoException (String message, String detail) {
        super (message);
        this.detail = detail;
    }
    
    public String getDetail () {
        return detail;
    }
}
