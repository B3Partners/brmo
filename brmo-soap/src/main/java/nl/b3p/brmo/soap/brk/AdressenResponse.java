package nl.b3p.brmo.soap.brk;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Chris
 */
public class AdressenResponse {

    private List<AdresResponse> BAGAdres = null;

    /**
     * @return the BAGAdres
     */
    @XmlElement
    public List<AdresResponse> getBAGAdres() {
        return BAGAdres;
    }

    /**
     * @param BAGAdres the BAGAdres to set
     */
    public void setBAGAdres(List<AdresResponse> BAGAdres) {
        this.BAGAdres = BAGAdres;
    }

}
