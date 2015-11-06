package nl.b3p.brmo.soap.brk;

/**
 *
 * @author Chris
 */
public class AdresResponse {
    
    private String straatNaam;
    private Integer huisnummer;
    private String huisLetter;
    private String huisLetterToevoeging;
    private String postcode;
    private String woonplaatsNaam;
            

    /**
     * @return the straatNaam
     */
    public String getStraatNaam() {
        return straatNaam;
    }

    /**
     * @param straatNaam the straatNaam to set
     */
    public void setStraatNaam(String straatNaam) {
        this.straatNaam = straatNaam;
    }

    /**
     * @return the huisnummer
     */
    public Integer getHuisnummer() {
        return huisnummer;
    }

    /**
     * @param huisnummer the huisnummer to set
     */
    public void setHuisnummer(Integer huisnummer) {
        this.huisnummer = huisnummer;
    }

    /**
     * @return the huisLetter
     */
    public String getHuisLetter() {
        return huisLetter;
    }

    /**
     * @param huisLetter the huisLetter to set
     */
    public void setHuisLetter(String huisLetter) {
        this.huisLetter = huisLetter;
    }

    /**
     * @return the huisLetterToevoeging
     */
    public String getHuisLetterToevoeging() {
        return huisLetterToevoeging;
    }

    /**
     * @param huisLetterToevoeging the huisLetterToevoeging to set
     */
    public void setHuisLetterToevoeging(String huisLetterToevoeging) {
        this.huisLetterToevoeging = huisLetterToevoeging;
    }

    /**
     * @return the postcode
     */
    public String getPostcode() {
        return postcode;
    }

    /**
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
