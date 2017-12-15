/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import java.io.Serializable;
import java.util.Date;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Type;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Beschrijft een (xml) bericht in de staging database.
 *
 * @author mprins
 */
@Entity
@Table(name = "bericht")
public class Bericht implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    @ForeignKey(name = "bericht_laadprocesid_fkey")
    private LaadProces laadprocesid;

    @Temporal(TemporalType.TIMESTAMP)
    private Date datum;

    private String object_ref;

    private Integer volgordenummer;

    private String soort;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String opmerking;

    @Enumerated(EnumType.STRING)
    private STATUS status;

    @Temporal(TemporalType.TIMESTAMP)
    private Date status_datum;

    private String job_id;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String br_xml;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String br_orgineel_xml;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String db_xml;

    private String xsl_version;

    public static enum STATUS {
// bij toevoegen van een status deze ook in nl.b3p.brmo.loader.entity.Bericht en /brmo-service/src/main/webapp/scripts/berichten.js toevoegen
        STAGING_OK, STAGING_NOK, STAGING_FORWARDED, RSGB_WAITING, RSGB_PROCESSING, RSGB_OK, RSGB_OUTDATED, RSGB_NOK, ARCHIVE, RSGB_BAG_NOK
    };

    public Bericht() {
    }

    public Bericht(String br_xml) {
        this.br_xml = br_xml;
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LaadProces getLaadprocesid() {
        return laadprocesid;
    }

    public void setLaadprocesid(LaadProces laadprocesid) {
        this.laadprocesid = laadprocesid;
    }

    public Date getDatum() {
        return datum;
    }

    public void setDatum(Date datum) {
        this.datum = datum;
    }

    public String getObject_ref() {
        return object_ref;
    }

    public void setObject_ref(String object_ref) {
        this.object_ref = object_ref;
    }

    public Integer getVolgordenummer() {
        return volgordenummer;
    }

    public void setVolgordenummer(Integer volgordenummer) {
        this.volgordenummer = volgordenummer;
    }

    public String getSoort() {
        return soort;
    }

    public void setSoort(String soort) {
        this.soort = soort;
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

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public String getBr_xml() {
        return br_xml;
    }

    public void setBr_xml(String br_xml) {
        this.br_xml = br_xml;
    }

    public String getBr_orgineel_xml() {
        return br_orgineel_xml;
    }

    public void setBr_orgineel_xml(String br_orgineel_xml) {
        this.br_orgineel_xml = br_orgineel_xml;
    }

    public String getDb_xml() {
        // als lengte in bytes precies 8000 is dan kan oracle
        // dit waarde niet lezen uit database zonde \o toe te voegen
        // TODO gaan we hier iets aan doen?
        return db_xml;
    }

    public void setDb_xml(String db_xml) {
        this.db_xml = db_xml;
    }

    public String getXsl_version() {
        return xsl_version;
    }

    public void setXsl_version(String xsl_version) {
        this.xsl_version = xsl_version;
    }
//</editor-fold>
}
