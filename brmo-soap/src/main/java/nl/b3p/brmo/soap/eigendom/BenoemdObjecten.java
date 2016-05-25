package nl.b3p.brmo.soap.eigendom;

import java.util.ArrayList;
import java.util.List;
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
    protected List<BenoemdObject> benoemdObject;

    public List<BenoemdObject> getBenoemdObject() {
        if (benoemdObject == null) {
            benoemdObject = new ArrayList<BenoemdObject>();
        }
        return this.benoemdObject;
    }

}
