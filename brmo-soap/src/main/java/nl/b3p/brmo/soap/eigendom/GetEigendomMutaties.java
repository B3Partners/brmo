
package nl.b3p.brmo.soap.eigendom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getEigendomMutaties complex type.
  */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getEigendomMutaties", propOrder = {
    "request"
})
public class GetEigendomMutaties {

    protected EigendomMutatieRequest request;

    /**
     * Gets the value of the request property.
     * 
     * @return
     *     possible object is
     *     {@link EigendomMutatieRequest }
     *     
     */
    public EigendomMutatieRequest getRequest() {
        return request;
    }

    /**
     * Sets the value of the request property.
     * 
     * @param value
     *     allowed object is
     *     {@link EigendomMutatieRequest }
     *     
     */
    public void setRequest(EigendomMutatieRequest value) {
        this.request = value;
    }

}
