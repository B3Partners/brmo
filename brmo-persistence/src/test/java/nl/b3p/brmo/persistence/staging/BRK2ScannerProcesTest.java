/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import static nl.b3p.brmo.persistence.staging.AutomatischProces.LOG_NEWLINE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import nl.b3p.brmo.persistence.TestUtil;
import org.junit.jupiter.api.Test;

/**
 * Testcase voor {@link nl.b3p.brmo.persistence.staging.BRK2ScannerProces}.
 *
 * @author mprins
 */
public class BRK2ScannerProcesTest extends TestUtil {

  /** round trip test van maken, opslaan en uitlezen van een BRK2ScannerProces. */
  @Test
  public void roundtrip() {
    BRK2ScannerProces p = new BRK2ScannerProces();
    p.setScanDirectory(DIR);
    p.getConfig().put("isActive", new ClobElement("true"));
    entityManager.persist(p);

    final long id = p.getId();

    BRK2ScannerProces c = entityManager.find(BRK2ScannerProces.class, id);
    assertEquals(DIR, c.getScanDirectory(), "De directory is zoals geconfigureerd.");
    assertEquals(
        "true",
        c.getConfig().get("isActive").getValue(),
        "Verwacht dat de parameter is zoals geconfigureerd.");

    entityManager.remove(c);
    entityManager.getTransaction().commit();
  }

  /**
   * testcase voor {@link
   * nl.b3p.brmo.persistence.staging.AutomatischProces#updateSamenvattingEnLogfile(java.lang.String)}
   */
  @Test
  public void testUpdateSamenvattingEnLogfile() {
    BRK2ScannerProces p = new BRK2ScannerProces();
    p.updateSamenvattingEnLogfile(NAAM_BESCHIJVING);
    entityManager.persist(p);
    final long id = p.getId();

    BRK2ScannerProces c = entityManager.find(BRK2ScannerProces.class, id);
    assertEquals(
        c.getLogfile(),
        c.getSamenvatting(),
        "Verwacht dat de logfile en de samenvatting hetzelfde zijn.");

    c.updateSamenvattingEnLogfile(NAAM);

    final String TWEEDE = "2e entry-";

    c.updateSamenvattingEnLogfile(TWEEDE + NAAM_BESCHIJVING);

    assertEquals(
        NAAM_BESCHIJVING + LOG_NEWLINE + NAAM + LOG_NEWLINE + TWEEDE + NAAM_BESCHIJVING,
        c.getLogfile());
    entityManager.merge(c);

    String[] s = p.getLogfile().split(NAAM);
    String expected = s[0].replace(LOG_NEWLINE, "");
    String actual = s[1].substring(TWEEDE.length() + 1).replace(LOG_NEWLINE, "");
    assertEquals(expected, actual, "Verwacht dat de logfile twee dezelfde delen bevat.");

    entityManager.remove(c);
    entityManager.getTransaction().commit();
  }
}
