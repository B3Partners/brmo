package nl.b3p.brmo.soap.brk;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Chris
 */
@XmlType
public class NietNatuurlijkPersoonResponse {

    private String identificatie;
    private String rechtsvorm;
    private String statutaireZetel;
    private String naam;

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
     * @return the rechtsvorm
     */
    public String getRechtsvorm() {
        return rechtsvorm;
    }

    /**
     * @param rechtsvorm the rechtsvorm to set
     */
    public void setRechtsvorm(String rechtsvorm) {
        this.rechtsvorm = rechtsvorm;
    }

    /**
     * @return the statutaireZetel
     */
    public String getStatutaireZetel() {
        return statutaireZetel;
    }

    /**
     * @param statutaireZetel the statutaireZetel to set
     */
    public void setStatutaireZetel(String statutaireZetel) {
        this.statutaireZetel = statutaireZetel;
    }

    /**
     * @return the naam
     */
    public String getNaam() {
        return naam;
    }

    /**
     * @param naam the naam to set
     */
    public void setNaam(String naam) {
        this.naam = naam;
    }

}
