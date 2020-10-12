/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import nl.b3p.brmo.persistence.TestUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testcase voor {@link nl.b3p.brmo.persistence.staging.BAGScannerProces}.
 *
 * @author mprins
 */
public class BAGScannerProcesTest extends TestUtil {

    /**
     * round trip test van maken en uitlezen van een BAGScannerProces.
     */
    @Test
    public void roundtrip() {
        BAGScannerProces p = new BAGScannerProces();
        p.setScanDirectory(DIR);
        p.setArchiefDirectory(DIR);
        p.getConfig().put("isActive", new ClobElement("true"));
        entityManager.persist(p);

        final long id = p.getId();
        BAGScannerProces c = entityManager.find(BAGScannerProces.class, id);

        assertEquals(DIR, c.getScanDirectory(), "Verwacht dat de scan directory is zoals geconfigureerd.");
        assertEquals(DIR, c.getArchiefDirectory(), "Verwacht dat de archief directory is zoals geconfigureerd.");
        assertEquals("true", c.getConfig().get("isActive").getValue(), "Verwacht dat de parameter is zoals geconfigureerd.");

        entityManager.remove(p);
        entityManager.getTransaction().commit();
    }
}
