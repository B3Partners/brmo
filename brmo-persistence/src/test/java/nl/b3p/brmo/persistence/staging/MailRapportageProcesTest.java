/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import nl.b3p.brmo.persistence.TestUtil;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

/**
 * test cases voor {@link nl.b3p.brmo.persistence.staging.MailRapportageProces}.
 *
 * @author Mark Prins <mark@b3partners.nl>
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
                EEN_ADRES, m.getMailAdressen()[0]);

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
                ADRESLIJST, m.getMailAdressen());

        entityManager.remove(m);
        entityManager.getTransaction().commit();
    }

}
