/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.Query;
import nl.b3p.brmo.persistence.TestUtil;
import org.junit.Test;

/**
 * Testcase voor {@link  nl.b3p.brmo.persistence.staging.Bericht}.
 *
 * @author mprins
 */
public class BerichtTest extends TestUtil {

    @Test
    public void shouldStoreBericht() {
        Bericht b = new Bericht();
        b.setStatus(Bericht.STATUS.RSGB_OK);
        entityManager.persist(b);
        entityManager.getTransaction().commit();
    }

    @Test
    public void shouldDeleteAllBericht() {
        Query q = entityManager.createQuery("DELETE FROM Bericht");
        int deleted = q.executeUpdate();
        entityManager.getTransaction().commit();
    }
}
