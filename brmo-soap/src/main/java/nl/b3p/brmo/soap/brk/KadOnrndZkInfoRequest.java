package nl.b3p.brmo.soap.brk;

import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Chris
 */
@XmlType
public class KadOnrndZkInfoRequest {

    private String gemeentecode;
    private String sectie;
    private String perceelnummer;
    private String appReVolgnummer;
    private String identificatie;

    public KadOnrndZkInfoRequest() {
    }

    /**
     * Afkorting voor kadastrale gemeente als gebruikt in AKR
     *
     * @return the gemeentecode
     */
    public String getGemeentecode() {
        return gemeentecode;
    }

    /**
     * Afkorting voor kadastrale gemeente als gebruikt in AKR
     *
     *
     * @param gemeentecode the gemeentecode to set
     */
    public void setGemeentecode(String gemeentecode) {
        this.gemeentecode = gemeentecode;
    }

    /**
     * Letter van sectie
     *
     * @return the sectie
     */
    public String getSectie() {
        return sectie;
    }

    /**
     * Letter van sectie
     *
     * @param sectie the sectie to set
     */
    public void setSectie(String sectie) {
        this.sectie = sectie;
    }

    /**
     * nummer van het perceel
     *
     * @return the perceelnummer
     */
    public String getPerceelnummer() {
        return perceelnummer;
    }

    /**
     * nummer van het perceel
     *
     * @param perceelnummer the perceelnummer to set
     */
    public void setPerceelnummer(String perceelnummer) {
        this.perceelnummer = perceelnummer;
    }

    /**
     * In geval van een appartement kan hier het volgnummer worden opgegeven
     *
     * @return the appReVolgnummer
     */
    public String getAppReVolgnummer() {
        return appReVolgnummer;
    }

    /**
     * In geval van een appartement kan hier het volgnummer worden opgegeven
     *
     * @param appReVolgnummer the appReVolgnummer to set
     */
    public void setAppReVolgnummer(String appReVolgnummer) {
        this.appReVolgnummer = appReVolgnummer;
    }

    /**
     * Formeel kadasstrale object nummer
     *
     * @return the identificatie
     */
    public String getIdentificatie() {
        return identificatie;
    }

    /**
     * Formele kadatstrale object nummer
     *
     * @param identificatie the identificatie to set
     */
    public void setIdentificatie(String identificatie) {
        this.identificatie = identificatie;
    }

}
