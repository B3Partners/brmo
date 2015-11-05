package nl.b3p.brmo.soap.brk;

import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Chris
 */
@XmlType
public class KadOnrndZkInfoResponse {

    private String gemeentecode;
    private String sectie;
    private String perceelnummer;
    private String appReVolgnummer;
    private String identificatie;
    private String aandSoortGrootte;
    private String begrenzingPerceel;
    private Float groottePerceel;
    private String omschr_deelperceel;
    private String aardCultuurOnbebouwd;
    private Date datumBeginGeldigheid;
    private Date datumEindeGeldigheid;
    private Float bedrag;
    private Integer koopjaar;
    private boolean meerOnroerendgoed = false;
    private String type; // appartement of perceel
    private AdressenResponse adressen;
    private RechtenResponse rechten;
    private RelatiesResponse relaties;

    public KadOnrndZkInfoResponse() {
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
     * Formele kadatstrale object nummer
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

    /**
     * @return the aandSoortGrootte
     */
    public String getAandSoortGrootte() {
        return aandSoortGrootte;
    }

    /**
     * @param aandSoortGrootte the aandSoortGrootte to set
     */
    public void setAandSoortGrootte(String aandSoortGrootte) {
        this.aandSoortGrootte = aandSoortGrootte;
    }

    /**
     * @return the begrenzingPerceel
     */
    public String getBegrenzingPerceel() {
        return begrenzingPerceel;
    }

    /**
     * @param begrenzingPerceel the begrenzingPerceel to set
     */
    public void setBegrenzingPerceel(String begrenzingPerceel) {
        this.begrenzingPerceel = begrenzingPerceel;
    }

    /**
     * @return the groottePerceel
     */
    public Float getGroottePerceel() {
        return groottePerceel;
    }

    /**
     * @param groottePerceel the groottePerceel to set
     */
    public void setGroottePerceel(Float groottePerceel) {
        this.groottePerceel = groottePerceel;
    }

    /**
     * @return the omschr_deelperceel
     */
    public String getOmschr_deelperceel() {
        return omschr_deelperceel;
    }

    /**
     * @param omschr_deelperceel the omschr_deelperceel to set
     */
    public void setOmschr_deelperceel(String omschr_deelperceel) {
        this.omschr_deelperceel = omschr_deelperceel;
    }

    /**
     * @return the aardCultuurOnbebouwd
     */
    public String getAardCultuurOnbebouwd() {
        return aardCultuurOnbebouwd;
    }

    /**
     * @param aardCultuurOnbebouwd the aardCultuurOnbebouwd to set
     */
    public void setAardCultuurOnbebouwd(String aardCultuurOnbebouwd) {
        this.aardCultuurOnbebouwd = aardCultuurOnbebouwd;
    }

    /**
     * @return the datumBeginGeldigheid
     */
    public Date getDatumBeginGeldigheid() {
        return datumBeginGeldigheid;
    }

    /**
     * @param datumBeginGeldigheid the datumBeginGeldigheid to set
     */
    public void setDatumBeginGeldigheid(Date datumBeginGeldigheid) {
        this.datumBeginGeldigheid = datumBeginGeldigheid;
    }

    /**
     * @return the datumEindeGeldigheid
     */
    public Date getDatumEindeGeldigheid() {
        return datumEindeGeldigheid;
    }

    /**
     * @param datumEindeGeldigheid the datumEindeGeldigheid to set
     */
    public void setDatumEindeGeldigheid(Date datumEindeGeldigheid) {
        this.datumEindeGeldigheid = datumEindeGeldigheid;
    }

    /**
     * @return the bedrag
     */
    public Float getBedrag() {
        return bedrag;
    }

    /**
     * @param bedrag the bedrag to set
     */
    public void setBedrag(Float bedrag) {
        this.bedrag = bedrag;
    }

    /**
     * @return the koopjaar
     */
    public Integer getKoopjaar() {
        return koopjaar;
    }

    /**
     * @param koopjaar the koopjaar to set
     */
    public void setKoopjaar(Integer koopjaar) {
        this.koopjaar = koopjaar;
    }

    /**
     * @return the meerOnroerendgoed
     */
    public boolean isMeerOnroerendgoed() {
        return meerOnroerendgoed;
    }

    /**
     * @param meerOnroerendgoed the meerOnroerendgoed to set
     */
    public void setMeerOnroerendgoed(boolean meerOnroerendgoed) {
        this.meerOnroerendgoed = meerOnroerendgoed;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the adressen
     */
    public AdressenResponse getAdressen() {
        return adressen;
    }

    /**
     * @param adressen the adressen to set
     */
    public void setAdressen(AdressenResponse adressen) {
        this.adressen = adressen;
    }

    /**
     * @return the rechten
     */
    public RechtenResponse getRechten() {
        return rechten;
    }

    /**
     * @param rechten the rechten to set
     */
    public void setRechten(RechtenResponse rechten) {
        this.rechten = rechten;
    }

    /**
     * @return the relaties
     */
    public RelatiesResponse getRelaties() {
        return relaties;
    }

    /**
     * @param relaties the relaties to set
     */
    public void setRelaties(RelatiesResponse relaties) {
        this.relaties = relaties;
    }

}
