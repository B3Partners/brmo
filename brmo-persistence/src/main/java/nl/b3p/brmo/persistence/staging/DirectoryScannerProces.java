/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.Transient;

/**
 * Levert een aantal gedeelde methoden voor directory gebaseerde processen.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
abstract /* package-private */ class DirectoryScannerProces extends AutomatischProces {

    private static final String SCAN_DIRECTORY = "scandirectory";

    private static final String ARCHIEF_DIRECTORY = "archiefdirectory";

    // deze zijn er ter genoegdoening van de reflection api van Stripes
    @Transient
    private volatile String scanDirectory;
    @Transient
    private volatile String archiefDirectory;

    /**
     * Zoekt de geconfigureerde scan directory op.
     *
     * @return de naam van de directory voor deze configuratie of null
     */
    public String getScanDirectory() {
        return this.getConfig().get(SCAN_DIRECTORY).getValue();
    }

    /**
     * Zoekt de geconfigureerde archief directory op.
     *
     * @return de naam van de directory voor deze configuratie of null
     */
    public String getArchiefDirectory() {
        return this.getConfig().get(ARCHIEF_DIRECTORY).getValue();
    }

    /**
     * Stelt de te scannen directory in.
     *
     * @param scandirectory
     */
    public void setScanDirectory(String scanDirectory) {
        this.getConfig().put(SCAN_DIRECTORY, new ClobElement(scanDirectory));
    }

    /**
     * Stelt de te gebruiken archief directory in.
     *
     * @param archiefdirectory
     */
    public void setArchiefDirectory(String archiefDirectory) {
        this.getConfig().put(ARCHIEF_DIRECTORY, new ClobElement(archiefDirectory));
    }
}
