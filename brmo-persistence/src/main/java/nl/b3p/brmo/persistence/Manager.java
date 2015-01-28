/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/**
 * Persistence manager voor de brmo staging database.
 *
 * @author Mark Prins <mark@b3partners.nl>
 *
 * @since 1.1
 */
public final class Manager {

    private static EntityManager entityManager = null;

    /**
     * utility om de entity manager op te halen met behulp van JNDI
     * {@code jdbc/brmo/staging} of de omgevingsvariable
     * {@code test.persistence.unit}, ten behoeve van unit en integratie tests.
     *
     * @return de ons bekende EntityManager
     *
     */
    public static EntityManager getEntityManager() {
        if (entityManager == null || !entityManager.isOpen()) {
            entityManager = Persistence.createEntityManagerFactory("brmo.persistence.oracle").createEntityManager();
        }
        return entityManager;
    }

    /**
     * utility om de entity manager op te halen. .
     *
     * @return de ons bekende EntityManager
     * @param persistenceUnit naam van de PU
     *
     */
    public static EntityManager getEntityManager(String persistenceUnit) {
        if (entityManager == null || entityManager.isOpen()) {
            entityManager = Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager();
        }
        return entityManager;
    }

    /**
     * private constructor voor deze utility klasse.
     */
    private Manager() {
    }
}
