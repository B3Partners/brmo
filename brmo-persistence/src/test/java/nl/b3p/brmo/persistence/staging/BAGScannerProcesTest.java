/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import nl.b3p.brmo.persistence.TestUtil;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

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

        assertEquals("Verwacht dat de scan directory is zoals geconfigureerd.", DIR, c.getScanDirectory());
        assertEquals("Verwacht dat de archief directory is zoals geconfigureerd.", DIR, c.getArchiefDirectory());
        assertEquals("Verwacht dat de parameter is zoals geconfigureerd.", "true", c.getConfig().get("isActive").getValue());

        entityManager.remove(p);
        entityManager.getTransaction().commit();
    }

}
