/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Testcase voor {@link nl.b3p.brmo.persistence.staging.BAGScannerProces}.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class BAGScannerProcesTest {

    private static final Logger logger = LoggerFactory.getLogger(BAGScannerProcesTest.class);

    private EntityManager entityManager;

    private static final String DIR = "/home/mark/dev/projects/rsgb/brmo-persistence/";

    /**
     *
     * @throws Exception if any
     */
    @Before
    public void setUp() throws Exception {
        final String persistenceUnit = System.getProperty("test.persistence.unit");
        logger.debug("Testing with: {}.", persistenceUnit);
        entityManager = Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager();

        entityManager.getTransaction().begin();
    }

    @After
    public void close() throws Exception {
        entityManager.close();
    }

    /**
     * round trip test van maken en uitlezen van een BAGScannerProces.
     */
    @Test
    public void roundtrip() {
        BAGScannerProces p = new BAGScannerProces();
        p.setScanDirectory(DIR);
        p.setArchiefDirectory(DIR);
        p.getConfig().put("isActive","true");
        entityManager.persist(p);

        final long id = p.getId();
        BAGScannerProces c = entityManager.find(BAGScannerProces.class, id);

        assertEquals("Verwacht dat de scan directory is zoals geconfigureerd.", DIR, c.getScanDirectory());
        assertEquals("Verwacht dat de archief directory is zoals geconfigureerd.", DIR, c.getArchiefDirectory());
        assertEquals("Verwacht dat de parameter is zoals geconfigureerd.", "true", c.getConfig().get("isActive"));

        entityManager.remove(p);
        entityManager.getTransaction().commit();
    }

}
