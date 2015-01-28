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
 * Testcase voor {@link  nl.b3p.brmo.persistence.staging.Bericht}.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class BerichtTest {

    private static final Logger logger = LoggerFactory.getLogger(BerichtTest.class);

    private EntityManager entityManager;

    @Before
    public void setUp() throws Exception {
        final String persistenceUnit = System.getProperty("test.persistence.unit");
        logger.debug("Testing with: {}.", persistenceUnit);
        entityManager = Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager();
    }

    @Test
    public void shouldStoreBericht() {
        Bericht b = new Bericht();
        entityManager.getTransaction().begin();
        entityManager.persist(b);
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    @Test
    public void shouldDeleteAllBericht() {
        entityManager.getTransaction().begin();
        Query q = entityManager.createQuery("DELETE FROM Bericht");
        int deleted = q.executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.close();
    }
}
