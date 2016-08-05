
package nl.b3p.brmo.soap.eigendom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for eigendomMutatieRequest complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "eigendomMutatieRequest", propOrder = {
    "identificatie"
})
public class EigendomMutatieRequest {

    protected String identificatie;
    @XmlAttribute(name = "maxAantalResultaten")
    protected Integer maxAantalResultaten;

    /**
     * Gets the value of the identificatie property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public String getIdentificatie() {
        return identificatie;
    }

    /**
     * Sets the value of the identificatie property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setIdentificatie(String value) {
        this.identificatie = value;
    }

    /**
     * Gets the value of the maxAantalResultaten property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMaxAantalResultaten() {
        return maxAantalResultaten;
    }

    /**
     * Sets the value of the maxAantalResultaten property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMaxAantalResultaten(Integer value) {
        this.maxAantalResultaten = value;
    }

}
