package nl.b3p.brmo.loader.entity;

import java.util.Date;

/**
 *
 * @author Boy de Wit
 */
public class Bericht {
    private Long id;
    private Integer laadProcesId;
    private String objectRef;
    private Date datum;
    private Integer volgordeNummer;
    private String soort;
    private String opmerking;
    private Bericht.STATUS status;
    private String jobId;
    private Date statusDatum;
    private String brXml;
    private String brOrgineelXml;
    private String dbXml;
    private String xslVersion;

    public static enum STATUS {
        STAGING_OK, STAGING_NOK, STAGING_FORWARDED, RSGB_WAITING, RSGB_PROCESSING, RSGB_OK, RSGB_OUTDATED, RSGB_NOK, ARCHIVE
    };

    public Bericht(String brXml) {
        this.brXml = brXml;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLaadProcesId() {
        return laadProcesId;
    }

    public void setLaadProcesId(Integer laadProcesId) {
        this.laadProcesId = laadProcesId;
    }

    public String getObjectRef() {
        return objectRef;
    }

    public void setObjectRef(String objectRef) {
        this.objectRef = objectRef;
    }

    public Date getDatum() {
        return datum;
    }

    public void setDatum(Date datum) {
        this.datum = datum;
    }

    public Integer getVolgordeNummer() {
        return volgordeNummer;
    }

    public void setVolgordeNummer(Integer volgordeNummer) {
        this.volgordeNummer = volgordeNummer;
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

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Date getStatusDatum() {
        return statusDatum;
    }

    public void setStatusDatum(Date statusDatum) {
        this.statusDatum = statusDatum;
    }

    public String getBrXml() {
        return brXml;
    }

    public void setBrXml(String brXml) {
        this.brXml = brXml;
    }

    public String getBrOrgineelXml() {
        return brOrgineelXml;
    }

    public void setBrOrgineelXml(String brOrgineelXml) {
        this.brOrgineelXml = brOrgineelXml;
    }

    public String getDbXml() {
        return dbXml;
    }

    public void setDbXml(String dbXml) {
        this.dbXml = dbXml;
    }

    public String getXslVersion() {
        return xslVersion;
    }

    public void setXslVersion(String xslVersion) {
        this.xslVersion = xslVersion;
    }
}
