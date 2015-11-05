package nl.b3p.brmo.soap.brk;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Chris
 */
public class RechtenResponse {

    private List<ZakelijkRechtResponse> zakelijkRecht = null;

    /**
     * @return the zakelijkRecht
     */
    @XmlElement
    public List<ZakelijkRechtResponse> getZakelijkRecht() {
        return zakelijkRecht;
    }

    /**
     * @param zakelijkRecht the zakelijkRecht to set
     */
    public void setZakelijkRecht(List<ZakelijkRechtResponse> zakelijkRecht) {
        this.zakelijkRecht = zakelijkRecht;
    }

}
