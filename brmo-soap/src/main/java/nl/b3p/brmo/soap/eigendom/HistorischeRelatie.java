package nl.b3p.brmo.soap.eigendom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for HistorischeRelatie.
  */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "identificatienummer",
    "aard"
})
public class HistorischeRelatie {

    @XmlElement(required = true)
    protected String identificatienummer;
    protected String aard;

    /**
     * Gets the value of the identificatienummer property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getIdentificatienummer() {
        return identificatienummer;
    }

    /**
     * Sets the value of the identificatienummer property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setIdentificatienummer(String value) {
        this.identificatienummer = value;
    }

    /**
     * Gets the value of the aard property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getAard() {
        return aard;
    }

    /**
     * Sets the value of the aard property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setAard(String value) {
        this.aard = value;
    }

}
