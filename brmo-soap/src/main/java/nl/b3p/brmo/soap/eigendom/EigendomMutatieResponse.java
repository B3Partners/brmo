package nl.b3p.brmo.soap.eigendom;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Chris
 */
public class EigendomMutatieResponse {
    
    private Date timestamp;

    private List<EigendomMutatie> eigendomMutatie = null;

    public EigendomMutatieResponse() {
        
    }

    /**
     * @return the timestamp
     */
    @XmlAttribute
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the eigendomMutatie
     */
    @XmlElement
    public List<EigendomMutatie> getEigendomMutatie() {
        return eigendomMutatie;
    }

    /**
     * @param eigendomMutatie the eigendomMutatie to set
     */
    public void setEigendomMutatie(List<EigendomMutatie> eigendomMutatie) {
        this.eigendomMutatie = eigendomMutatie;
    }
    
    public List<EigendomMutatie> addEigendomMutatie(EigendomMutatie koz) {
        if (eigendomMutatie==null) {
            eigendomMutatie = new ArrayList<EigendomMutatie>();
        }
        eigendomMutatie.add(koz);
        return eigendomMutatie;
    }

}
