
package nl.b3p.brmo.soap.eigendom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getMutatieListResponse complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getMutatieListResponse", propOrder = {
    "_return"
})
public class GetMutatieListResponse {

    @XmlElement(name = "return")
    protected MutatieListResponse _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link MutatieListResponse }
     *     
     */
    public MutatieListResponse getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link MutatieListResponse }
     *     
     */
    public void setReturn(MutatieListResponse value) {
        this._return = value;
    }

}
