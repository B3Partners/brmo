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
    "historischeRelatie"
})
public class HistorischeRelaties {

    @XmlElement(name = "historische_relatie", required = true)
    protected List<HistorischeRelatie> historischeRelatie;

    /**
     * Gets the value of the historischeRelatie property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the historischeRelatie property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getHistorischeRelatie().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link HistorischeRelatie }
     * {EigendomMutatie.HistorischeRelaties.HistorischeRelatie}
     *
     *
     */
    public List<HistorischeRelatie> getHistorischeRelatie() {
        if (historischeRelatie == null) {
            historischeRelatie = new ArrayList<HistorischeRelatie>();
        }
        return this.historischeRelatie;
    }

}
