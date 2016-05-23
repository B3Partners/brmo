
package nl.b3p.brmo.soap.eigendom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getMutatieList complex type.
  */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getMutatieList", propOrder = {
    "request"
})
public class GetMutatieList {

    protected MutatieListRequest request;

    /**
     * Gets the value of the request property.
     * 
     * @return
     *     possible object is
     *     {@link MutatieListRequest }
     *     
     */
    public MutatieListRequest getRequest() {
        return request;
    }

    /**
     * Sets the value of the request property.
     * 
     * @param value
     *     allowed object is
     *     {@link MutatieListRequest }
     *     
     */
    public void setRequest(MutatieListRequest value) {
        this.request = value;
    }

}
