/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

/**
 * Levert een aantal gedeelde methoden voor directory gebaseerde processen.
 *
 * @author mprins
 */
public abstract class DirectoryScannerProces extends AutomatischProces {

    private static final String SCAN_DIRECTORY = "scandirectory";

    private static final String ARCHIEF_DIRECTORY = "archiefdirectory";

    /**
     * Zoekt de geconfigureerde scan directory op.
     *
     * @return de naam van de directory voor deze configuratie of null
     */
    public String getScanDirectory() {
        return ClobElement.nullSafeGet(this.getConfig().get(SCAN_DIRECTORY));
    }

    /**
     * Zoekt de geconfigureerde archief directory op.
     *
     * @return de naam van de directory voor deze configuratie of null
     */
    public String getArchiefDirectory() {
        return ClobElement.nullSafeGet(this.getConfig().get(ARCHIEF_DIRECTORY));
    }

    /**
     * Stelt de te scannen directory in.
     *
     * @param scanDirectory te scannen directory
     */
    public void setScanDirectory(String scanDirectory) {
        if (scanDirectory == null) {
            this.getConfig().put(SCAN_DIRECTORY, null);
        } else {
            this.getConfig().put(SCAN_DIRECTORY, new ClobElement(scanDirectory));
        }
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
