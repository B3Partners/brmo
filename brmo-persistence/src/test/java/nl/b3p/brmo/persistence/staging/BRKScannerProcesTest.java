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

        entityManager.remove(c);
        entityManager.getTransaction().commit();
    }

    @Test
    public void testUpdateSamenvattingEnLogfile() {
        BRKScannerProces p = new BRKScannerProces();
        p.updateSamenvattingEnLogfile(NAAM_BESCHIJVING);
        entityManager.persist(p);
        final long id = p.getId();

        BRKScannerProces c = entityManager.find(BRKScannerProces.class, id);
        assertEquals("Verwacht dat de logfile en de samenvatting hetzelfde zijn.", c.getLogfile(), c.getSamenvatting());

        c.updateSamenvattingEnLogfile(NAAM);
        c.updateSamenvattingEnLogfile("2e\n" + NAAM_BESCHIJVING);
        assertEquals(NAAM_BESCHIJVING + NAAM + "2e\n" + NAAM_BESCHIJVING, c.getLogfile());
        entityManager.merge(c);

        String[] s = p.getLogfile().split(NAAM);
        assertEquals("Verwacht dat de logfile twee dezelfde delen bevat.", "2e\n" + s[0], s[1]);

        entityManager.remove(c);
        entityManager.getTransaction().commit();
    }
}
