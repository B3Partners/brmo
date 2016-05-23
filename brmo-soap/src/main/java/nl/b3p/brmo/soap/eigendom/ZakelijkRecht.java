package nl.b3p.brmo.soap.eigendom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ZakelijkRecht.
  */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "identificatienummer",
    "noemer",
    "teller",
    "bsn",
    "aardRecht"
})
public class ZakelijkRecht {

    @XmlElement(required = true)
    protected String identificatienummer;
    protected String noemer;
    protected String teller;
    @XmlElement(name = "BSN")
    protected String bsn;
    @XmlElement(name = "aard_recht")
    protected String aardRecht;

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
     * Gets the value of the noemer property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getNoemer() {
        return noemer;
    }

    /**
     * Sets the value of the noemer property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setNoemer(String value) {
        this.noemer = value;
    }

    /**
     * Gets the value of the teller property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getTeller() {
        return teller;
    }

    /**
     * Sets the value of the teller property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setTeller(String value) {
        this.teller = value;
    }

    /**
     * Gets the value of the bsn property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getBSN() {
        return bsn;
    }

    /**
     * Sets the value of the bsn property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setBSN(String value) {
        this.bsn = value;
    }

    /**
     * Gets the value of the aardRecht property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getAardRecht() {
        return aardRecht;
    }

    /**
     * Sets the value of the aardRecht property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setAardRecht(String value) {
        this.aardRecht = value;
    }

}
