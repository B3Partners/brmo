package nl.b3p.brmo.soap.eigendom;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Chris
 */

@XmlType
public class EigendomMutatieRequest {
    
    private Integer identificatie;

    private Integer maxAantalResultaten; // = Integer.parseInt("15");
 
    public EigendomMutatieRequest() {

    }

    /**
     * De response mag maximaal het hier opgegeven aantal gegevensrecords worden
     * opgenomen, default= 15, bij meer resultaten een foutmelding
     *
     * @return the maxAantalResultaten
     */
    @XmlAttribute
    public Integer getMaxAantalResultaten() {
        return maxAantalResultaten;
    }

    /**
     * @param maxAantalResultaten the maxAantalResultaten to set
     */
    public void setMaxAantalResultaten(Integer maxAantalResultaten) {
        this.maxAantalResultaten = maxAantalResultaten;
    }

    /**
     * Het ID van de kadastrale onroerende zaak welk gevraagd wordt.
     * 
     * @return the identificatie
     */
    public Integer getIdentificatie() {
        return identificatie;
    }

    /**
     * Het ID van de kadastrale onroerende zaak welk gevraagd wordt.
     * 
     * @param identificatie the identificatie to set
     */
    public void setIdentificatie(Integer identificatie) {
        this.identificatie = identificatie;
    }

}
