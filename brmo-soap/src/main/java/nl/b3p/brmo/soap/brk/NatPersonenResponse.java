package nl.b3p.brmo.soap.brk;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Chris
 */
public class NatPersonenResponse {

   private List<NatuurlijkPersoonResponse> natuurlijkPersoon = null;

    /**
     * @return the natuurlijkPersoon
     */
    @XmlElement
    public List<NatuurlijkPersoonResponse> getNatuurlijkPersoon() {
        return natuurlijkPersoon;
    }

    /**
     * @param natuurlijkPersoon the natuurlijkPersoon to set
     */
    public void setNatuurlijkPersoon(List<NatuurlijkPersoonResponse> natuurlijkPersoon) {
        this.natuurlijkPersoon = natuurlijkPersoon;
    }

}
