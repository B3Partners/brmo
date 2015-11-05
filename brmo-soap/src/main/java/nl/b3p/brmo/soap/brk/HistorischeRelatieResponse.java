package nl.b3p.brmo.soap.brk;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Chris
 */
@XmlType
public class HistorischeRelatieResponse {
    
    private String identificatie;
    private String aard;
    private Float overgangsgrootte;

    /**
     * @return the identificatie
     */
    @XmlElement(required = true)
    public String getIdentificatie() {
        return identificatie;
    }

    /**
     * @param identificatie the identificatie to set
     */
    public void setIdentificatie(String identificatie) {
        this.identificatie = identificatie;
    }

    /**
     * @return the aard
     */
    public String getAard() {
        return aard;
    }

    /**
     * @param aard the aard to set
     */
    public void setAard(String aard) {
        this.aard = aard;
    }

    /**
     * @return the overgangsgrootte
     */
    public Float getOvergangsgrootte() {
        return overgangsgrootte;
    }

    /**
     * @param overgangsgrootte the overgangsgrootte to set
     */
    public void setOvergangsgrootte(Float overgangsgrootte) {
        this.overgangsgrootte = overgangsgrootte;
    }
    
}
