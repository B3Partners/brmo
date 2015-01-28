/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Testcase voor {@link  nl.b3p.brmo.persistence.staging.LaadProces}.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class LaadProcesTest {

    private static final Logger logger = LoggerFactory.getLogger(LaadProcesTest.class);

    private EntityManager entityManager;

    /**
     *
     * @throws Exception if any
     */
    @Before
    public void setUp() throws Exception {
        final String persistenceUnit = System.getProperty("test.persistence.unit");
        logger.debug("Testing with: {}.", persistenceUnit);
        entityManager = Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager();
    }

    @Test
    public void shouldStoreLaadProces() {
        LaadProces p = new LaadProces();
        entityManager.getTransaction().begin();
        entityManager.persist(p);
        Bericht b = new Bericht();
        b.setLaadprocesid(p);
        entityManager.persist(b);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Test
    public void shouldDeleteAllLaadProces() {
        entityManager.getTransaction().begin();
        Query q = entityManager.createQuery("DELETE FROM LaadProces");
        int deleted = q.executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
