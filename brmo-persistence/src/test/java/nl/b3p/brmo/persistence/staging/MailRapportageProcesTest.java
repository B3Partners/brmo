/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import nl.b3p.brmo.persistence.TestUtil;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

/**
 * test cases voor {@link nl.b3p.brmo.persistence.staging.MailRapportageProces}.
 *
 * @author mprins
 */
public class MailRapportageProcesTest extends TestUtil {

    /**
     * Test met een enkelvoudig adres.
     */
    @Test
    public void testMailAdres() {
        MailRapportageProces m = new MailRapportageProces();
        m.setMailAdressen(EEN_ADRES);
        entityManager.persist(m);
        assertEquals("Verwacht dat opgeslagen email adres identiek is.",
                EEN_ADRES, m.getMailAdressenArray()[0]);
        assertEquals("Verwacht dat opgeslagen email adres identiek is.",
                EEN_ADRES, m.getMailAdressen());

        entityManager.remove(m);
        entityManager.getTransaction().commit();
    }

    /**
     * Test meerdere adressen.
     */
    @Test
    public void testMailAdressen() {
        MailRapportageProces m = new MailRapportageProces();
        m.setMailAdressen(ADRESLIJST);
        entityManager.persist(m);

        assertArrayEquals("Verwacht dat opgeslagen email adressen identiek zijn.",
                ADRESLIJST, m.getMailAdressenArray());

        entityManager.remove(m);
        entityManager.getTransaction().commit();
    }

    /**
     * test filtering op id en status.
     */
    @Test
    public void testRapportageLijst() {
        BAGScannerProces p = new BAGScannerProces();
        p.setScanDirectory(DIR);
        p.setArchiefDirectory(DIR);
        p.getConfig().put("isActive", new ClobElement("true"));
        p.setStatus(AutomatischProces.ProcessingStatus.ERROR);
        p.setSamenvatting(NAAM_BESCHIJVING);
        entityManager.persist(p);
        final long pId = p.getId();

        BRKScannerProces p2 = new BRKScannerProces();
        p2.setScanDirectory(DIR);
        p2.getConfig().put("isActive", new ClobElement("true"));
        p2.setStatus(AutomatischProces.ProcessingStatus.ERROR);
        p2.setSamenvatting(NAAM_BESCHIJVING);
        entityManager.persist(p2);
        final long pId2 = p2.getId();

        BRKScannerProces p3 = new BRKScannerProces();
        p3.setScanDirectory(DIR);
        p3.getConfig().put("isActive", new ClobElement("true"));
        p3.setStatus(AutomatischProces.ProcessingStatus.WAITING);
        p3.setSamenvatting(NAAM_BESCHIJVING);
        entityManager.persist(p3);
        final long pId3 = p3.getId();

        MailRapportageProces m = new MailRapportageProces();
        m.setMailAdressen(ADRESLIJST);
        m.setForStatus(AutomatischProces.ProcessingStatus.ERROR);
        m.getConfig().put(MailRapportageProces.PIDS, new ClobElement(pId + "," + pId2 + "," + pId3));
        entityManager.persist(m);

        List<Predicate> predicates = new ArrayList<Predicate>();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AutomatischProces> cq = cb.createQuery(AutomatischProces.class);
        Root<AutomatischProces> from = cq.from(AutomatischProces.class);

        // where= filter
        if (m.getForStatus() != null) {
            Predicate where = cb.equal(from.get("status"), m.getForStatus());
            predicates.add(where);
        }
        // id in... filter
        String pids = m.getConfig().get(MailRapportageProces.PIDS).getValue();
        if (pids != null) {
            List<Long> pidLijst = new ArrayList<Long>();
            Matcher match = (Pattern.compile("[0-9]+")).matcher(pids);
            while (match.find()) {
                pidLijst.add(Long.valueOf(match.group()));
            }
            Predicate in = from.get("id").in(pidLijst);
            predicates.add(in);
        }
        // niet eigen proces rapporteren
        Predicate notIn = from.get("id").in(m.getId()).not();
        predicates.add(notIn);

        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        cq.orderBy(cb.asc(from.get("status")));

        List<AutomatischProces> list = entityManager.createQuery(cq).getResultList();
        assertThat("Het aantal processen met status ERROR.", 2, is(list.size()));

        entityManager.remove(p);
        entityManager.remove(m);
        entityManager.getTransaction().commit();
    }

}
