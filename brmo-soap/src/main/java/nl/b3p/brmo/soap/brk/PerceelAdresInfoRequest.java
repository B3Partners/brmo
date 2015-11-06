package nl.b3p.brmo.soap.brk;

import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Chris
 */
@XmlType
public class PerceelAdresInfoRequest {
    
    private String straatNaam;
    private Integer huisnummer;
    private String postcode;
    private String woonplaatsNaam;
    

    public PerceelAdresInfoRequest() {
    }

    /**
     * Naam van de straat welke via de BAG koppeling aan 
     * het perceel is gekoppeld
     * 
     * @return the straatNaam
     */
    public String getStraatNaam() {
        return straatNaam;
    }

    /**
     * Naam van de straat welke via de BAG koppeling aan 
     * het perceel is gekoppeld
     * 
     * @param straatNaam the straatNaam to set
     */
    public void setStraatNaam(String straatNaam) {
        this.straatNaam = straatNaam;
    }

    /**
     * Huisnummer van de straat welke via de BAG koppeling aan 
     * het perceel is gekoppeld
     * 
     * @return the huisnummer
     */
    public Integer getHuisnummer() {
        return huisnummer;
    }

    /**
     * Huisnummer van de straat welke via de BAG koppeling aan 
     * het perceel is gekoppeld
     * 
     * @param huisnummer the huisnummer to set
     */
    public void setHuisnummer(Integer huisnummer) {
        this.huisnummer = huisnummer;
    }

    /**
     * Postcode welke via de BAG koppeling aan 
     * het perceel is gekoppeld
     * 
     * @return the postcode
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Postcode welke via de BAG koppeling aan 
     * het perceel is gekoppeld
     * 
     * @param postcode the postcode to set
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * @return the woonplaatsNaam
     */
    public String getWoonplaatsNaam() {
        return woonplaatsNaam;
    }

    /**
     * @param woonplaatsNaam the woonplaatsNaam to set
     */
    public void setWoonplaatsNaam(String woonplaatsNaam) {
        this.woonplaatsNaam = woonplaatsNaam;
    }

}
