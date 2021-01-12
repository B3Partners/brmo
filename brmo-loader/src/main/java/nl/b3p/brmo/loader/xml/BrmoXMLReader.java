package nl.b3p.brmo.loader.xml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import nl.b3p.brmo.loader.entity.Bericht;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * abstract reader of basisregistratie files
 *
 * @author Boy de Wit
 */
public abstract class BrmoXMLReader {

    private static final Log log = LogFactory.getLog(BrmoXMLReader.class);

    // laadproces
    private String bestandsNaam;
    private Date bestandsDatum;
    protected String soort; //waarde gezet in implementatie
    private String gebied;
    private String opmerking;
    private String status;
    private Date statusDatum;
    private String contactEmail;

    public abstract void init() throws Exception;
    public abstract boolean hasNext() throws Exception;
    public abstract Bericht next() throws Exception;

    public void setDatumAsString(String datumString, String simpleDateFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat(simpleDateFormat);
        try {
            bestandsDatum = sdf.parse(datumString);
        } catch (ParseException pe) {
            log.error("Fout bij parsen bericht datum \"" + datumString + "\" met formaat " + simpleDateFormat, pe);
        }
    }
    public String getHash(String bsn) {
        String hash = DigestUtils.sha1Hex(bsn);
        return  hash;
    }

    public void setDatumAsString(String brkDatumString) {
        setDatumAsString(brkDatumString, "yyyy-MM-dd");
    }

    public String getBestandsNaam() {
        return bestandsNaam;
    }

    public void setBestandsNaam(String bestandsNaam) {
        this.bestandsNaam = bestandsNaam;
    }

    public Date getBestandsDatum() {
        return bestandsDatum;
    }

    public void setBestandsDatum(Date bestandsDatum) {
        this.bestandsDatum = bestandsDatum;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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
