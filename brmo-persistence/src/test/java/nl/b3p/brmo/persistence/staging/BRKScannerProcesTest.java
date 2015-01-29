/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import nl.b3p.brmo.persistence.TestUtil;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Testcase voor {@link nl.b3p.brmo.persistence.staging.BRKScannerProces}.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class BRKScannerProcesTest extends TestUtil {

    /**
     * round trip test van maken, opslaan en uitlezen van een BRKScannerProces.
     */
    @Test
    public void roundtrip() {
        BRKScannerProces p = new BRKScannerProces();
        p.setScanDirectory(DIR);
        p.getConfig().put("isActive", "true");
        entityManager.persist(p);

        final long id = p.getId();

        BRKScannerProces c = entityManager.find(BRKScannerProces.class, id);
        assertEquals("De directory is zoals geconfigureerd.", DIR, c.getScanDirectory());
        assertEquals("Verwacht dat de parameter is zoals geconfigureerd.", "true", c.getConfig().get("isActive"));

        //   entityManager.remove(p);
        entityManager.getTransaction().commit();
    }
}
