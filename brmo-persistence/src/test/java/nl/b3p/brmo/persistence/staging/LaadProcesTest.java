/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.Query;
import nl.b3p.brmo.persistence.TestUtil;
import nl.b3p.brmo.persistence.staging.LaadProces.STATUS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Testcase voor {@link  nl.b3p.brmo.persistence.staging.LaadProces}.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class LaadProcesTest extends TestUtil {

    @Test
    public void shouldStoreLaadProces() {
        LaadProces p = new LaadProces();
        p.setStatus(LaadProces.STATUS.STAGING_OK);
        entityManager.persist(p);
        assertTrue(p.getId() > 0);

        Bericht b = new Bericht();
        b.setLaadprocesid(p);
        entityManager.persist(b);
        entityManager.getTransaction().commit();

        assertEquals("Verwacht dezelfde status", STATUS.STAGING_OK, p.getStatus());
    }

    @Test
    public void shouldDeleteAllLaadProces() {
        Query q = entityManager.createQuery("DELETE FROM LaadProces");
        int deleted = q.executeUpdate();
        entityManager.getTransaction().commit();
    }
}
