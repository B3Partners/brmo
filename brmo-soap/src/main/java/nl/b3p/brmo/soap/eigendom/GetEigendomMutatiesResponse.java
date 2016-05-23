
package nl.b3p.brmo.soap.eigendom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getEigendomMutatiesResponse complex type.
  */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getEigendomMutatiesResponse", propOrder = {
    "_return"
})
public class GetEigendomMutatiesResponse {

    @XmlElement(name = "return")
    protected EigendomMutatieResponse _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link EigendomMutatieResponse }
     *     
     */
    public EigendomMutatieResponse getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link EigendomMutatieResponse }
     *     
     */
    public void setReturn(EigendomMutatieResponse value) {
        this._return = value;
    }

}
