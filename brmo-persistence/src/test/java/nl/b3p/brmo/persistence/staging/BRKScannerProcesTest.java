/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Testcase voor {@link nl.b3p.brmo.persistence.staging.BRKScannerProces}.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class BRKScannerProcesTest {

    private static final Logger logger = LoggerFactory.getLogger(BRKScannerProcesTest.class);

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

    /**
     *
     * @throws Exception if any
     */
    @After
    public void close() throws Exception {
        entityManager.close();
    }

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
