/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.Entity;

/**
 * Proces configuratie voor de BGT Light directory scanner.
 *
 * @author mprins
 */
@Entity
public class BGTLightScannerProces extends DirectoryScannerProces {
    /**
     * doet niets
     *
     * @return null
     */
    @Override
    public String getArchiefDirectory() {
        return null;
    }

    /**
     * doet niets
     *
     * @param archiefDirectory ignored
     */
    @Override
    public void setArchiefDirectory(String archiefDirectory) {

    }
}
