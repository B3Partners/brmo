package nl.b3p.brmo.loader.entity;

import java.math.BigInteger;
import java.util.Date;

/**
 *
 * @author Boy de Wit
 */
public class LaadProces {

    private Long id;
    private String bestandNaam;
    private String bestandNaamHersteld;
    private Date bestandDatum;
    private String soort;
    private String gebied;
    private String opmerking;
    private LaadProces.STATUS status;
    private Date statusDatum;
    private String contactEmail;
    private Long automatischProces;

    private BigInteger klantafgiftenummer;

    private BigInteger contractafgiftenummer;

    private String artikelnummer;

    private String contractnummer;

    private String afgifteid;

    private String afgiftereferentie;

    private String bestandsreferentie;

    private Date beschikbaar_tot;


    /**
     * see nl.b3p.brmo.persistence.staging.LaadProces
     */
    public static enum STATUS {
        // als je er bij maakt ook in de andere LaadProces klasse toevoegen
        STAGING_OK, STAGING_NOK, ARCHIVE, STAGING_DUPLICAAT, RSGB_BGT_OK, RSGB_BGT_NOK, RSGB_BGT_WAITING, RSGB_TOPNL_WAITING, RSGB_TOPNL_OK, RSGB_TOPNL_NOK, STAGING_MISSING
    };

    public LaadProces() {
    }

    public LaadProces(String bestandNaam, String soort) {
        this.bestandNaam = bestandNaam;
        this.soort = soort;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBestandNaam() {
        return bestandNaam;
    }

    public void setBestandNaam(String bestandNaam) {
        this.bestandNaam = bestandNaam;
    }

    public Date getBestandDatum() {
        return bestandDatum;
    }

    public void setBestandDatum(Date bestandDatum) {
        this.bestandDatum = bestandDatum;
    }

    public String getSoort() {
        return soort;
    }

    public void setSoort(String soort) {
        this.soort = soort;
    }

    public String getGebied() {
        return gebied;
    }

    public void setGebied(String gebied) {
        this.gebied = gebied;
    }

    public String getOpmerking() {
        return opmerking;
    }

    public void setOpmerking(String opmerking) {
        this.opmerking = opmerking;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public Date getStatusDatum() {
        return statusDatum;
    }

    public void setStatusDatum(Date statusDatum) {
        this.statusDatum = statusDatum;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    /**
     * itt de hibernate variant geeft dit de id terug en niet het object.
     *
     * @return automatisch proces id
     */
    public Long getAutomatischProcesId() {
        return automatischProces;
    }

    /**
     * itt de hibernate variant neemt dit aan en niet het object, dat betaat
     * niet in dit model.
     *
     * @param automatischProces automatisch proces id
     */
    public void setAutomatischProcesId(Long automatischProces) {
        this.automatischProces = automatischProces;
    }

    public String getBestandNaamHersteld() {
        return bestandNaamHersteld;
    }

    public void setBestandNaamHersteld(String bestandNaamHersteld) {
        this.bestandNaamHersteld = bestandNaamHersteld;
    }

    public BigInteger getKlantafgiftenummer() {
        return klantafgiftenummer;
    }

    public void setKlantafgiftenummer(BigInteger klantafgiftenummer) {
        this.klantafgiftenummer = klantafgiftenummer;
    }

    public BigInteger getContractafgiftenummer() {
        return contractafgiftenummer;
    }

    public void setContractafgiftenummer(BigInteger contractafgiftenummer) {
        this.contractafgiftenummer = contractafgiftenummer;
    }

    public String getArtikelnummer() {
        return artikelnummer;
    }

    public void setArtikelnummer(String artikelnummer) {
        this.artikelnummer = artikelnummer;
    }

    public String getContractnummer() {
        return contractnummer;
    }

    public void setContractnummer(String contractnummer) {
        this.contractnummer = contractnummer;
    }

    public String getAfgifteid() {
        return afgifteid;
    }

    public void setAfgifteid(String afgifteid) {
        this.afgifteid = afgifteid;
    }

    public String getAfgiftereferentie() {
        return afgiftereferentie;
    }

    public void setAfgiftereferentie(String afgiftereferentie) {
        this.afgiftereferentie = afgiftereferentie;
    }

    public String getBestandsreferentie() {
        return bestandsreferentie;
    }

    public void setBestandsreferentie(String bestandsreferentie) {
        this.bestandsreferentie = bestandsreferentie;
    }

    public Date getBeschikbaar_tot() {
        return beschikbaar_tot;
    }

    public void setBeschikbaar_tot(Date beschikbaar_tot) {
        this.beschikbaar_tot = beschikbaar_tot;
    }
}
