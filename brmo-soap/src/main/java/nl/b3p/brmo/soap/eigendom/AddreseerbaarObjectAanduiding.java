package nl.b3p.brmo.soap.eigendom;

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
    "identificatienummer",
    "postcode",
    "huisnummer",
    "huisletter",
    "huisnummertoevoeging",
    "woonplaatsIdentificatie",
    "openbareRuimteNaam"
})
public class AddreseerbaarObjectAanduiding {

    @XmlElement(required = true)
    protected String identificatienummer;
    protected String postcode;
    protected String huisnummer;
    protected String huisletter;
    protected String huisnummertoevoeging;
    @XmlElement(name = "woonplaats_identificatie")
    protected String woonplaatsIdentificatie;
    @XmlElement(name = "openbare_ruimte_naam")
    protected String openbareRuimteNaam;

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
     * Gets the value of the postcode property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Sets the value of the postcode property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setPostcode(String value) {
        this.postcode = value;
    }

    /**
     * Gets the value of the huisnummer property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getHuisnummer() {
        return huisnummer;
    }

    /**
     * Sets the value of the huisnummer property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setHuisnummer(String value) {
        this.huisnummer = value;
    }

    /**
     * Gets the value of the huisletter property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getHuisletter() {
        return huisletter;
    }

    /**
     * Sets the value of the huisletter property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setHuisletter(String value) {
        this.huisletter = value;
    }

    /**
     * Gets the value of the huisnummertoevoeging property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getHuisnummertoevoeging() {
        return huisnummertoevoeging;
    }

    /**
     * Sets the value of the huisnummertoevoeging property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setHuisnummertoevoeging(String value) {
        this.huisnummertoevoeging = value;
    }

    /**
     * Gets the value of the woonplaatsIdentificatie property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getWoonplaatsIdentificatie() {
        return woonplaatsIdentificatie;
    }

    /**
     * Sets the value of the woonplaatsIdentificatie property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setWoonplaatsIdentificatie(String value) {
        this.woonplaatsIdentificatie = value;
    }

    /**
     * Gets the value of the openbareRuimteNaam property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getOpenbareRuimteNaam() {
        return openbareRuimteNaam;
    }

    /**
     * Sets the value of the openbareRuimteNaam property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setOpenbareRuimteNaam(String value) {
        this.openbareRuimteNaam = value;
    }

}
