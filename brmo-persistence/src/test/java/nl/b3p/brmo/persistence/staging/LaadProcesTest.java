/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import nl.b3p.brmo.persistence.TestUtil;
import org.junit.jupiter.api.Test;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testcase voor {@link  nl.b3p.brmo.persistence.staging.LaadProces}.
 *
 * @author mprins
 */
public class LaadProcesTest extends TestUtil {

    @Test
    public void shouldStoreLaadProces() {
        LaadProces p = new LaadProces();
        p.setStatus(LaadProces.STATUS.STAGING_OK);
        entityManager.persist(p);
        Bericht b = new Bericht();
        b.setLaadprocesid(p);
        b.setStatus(Bericht.STATUS.STAGING_OK);
        entityManager.persist(b);
        entityManager.getTransaction().commit();
    }

    @Test
    public void testDuplicateQuery() {
        // zit in nl.b3p.brmo.service.scanner.AbstractExecutableProces#isDuplicaatLaadProces(...)
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LaadProces> criteriaQuery = criteriaBuilder.createQuery(LaadProces.class);
        Root<LaadProces> from = criteriaQuery.from(LaadProces.class);
        CriteriaQuery<LaadProces> select = criteriaQuery.select(from);
        Predicate _bestand_naam = criteriaBuilder.equal(from.get("bestand_naam"), "naam");
        Predicate _soort = criteriaBuilder.equal(from.get("soort"), "soort");
        criteriaQuery.where(criteriaBuilder.and(_bestand_naam, _soort));
        TypedQuery<LaadProces> typedQuery = entityManager.createQuery(select);
        // verwacht dat er geen resultaten zijn / lege lijst
        assertTrue(typedQuery.getResultList().isEmpty());
    }

    @Test
    public void shouldDeleteAllLaadProces() {
        Query q = entityManager.createQuery("DELETE FROM LaadProces");
        int deleted = q.executeUpdate();
        entityManager.getTransaction().commit();
    }

}
