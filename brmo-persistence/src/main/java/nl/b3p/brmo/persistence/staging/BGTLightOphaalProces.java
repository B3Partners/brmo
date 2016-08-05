/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.Entity;

/**
 * Proces configuratie voor de BGT light downloader.
 *
 * @author mprins
 */
@Entity
public class BGTLightOphaalProces extends AutomatischProces {
    private static final String ARCHIEF_DIRECTORY = "archiefdirectory";

    /**
     * Zoekt de geconfigureerde archief directory op.
     *
     * @return de naam van de directory voor deze configuratie of null
     */
    public String getArchiefDirectory() {
        return ClobElement.nullSafeGet(this.getConfig().get(ARCHIEF_DIRECTORY));
    }

    /**
     * Stelt de te gebruiken archief directory in.
     *
     * @param archiefDirectory te gebruiken archief directory
     */
    public void setArchiefDirectory(String archiefDirectory) {
        if (archiefDirectory == null) {
            this.getConfig().put(ARCHIEF_DIRECTORY, null);
        } else {
            this.getConfig().put(ARCHIEF_DIRECTORY, new ClobElement(archiefDirectory));
        }
    }
}
