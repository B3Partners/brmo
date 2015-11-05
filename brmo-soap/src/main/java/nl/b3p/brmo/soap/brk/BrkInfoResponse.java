package nl.b3p.brmo.soap.brk;

import java.util.Date;
import javax.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author Chris
 */
public class BrkInfoResponse {
    
    private boolean bevatGevoeligeInfo = true;
    private Date timestamp;

    private KadOnrndZkInfoResponse kadOnrndZk;

    public BrkInfoResponse() {
        
    }

    /**
     * @return the bevatGevoeligeInfo
     */
    @XmlAttribute
    public boolean isBevatGevoeligeInfo() {
        return bevatGevoeligeInfo;
    }

    /**
     * @param bevatGevoeligeInfo the bevatGevoeligeInfo to set
     */
    public void setBevatGevoeligeInfo(boolean bevatGevoeligeInfo) {
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
    public KadOnrndZkInfoResponse getKadOnrndZk() {
        return kadOnrndZk;
    }

    /**
     * @param kadOnrndZk the kadOnrndZk to set
     */
    public void setKadOnrndZk(KadOnrndZkInfoResponse kadOnrndZk) {
        this.kadOnrndZk = kadOnrndZk;
    }
    
}
