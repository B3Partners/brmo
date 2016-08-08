/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.Entity;

/**
 * Mail rapportage proces.
 *
 * @author mprins
 */
@Entity
public class MailRapportageProces extends AutomatischProces {

    public static final String DELIM = ",";
    /**
     * de sleutel {@value EMAIL}.
     */
    public static final String EMAIL = "email";
    /**
     * de sleutel {@value PIDS}.
     */
    public static final String PIDS = "pIDS";

    /**
     * de sleutel {@value FOR_STATUS}. Geldige waarden komen uit de verzameling
     * van {@link AutomatischProces#status }
     */
    public static final String FOR_STATUS = "forStatus";

    /**
     * haalt de lijst van email adressen op.
     *
     * @return string met adressen
     */
    public String getMailAdressen() {
        return ClobElement.nullSafeGet(this.getConfig().get(EMAIL));
    }

    /**
     * haalt de lijst van email adressen op.
     *
     * @return array van string met adressen
     */
    public String[] getMailAdressenArray() {
        final String adreslijst = this.getMailAdressen();
        if (adreslijst != null) {
            return adreslijst.split(DELIM);
        } else {
            return null;
        }
    }

    /**
     * wordt gebruikt om opslag van mailadressen te normaliseren.
     *
     * @param adressen een lijst adressen
     */
    public void setMailAdressen(String... adressen) {
        StringBuilder sb = new StringBuilder();
        for (String adres : adressen) {
            sb.append(adres.trim()).append(DELIM);
        }
        sb.setLength(sb.length() - 1);
        this.getConfig().put(EMAIL, new ClobElement(sb.toString()));
    }

    /**
     * voor stripes formulier input...
     *
     * @param adres een (lijst) adres(sen)
     */
    public void setMailAdressen(String adres) {
        if (adres == null) {
            this.getConfig().put(EMAIL, null);
        } else if (adres.contains(DELIM)) {
            this.setMailAdressen(adres.split(DELIM));
        } else {
            this.getConfig().put(EMAIL, new ClobElement(adres.trim()));
        }
    }

    public void setForStatus(ProcessingStatus status) {
        if (status == null) {
            this.getConfig().put(FOR_STATUS, null);
        } else {
            this.getConfig().put(FOR_STATUS, new ClobElement(status.name()));
        }
    }

    public ProcessingStatus getForStatus() {
        if (this.getConfig().get(FOR_STATUS) == null) {
            return null;
        } else {
            return ProcessingStatus.valueOf(this.getConfig().get(FOR_STATUS).getValue());
        }
    }
}
