/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.Entity;

/**
 * Mail rapportage proces.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
@Entity
public class MailRapportageProces extends AutomatischProces {

    private static final String DELIM = ",";

    private static final String EMAIL = "email";

    /**
     * haalt de lijst van email adressen op.
     *
     * @return array van string met adressen
     */
    public String[] getMailAdressen() {
        final String adreslijst = this.getConfig().get(EMAIL);
        if (adreslijst != null) {
            return adreslijst.split(DELIM);
        } else {
            return null;
        }
    }

    /**
     *
     * @param enkeladres een enkel adres
     */
    public void setMailAdressen(String enkeladres) {
        this.setMailAdressen(new String[]{enkeladres});
    }

    /**
     *
     * @param adressen een lijst adressen
     */
    public void setMailAdressen(String... adressen) {
        StringBuilder sb = new StringBuilder();
        for (String adres : adressen) {
            sb.append(adres).append(DELIM);
        }
        sb.setLength(sb.length() - 1);
        this.getConfig().put(EMAIL, sb.toString());
    }

}
