/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.Query;
import nl.b3p.brmo.persistence.TestUtil;
import org.junit.Test;

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
    public void shouldDeleteAllLaadProces() {
        Query q = entityManager.createQuery("DELETE FROM LaadProces");
        int deleted = q.executeUpdate();
        entityManager.getTransaction().commit();
    }
}
