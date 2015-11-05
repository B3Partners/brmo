package nl.b3p.brmo.soap.brk;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Chris
 */

@XmlType
public class BrkInfoRequest {

    private boolean subjectsToevoegen = false;
    private boolean adressenToevoegen = false;
    private Integer maxAantalResultaten; // = Integer.parseInt("15");
    private boolean gevoeligeInfoOphalen = false;

    private String zoekgebied;
    private Integer bufferLengte;

    private KadOnrndZkInfoRequest kadOnrndZk;

    private String subjectNaam;

    private PerceelAdresInfoRequest perceelAdres;

    private String identificatie;

    public BrkInfoRequest() {

    }

    public BrkInfoRequest(String identificatie) {
        this.identificatie = identificatie;
    }

    /**
     * @return the identificatie
     */
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
     * In de response dient subject informatie te worden opgenomen (indien
     * beschikbaar)
     *
     * @return the subjectsToevoegen
     */
    @XmlAttribute(required = false)
    public boolean isSubjectsToevoegen() {
        return subjectsToevoegen;
    }

    /**
     * @param subjectsToevoegen the subjectsToevoegen to set
     */
    public void setSubjectsToevoegen(boolean subjectsToevoegen) {
        this.subjectsToevoegen = subjectsToevoegen;
    }

    /**
     * In de response moet adres info worden opgenomen (indien beschikbaar)
     *
     * @return the adressenToevoegen
     */
    @XmlAttribute(required = false)
    public boolean isAdressenToevoegen() {
        return adressenToevoegen;
    }

    /**
     * @param adressenToevoegen the adressenToevoegen to set
     */
    public void setAdressenToevoegen(boolean adressenToevoegen) {
        this.adressenToevoegen = adressenToevoegen;
    }

    /**
     * De response mag maximaal het hier opgegeven aantal gegevensrecords worden
     * opgenomen, default= 15, bij meer resultaten een foutmelding
     *
     * @return the maxAantalResultaten
     */
    @XmlAttribute(required = false)
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
     * In de response mag priivacygevoelige informatie (BSN nummer) worden
     * opgenomen (of dit ook daadwerkelijk gebeurt hangt af van autorisaties)
     *
     * @return the gevoeligeInfoOphalen
     */
    @XmlAttribute(required = false)
    public boolean isGevoeligeInfoOphalen() {
        return gevoeligeInfoOphalen;
    }

    /**
     * @param gevoeligeInfoOphalen the gevoeligeInfoOphalen to set
     */
    public void setGevoeligeInfoOphalen(boolean gevoeligeInfoOphalen) {
        this.gevoeligeInfoOphalen = gevoeligeInfoOphalen;
    }

    /**
     * Het zoekgebied is een aanduiding van de locatie als WKT. Zoeken op een
     * klikpunt wordt hiermee ondersteund doordat een POINT opgenomen kan worden
     * in de WKT
     *
     * @return the zoekgebied
     */
    public String getZoekgebied() {
        return zoekgebied;
    }

    /**
     * @param zoekgebied the zoekgebied to set
     */
    public void setZoekgebied(String zoekgebied) {
        this.zoekgebied = zoekgebied;
    }

    /**
     * De bufferlengte is een getal met als eenheid [meters]. Indien een
     * zoekgebied is opgegeven wordt de opgegeven locatie gebufferd met de
     * genoemde bufferlengte voordat deze wordt toegepast voor de selectie
     *
     * @return the bufferLengte
     */
    public Integer getBufferLengte() {
        return bufferLengte;
    }

    /**
     * @param bufferLengte the bufferLengte to set
     */
    public void setBufferLengte(Integer bufferLengte) {
        this.bufferLengte = bufferLengte;
    }

    /**
     * @return the kadOnrndZk
     */
    public KadOnrndZkInfoRequest getKadOnrndZk() {
        return kadOnrndZk;
    }

    /**
     * @param kadOnrndZk the kadOnrndZk to set
     */
    public void setKadOnrndZk(KadOnrndZkInfoRequest kadOnrndZk) {
        this.kadOnrndZk = kadOnrndZk;
    }

    /**
     * Naam waarop gezocht moet worden binnen de gerelateerde subjecten, welke
     * als comfort informatie via de BRK beschikbaar is. Er wordt minimaal
     * gezocht op achternaam en op bedrijfsnaam.
     *
     * @return the subjectNaam
     */
    public String getSubjectNaam() {
        return subjectNaam;
    }

    /**
     * Naam waarop gezocht moet worden binnen de gerelateerde subjecten, welke
     * als comfort informatie via de BRK beschikbaar is. Er wordt minimaal
     * gezocht op achternaam en op bedrijfsnaam.
     *
     * @param subjectNaam the subjectNaam to set
     */
    public void setSubjectNaam(String subjectNaam) {
        this.subjectNaam = subjectNaam;
    }

    /**
     * @return the perceelAdres
     */
    public PerceelAdresInfoRequest getPerceelAdres() {
        return perceelAdres;
    }

    /**
     * @param perceelAdres the perceelAdres to set
     */
    public void setPerceelAdres(PerceelAdresInfoRequest perceelAdres) {
        this.perceelAdres = perceelAdres;
    }

}
