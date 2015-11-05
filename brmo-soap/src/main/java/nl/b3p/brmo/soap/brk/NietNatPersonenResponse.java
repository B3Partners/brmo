package nl.b3p.brmo.soap.brk;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Chris
 */
public class NietNatPersonenResponse {

    private List<NietNatuurlijkPersoonResponse> nietNatuurlijkPersoon = null;

    /**
     * @return the nietNatuurlijkPersoon
     */
    @XmlElement
    public List<NietNatuurlijkPersoonResponse> getNietNatuurlijkPersoon() {
        return nietNatuurlijkPersoon;
    }

    /**
     * @param nietNatuurlijkPersoon the nietNatuurlijkPersoon to set
     */
    public void setNietNatuurlijkPersoon(List<NietNatuurlijkPersoonResponse> nietNatuurlijkPersoon) {
        this.nietNatuurlijkPersoon = nietNatuurlijkPersoon;
    }

}
