/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import nl.b3p.brmo.persistence.TestUtil;
import org.junit.jupiter.api.Test;

/**
 * test cases voor {@link nl.b3p.brmo.persistence.staging.MailRapportageProces}.
 *
 * @author mprins
 */
public class MailRapportageProcesTest extends TestUtil {

  /** Test met een enkelvoudig adres. */
  @Test
  public void testMailAdres() {
    MailRapportageProces m = new MailRapportageProces();
    m.setMailAdressen(EEN_ADRES);
    entityManager.persist(m);
    assertEquals(
        EEN_ADRES, m.getMailAdressenArray()[0], "Verwacht dat opgeslagen email adres identiek is.");
    assertEquals(
        EEN_ADRES, m.getMailAdressen(), "Verwacht dat opgeslagen email adres identiek is.");

    entityManager.remove(m);
    entityManager.getTransaction().commit();
  }

  /** Test meerdere adressen. */
  @Test
  public void testMailAdressen() {
    MailRapportageProces m = new MailRapportageProces();
    m.setMailAdressen(ADRESLIJST);
    entityManager.persist(m);

    assertArrayEquals(
        ADRESLIJST,
        m.getMailAdressenArray(),
        "Verwacht dat opgeslagen email adressen identiek zijn.");

    entityManager.remove(m);
    entityManager.getTransaction().commit();
  }

  /** test filtering op id en status. */
  @Test
  public void testRapportageLijst() {
    BRK2ScannerProces p2 = new BRK2ScannerProces();
    p2.setScanDirectory(DIR);
    p2.getConfig().put("isActive", new ClobElement("true"));
    p2.setStatus(AutomatischProces.ProcessingStatus.ERROR);
    p2.setSamenvatting(NAAM_BESCHIJVING);
    entityManager.persist(p2);
    final long pId2 = p2.getId();

    BRK2ScannerProces p3 = new BRK2ScannerProces();
    p3.setScanDirectory(DIR);
    p3.getConfig().put("isActive", new ClobElement("true"));
    p3.setStatus(AutomatischProces.ProcessingStatus.WAITING);
    p3.setSamenvatting(NAAM_BESCHIJVING);
    entityManager.persist(p3);
    final long pId3 = p3.getId();

    MailRapportageProces m = new MailRapportageProces();
    m.setMailAdressen(ADRESLIJST);
    m.setForStatus(AutomatischProces.ProcessingStatus.ERROR);
    m.getConfig().put(MailRapportageProces.PIDS, new ClobElement(pId2 + "," + pId3));
    entityManager.persist(m);

    List<Predicate> predicates = new ArrayList<>();
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
      List<Long> pidLijst = new ArrayList<>();
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
    assertEquals(1, list.size(), "Het aantal processen met status ERROR.");

    entityManager.remove(p2);
    entityManager.remove(m);
    entityManager.getTransaction().commit();
  }
}
