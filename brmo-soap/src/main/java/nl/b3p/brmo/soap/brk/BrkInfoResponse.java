package nl.b3p.brmo.soap.brk;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Chris
 */
public class BrkInfoResponse {
    
    private Boolean bevatGevoeligeInfo;
    private Date timestamp;

    private List<KadOnrndZkInfoResponse> kadOnrndZk = null;

    public BrkInfoResponse() {
        
    }

    /**
     * @return the bevatGevoeligeInfo
     */
    @XmlAttribute
    public Boolean isBevatGevoeligeInfo() {
        return bevatGevoeligeInfo;
    }

    /**
     * @param bevatGevoeligeInfo the bevatGevoeligeInfo to set
     */
    public void setBevatGevoeligeInfo(Boolean bevatGevoeligeInfo) {
        this.bevatGevoeligeInfo = bevatGevoeligeInfo;
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
     * @return the kadOnrndZk
     */
    @XmlElement
    public List<KadOnrndZkInfoResponse> getKadOnrndZk() {
        return kadOnrndZk;
    }

    /**
     * @param kadOnrndZk the kadOnrndZk to set
     */
    public void setKadOnrndZk(List<KadOnrndZkInfoResponse> kadOnrndZk) {
        this.kadOnrndZk = kadOnrndZk;
    }
    
    public List<KadOnrndZkInfoResponse> addKadOnrndZk(KadOnrndZkInfoResponse koz) {
        if (kadOnrndZk==null) {
            kadOnrndZk = new ArrayList<KadOnrndZkInfoResponse>();
        }
        kadOnrndZk.add(koz);
        return kadOnrndZk;
    }

}
