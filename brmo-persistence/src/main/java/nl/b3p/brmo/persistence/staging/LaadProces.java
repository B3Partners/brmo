/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
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

    @Temporal(TemporalType.TIMESTAMP)
    private Date bestand_datum;

    private String soort;

    private String gebied;

    @Column
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String opmerking;

    @Enumerated(EnumType.STRING)
    private STATUS status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date status_datum;

    private String contact_email;

    @ManyToOne
    private AutomatischProces automatischProces;

    /**
     * see nl.b3p.brmo.loader.entity.LaadProces
     */
    public static enum STATUS {
        // als je er bij maakt ook in de andere LaadProces klasse toevoegen
        STAGING_OK, STAGING_NOK, ARCHIVE, STAGING_DUPLICAAT, RSGB_BGT_OK, RSGB_BGT_NOK, RSGB_BGT_WAITING, RSGB_TOPNL_WAITING, RSGB_TOPNL_OK, RSGB_TOPNL_NOK
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
    // </editor-fold>
}
