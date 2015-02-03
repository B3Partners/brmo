/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import nl.b3p.brmo.persistence.TestUtil;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Testcase voor {@link  nl.b3p.brmo.persistence.staging.Bericht}.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class BerichtTest extends TestUtil {

    @Test
    public void shouldStoreAndRemoveBericht() {
        Bericht b = new Bericht();
        b.setStatus(Bericht.STATUS.RSGB_OK);
        entityManager.persist(b);

        assertTrue(b.getId() > 0);
        entityManager.remove(b);
        entityManager.getTransaction().commit();
    }

    /**
     * test {@code select count(*) bericht}.
     */
    @Test
    public void testCount() {
        final int loopCount = 50;
        for (int i = 0; i < loopCount; i++) {
            Bericht b = new Bericht();
            b.setStatus(Bericht.STATUS.RSGB_OK);
            entityManager.persist(b);
        }
        entityManager.getTransaction().commit();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(Bericht.class)));
        long count = entityManager.createQuery(cq).getSingleResult();
        assertEquals("verwacht 50 berichten te tellen.", loopCount, count);

        // opruimen
        entityManager.getTransaction().begin();
        Query q = entityManager.createQuery("DELETE FROM Bericht");
        count = q.executeUpdate();
        entityManager.getTransaction().commit();
        assertEquals("verwacht 50 berichten te verwijderen.", loopCount, count);
    }

    /**
     * test {@code select from bericht b where b.status in (RSGB_OK, RSGB_NOK);}
     */
    @Test
    public void testCountSubset() {
        final int loopCount = 50;
        for (int i = 0; i < loopCount; i++) {
            Bericht b = new Bericht();
            if (i % 2 == 0) {
                b.setStatus(Bericht.STATUS.RSGB_OK);
            } else {
                b.setStatus(Bericht.STATUS.RSGB_WAITING);
            }
            entityManager.persist(b);
        }
        entityManager.getTransaction().commit();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Bericht> cqB = cb.createQuery(Bericht.class);
        Root<Bericht> bericht = cqB.from(Bericht.class);

        List<Bericht.STATUS> ins = new ArrayList<Bericht.STATUS>();
        ins.add(Bericht.STATUS.RSGB_OK);
        Expression<String> exp = bericht.get("status");
        Predicate predicate = exp.in(ins);
        cqB.where(predicate);

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(Bericht.class)));
        List<Bericht> results = entityManager.createQuery(cqB).getResultList();
        long count = results.size();
        assertEquals("verwacht 25 berichten te tellen in de resultatenlijst.", loopCount / 2, count);
    }

    /**
     * test
     * {@code select count(b) from bericht b where b.status in (RSGB_OK, RSGB_NOK) and b.soort in ('TEST');}.
     */
    @Test
    public void testCountWhereIn() {
        // 50 + 1 berichten maken en opslaan
        final int loopCount = 50;
        for (int i = 0; i < loopCount; i++) {
            Bericht b = new Bericht();
            if (i % 2 == 0) {
                b.setStatus(Bericht.STATUS.RSGB_OK);
                b.setSoort("TEST");
            } else {
                b.setStatus(Bericht.STATUS.RSGB_WAITING);
                b.setSoort("TEST");
            }
            entityManager.persist(b);
        }
        Bericht b = new Bericht();
        b.setStatus(Bericht.STATUS.RSGB_NOK);
        b.setSoort("TEST");
        entityManager.persist(b);
        entityManager.getTransaction().commit();

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = builder.createQuery(Long.class);
        Root<Bericht> from = cq.from(Bericht.class);

        // status in... predicate
        List<Bericht.STATUS> berichtStatussen = new ArrayList<Bericht.STATUS>();
        berichtStatussen.add(Bericht.STATUS.RSGB_OK);
        berichtStatussen.add(Bericht.STATUS.RSGB_NOK);
        Expression<String> exp = from.get("status");
        Predicate predicate = exp.in(berichtStatussen);

        // soort in... predicate
        List<String> berichtSoorten = Arrays.asList(new String[]{"TEST"});
        Predicate predicate1 = from.get("soort").in(berichtSoorten);

        Predicate[] p = {predicate, predicate1};
        // cq.where(builder.and(p));

        // select count...
        CriteriaQuery<Long> select = cq.select(builder.count(from));
        select.where(builder.and(p));

        long count = entityManager.createQuery(select).getSingleResult();
        assertEquals("verwacht 25+1 berichten te tellen.", loopCount / 2 + 1, count);
    }

    @After
    public void cleanup() {
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        Query q = entityManager.createQuery("DELETE FROM Bericht");
        int deleted = q.executeUpdate();
        entityManager.getTransaction().commit();
    }
}
