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
    "benoemdObject"
})
public class BenoemdObjecten {

    @XmlElement(name = "benoemd_object", required = true)
    protected BenoemdObject benoemdObject;

    /**
     * Gets the value of the benoemdObject property.
     *
     * @return possible object is
         *     {@link EigendomMutatie.BenoemdObjecten.BenoemdObject }
     *
     */
    public BenoemdObject getBenoemdObject() {
        return benoemdObject;
    }

    /**
     * Sets the value of the benoemdObject property.
     *
     * @param value allowed object is
         *     {@link EigendomMutatie.BenoemdObjecten.BenoemdObject }
     *
     */
    public void setBenoemdObject(BenoemdObject value) {
        this.benoemdObject = value;
    }

}
