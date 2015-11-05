package nl.b3p.brmo.soap.brk;

import java.util.Date;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Chris
 */
@XmlType
public class NatuurlijkPersoonResponse {

    private String identificatie;
    private Date geboortedatum;
    private Date overlijdensdatum;
    private String bsn;
    private String geboorteplaats;
    private String locatieBeschrijving;
    private String aanduidingNaamgebruik;
    private String geslachtsaanduiding;
    private String geslachtsnaam;
    private String voornamen;
    private String voorvoegsel;

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
     * @return the geboortedatum
     */
    public Date getGeboortedatum() {
        return geboortedatum;
    }

    /**
     * @param geboortedatum the geboortedatum to set
     */
    public void setGeboortedatum(Date geboortedatum) {
        this.geboortedatum = geboortedatum;
    }

    /**
     * @return the overlijdensdatum
     */
    public Date getOverlijdensdatum() {
        return overlijdensdatum;
    }

    /**
     * @param overlijdensdatum the overlijdensdatum to set
     */
    public void setOverlijdensdatum(Date overlijdensdatum) {
        this.overlijdensdatum = overlijdensdatum;
    }

    /**
     * @return the bsn
     */
    public String getBsn() {
        return bsn;
    }

    /**
     * @param bsn the bsn to set
     */
    public void setBsn(String bsn) {
        this.bsn = bsn;
    }

    /**
     * @return the geboorteplaats
     */
    public String getGeboorteplaats() {
        return geboorteplaats;
    }

    /**
     * @param geboorteplaats the geboorteplaats to set
     */
    public void setGeboorteplaats(String geboorteplaats) {
        this.geboorteplaats = geboorteplaats;
    }

    /**
     * @return the locatieBeschrijving
     */
    public String getLocatieBeschrijving() {
        return locatieBeschrijving;
    }

    /**
     * @param locatieBeschrijving the locatieBeschrijving to set
     */
    public void setLocatieBeschrijving(String locatieBeschrijving) {
        this.locatieBeschrijving = locatieBeschrijving;
    }

    /**
     * @return the aanduidingNaamgebruik
     */
    public String getAanduidingNaamgebruik() {
        return aanduidingNaamgebruik;
    }

    /**
     * @param aanduidingNaamgebruik the aanduidingNaamgebruik to set
     */
    public void setAanduidingNaamgebruik(String aanduidingNaamgebruik) {
        this.aanduidingNaamgebruik = aanduidingNaamgebruik;
    }

    /**
     * @return the geslachtsaanduiding
     */
    public String getGeslachtsaanduiding() {
        return geslachtsaanduiding;
    }

    /**
     * @param geslachtsaanduiding the geslachtsaanduiding to set
     */
    public void setGeslachtsaanduiding(String geslachtsaanduiding) {
        this.geslachtsaanduiding = geslachtsaanduiding;
    }

    /**
     * @return the geslachtsnaam
     */
    public String getGeslachtsnaam() {
        return geslachtsnaam;
    }

    /**
     * @param geslachtsnaam the geslachtsnaam to set
     */
    public void setGeslachtsnaam(String geslachtsnaam) {
        this.geslachtsnaam = geslachtsnaam;
    }

    /**
     * @return the voornamen
     */
    public String getVoornamen() {
        return voornamen;
    }

    /**
     * @param voornamen the voornamen to set
     */
    public void setVoornamen(String voornamen) {
        this.voornamen = voornamen;
    }

    /**
     * @return the voorvoegsel
     */
    public String getVoorvoegsel() {
        return voorvoegsel;
    }

    /**
     * @param voorvoegsel the voorvoegsel to set
     */
    public void setVoorvoegsel(String voorvoegsel) {
        this.voorvoegsel = voorvoegsel;
    }

}
