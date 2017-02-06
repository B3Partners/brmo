package nl.b3p.brmo.soap.eigendom;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Java class for anonymous complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "document"
})
public class Brondocumenten {

    @XmlElement(required = true)
    protected List<Document> document;

    /**
     * Gets the value of the document property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a
     * snapshot. Therefore any modification you make to the returned list will
     * be present inside the JAXB object. This is why there is not a
     * <CODE>set</CODE> method for the document property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDocument().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {EigendomMutatie.Brondocumenten.Document }
     *
     *
     */
    public List<Document> getDocument() {
        if (document == null) {
            document = new ArrayList<Document>();
        }
        return this.document;
    }

}
