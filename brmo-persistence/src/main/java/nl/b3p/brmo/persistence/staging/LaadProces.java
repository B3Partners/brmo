/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import javax.persistence.*;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Type;

/**
 * Beschrijft een laad proces van een bericht of bestand.
 *
 * @author mprins
 */
@Entity
@Table(name = "laadproces")
public class LaadProces implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    private String bestand_naam;
    
    private String bestand_naam_hersteld;

    @Temporal(TemporalType.TIMESTAMP)
    private Date bestand_datum;

    private String soort;

    private String gebied;

    @Column
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String opmerking;

    @Enumerated(EnumType.STRING)
    private STATUS status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date status_datum;

    private String contact_email;

    @ManyToOne
    @JoinColumn(name="automatisch_proces")
    private AutomatischProces automatischProces;

    private BigInteger klantafgiftenummer;

    private BigInteger contractafgiftenummer;

    private String artikelnummer;

    private String contractnummer;

    private String afgifteid;

    private String afgiftereferentie;

    private String bestandsreferentie;

    @Temporal(TemporalType.TIMESTAMP)
    private Date beschikbaar_tot;

    /**
     * see nl.b3p.brmo.loader.entity.LaadProces
     */
    public static enum STATUS {
        // als je er bij maakt ook in de andere LaadProces klasse toevoegen
        STAGING_OK, STAGING_NOK, STAGING_MISSING, STAGING_DUPLICAAT, ARCHIVE, RSGB_BGT_OK, RSGB_BGT_NOK, RSGB_BGT_WAITING, RSGB_TOPNL_WAITING, RSGB_TOPNL_OK, RSGB_TOPNL_NOK
    };

        public LaadProces() {
    }

    public LaadProces(String bestand_naam, String soort) {
        this.bestand_naam = bestand_naam;
        this.soort = soort;
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBestand_naam() {
        return bestand_naam;
    }

    public void setBestand_naam(String bestand_naam) {
        this.bestand_naam = bestand_naam;
    }

    public Date getBestand_datum() {
        return bestand_datum;
    }

    public void setBestand_datum(Date bestand_datum) {
        this.bestand_datum = bestand_datum;
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

    public Date getStatus_datum() {
        return status_datum;
    }

    public void setStatus_datum(Date status_datum) {
        this.status_datum = status_datum;
    }

    public String getContact_email() {
        return contact_email;
    }

    public void setContact_email(String contact_email) {
        this.contact_email = contact_email;
    }

    public AutomatischProces getAutomatischProces() {
        return automatischProces;
    }

    public void setAutomatischProces(AutomatischProces automatischProces) {
        this.automatischProces = automatischProces;
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

    public String getBestand_naam_hersteld() {
        return bestand_naam_hersteld;
    }

    public void setBestand_naam_hersteld(String bestand_naam_hersteld) {
        this.bestand_naam_hersteld = bestand_naam_hersteld;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "LaadProces{" +
                "id=" + id +
                ", bestandNaam='" + bestand_naam + '\'' +
                ", bestandNaamHersteld='" + bestand_naam_hersteld + '\'' +
                ", bestandDatum=" + bestand_datum +
                ", soort='" + soort + '\'' +
                ", gebied='" + gebied + '\'' +
                ", opmerking='" + opmerking + '\'' +
                ", status=" + status +
                ", statusDatum=" + status_datum +
                ", contactEmail='" + contact_email + '\'' +
                ", automatischProces=" + automatischProces +
                ", klantafgiftenummer=" + klantafgiftenummer +
                ", contractafgiftenummer=" + contractafgiftenummer +
                ", artikelnummer='" + artikelnummer + '\'' +
                ", contractnummer='" + contractnummer + '\'' +
                ", afgifteid='" + afgifteid + '\'' +
                ", afgiftereferentie='" + afgiftereferentie + '\'' +
                ", bestandsreferentie='" + bestandsreferentie + '\'' +
                ", beschikbaar_tot=" + beschikbaar_tot +
                '}';
    }
}
