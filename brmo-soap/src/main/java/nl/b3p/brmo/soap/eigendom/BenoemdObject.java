package nl.b3p.brmo.soap.eigendom;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for anonymous complex type.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "identificatienummer",
    "gebouwdObjectOppervlakte",
    "gebouwdObjectGebruiksdoel",
    "addreseerbaarObjectAanduiding",
    "brondocumenten"
})
public class BenoemdObject {

    @XmlElement(required = true)
    protected String identificatienummer;
    @XmlElement(name = "gebouwd_object_oppervlakte")
    protected String gebouwdObjectOppervlakte;
    @XmlElement(name = "gebouwd_object_gebruiksdoel")
    protected String gebouwdObjectGebruiksdoel;
    @XmlElement(name = "addreseerbaar_object_aanduiding")
    protected List<AddreseerbaarObjectAanduiding> addreseerbaarObjectAanduiding;
    protected Brondocumenten brondocumenten;

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
     * Gets the value of the gebouwdObjectOppervlakte property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getGebouwdObjectOppervlakte() {
        return gebouwdObjectOppervlakte;
    }

    /**
     * Sets the value of the gebouwdObjectOppervlakte property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setGebouwdObjectOppervlakte(String value) {
        this.gebouwdObjectOppervlakte = value;
    }

    /**
     * Gets the value of the gebouwdObjectGebruiksdoel property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getGebouwdObjectGebruiksdoel() {
        return gebouwdObjectGebruiksdoel;
    }

    /**
     * Sets the value of the gebouwdObjectGebruiksdoel property.
     *
     * @param value allowed object is {@link String }
     *
     */
    public void setGebouwdObjectGebruiksdoel(String value) {
        this.gebouwdObjectGebruiksdoel = value;
    }

    /**
     * Gets the value of the addreseerbaarObjectAanduiding property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the addreseerbaarObjectAanduiding property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAddreseerbaarObjectAanduiding().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link AddreseerbaarObjectAanduiding }
     * {EigendomMutatie.BenoemdObjecten.BenoemdObject.AddreseerbaarObjectAanduiding}
     *
     *
     */
    public List<AddreseerbaarObjectAanduiding> getAddreseerbaarObjectAanduiding() {
        if (addreseerbaarObjectAanduiding == null) {
            addreseerbaarObjectAanduiding = new ArrayList<AddreseerbaarObjectAanduiding>();
        }
        return this.addreseerbaarObjectAanduiding;
    }

    /**
     * Gets the value of the brondocumenten property.
     *
     * @return possible object is {@link Brondocumenten }
     *     {EigendomMutatie.BenoemdObjecten.BenoemdObject.Brondocumenten}
     *
     *
     */
    public Brondocumenten getBrondocumenten() {
        return brondocumenten;
    }

    /**
     * Sets the value of the brondocumenten property.
     *
     * @param value allowed object is {@link Brondocumenten }
     *     {EigendomMutatie.BenoemdObjecten.BenoemdObject.Brondocumenten}
     *
     */
    public void setBrondocumenten(Brondocumenten value) {
        this.brondocumenten = value;
    }

}
