package nl.b3p.brmo.soap.brk;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Chris
 */
public class RelatiesResponse {

    private List<HistorischeRelatieResponse> historischeRelatie = null;
    private String grondPerceel;

    /**
     * @return the historischeRelatie
     */
    @XmlElement
    public List<HistorischeRelatieResponse> getHistorischeRelaties() {
        return historischeRelatie;
    }

    /**
     * @param historischeRelatie the historischeRelatie to set
     */
    public void setHistorischeRelaties(List<HistorischeRelatieResponse> historischeRelatie) {
        this.historischeRelatie = historischeRelatie;
    }

    /**
     * @return the grondPerceel
     */
    public String getGrondPerceel() {
        return grondPerceel;
    }

    /**
     * @param grondPerceel the grondPerceel to set
     */
    public void setGrondPerceel(String grondPerceel) {
        this.grondPerceel = grondPerceel;
    }

}
