package nl.b3p.brmo.loader.entity;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import static nl.b3p.brmo.loader.entity.NhrBerichten.NS_BRMO_BERICHT;

/**
 *
 * @author Boy de Wit
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Bericht {
    private Long id;
    private Integer laadProcesId;
    @XmlElement(namespace=NS_BRMO_BERICHT,name="object_ref")
    protected String objectRef;
    @XmlElement(namespace=NS_BRMO_BERICHT)
    @XmlJavaTypeAdapter(NhrPeildatumAdapter.class)
    protected Date datum;
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
        // bij toevoegen van een status deze ook in nl.b3p.brmo.persistence.staging.Bericht en /brmo-service/src/main/webapp/scripts/berichten.js toevoegen
        STAGING_OK, STAGING_NOK, STAGING_FORWARDED, RSGB_WAITING, RSGB_PROCESSING, RSGB_OK, RSGB_OUTDATED, RSGB_NOK, ARCHIVE, RSGB_BAG_NOK
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

    @Override
    public String toString() {
        return "Bericht{" + "id=" + id + ", objectRef=" + objectRef + ", datum=" + datum + ", volgordeNummer=" + volgordeNummer + ", soort=" + soort + ", status=" + status + ", jobId=" + jobId + ", statusDatum=" + statusDatum + '}';
    }
}
