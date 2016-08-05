package nl.b3p.brmo.soap.eigendom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * <p>
 * Java class for mutatieEntry complex type.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "objectRef",
    "volgnummer",
    "datum",
    "statusDatum"
})
public class MutatieEntry {

    @XmlElement(name = "object_ref", required = true)
    private String objectRef;
    @XmlElement(required = true)
    private String volgnummer;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar datum;
    @XmlElement(name = "status_datum", required = true)
    @XmlSchemaType(name = "dateTime")
    private XMLGregorianCalendar statusDatum;

    /**
     * @return the objectRef
     */
    public String getObjectRef() {
        return objectRef;
    }

    /**
     * @param objectRef the objectRef to set
     */
    public void setObjectRef(String objectRef) {
        this.objectRef = objectRef;
    }

    /**
     * @return the volgnummer
     */
    public String getVolgnummer() {
        return volgnummer;
    }

    /**
     * @param volgnummer the volgnummer to set
     */
    public void setVolgnummer(String volgnummer) {
        this.volgnummer = volgnummer;
    }

    /**
     * @return the datum
     */
    public XMLGregorianCalendar getDatum() {
        return datum;
    }

    /**
     * @param datum the datum to set
     */
    public void setDatum(XMLGregorianCalendar datum) {
        this.datum = datum;
    }

    /**
     * @return the statusDatum
     */
    public XMLGregorianCalendar getStatusDatum() {
        return statusDatum;
    }

    /**
     * @param statusDatum the statusDatum to set
     */
    public void setStatusDatum(XMLGregorianCalendar statusDatum) {
        this.statusDatum = statusDatum;
    }

}
