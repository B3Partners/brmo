package nl.b3p.brmo.soap.eigendom;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

/** Java class for mutatieListResponse complex type. */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
        name = "mutatieListResponse",
        propOrder = {"mutatieEntry"})
public class MutatieListResponse {

    protected List<MutatieEntry> mutatieEntry;

    @XmlAttribute(name = "timestamp")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar timestamp;

    /**
     * Gets the value of the mutatieEntry property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot. Therefore any
     * modification you make to the returned list will be present inside the JAXB object. This is
     * why there is not a <CODE>set</CODE> method for the mutatieEntry property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     *    getMutatieList().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list {@link String }
     */
    public List<MutatieEntry> getMutatieEntry() {
        if (mutatieEntry == null) {
            mutatieEntry = new ArrayList<MutatieEntry>();
        }
        return this.mutatieEntry;
    }

    /**
     * Gets the value of the timestamp property.
     *
     * @return possible object is {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     *
     * @param value allowed object is {@link XMLGregorianCalendar }
     */
    public void setTimestamp(XMLGregorianCalendar value) {
        this.timestamp = value;
    }
}
