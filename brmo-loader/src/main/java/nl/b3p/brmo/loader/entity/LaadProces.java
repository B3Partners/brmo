package nl.b3p.brmo.loader.entity;

import java.util.Date;

/**
 *
 * @author Boy de Wit
 */
public class LaadProces {
    
    private Long id;
    private String bestandNaam;
    private Date bestandDatum;
    private String soort;
    private String gebied;
    private String opmerking;
    private LaadProces.STATUS status;
    private Date statusDatum;
    private String contactEmail;
    
    public static enum STATUS {
        STAGING_OK, STAGING_NOK, ARCHIVE
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
}
