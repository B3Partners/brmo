package nl.b3p.brmo.soap.eigendom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for mutatieListRequest complex type.
  */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mutatieListRequest", propOrder = {
    "fromDate",
    "objectprefix",
    "toDate"
})
public class MutatieListRequest {

    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar fromDate;
    protected String objectprefix;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar toDate;

    /**
     * Gets the value of the fromDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getFromDate() {
        return fromDate;
    }

    /**
     * Sets the value of the fromDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setFromDate(XMLGregorianCalendar value) {
        this.fromDate = value;
    }

    /**
     * Gets the value of the objectprefix property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjectprefix() {
        return objectprefix;
    }

    /**
     * Sets the value of the objectprefix property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjectprefix(String value) {
        this.objectprefix = value;
    }

    /**
     * Gets the value of the toDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getToDate() {
        return toDate;
    }

    /**
     * Sets the value of the toDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setToDate(XMLGregorianCalendar value) {
        this.toDate = value;
    }

}
