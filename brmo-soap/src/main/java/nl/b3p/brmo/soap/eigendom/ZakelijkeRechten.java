package nl.b3p.brmo.soap.eigendom;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "zakelijkRecht"
})
public class ZakelijkeRechten {

    @XmlElement(name = "zakelijk_recht", required = true)
    protected List<ZakelijkRecht> zakelijkRecht;

    /**
     * Gets the value of the zakelijkRecht property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the zakelijkRecht property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getZakelijkRecht().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link ZakelijkRecht }
     * {EigendomMutatie.ZakelijkeRechten.ZakelijkRecht}
     *
     *
     */
    public List<ZakelijkRecht> getZakelijkRecht() {
        if (zakelijkRecht == null) {
            zakelijkRecht = new ArrayList<ZakelijkRecht>();
        }
        return this.zakelijkRecht;
    }

}
