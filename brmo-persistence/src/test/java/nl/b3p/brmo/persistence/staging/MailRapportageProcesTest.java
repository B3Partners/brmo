/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * test cases voor {@link nl.b3p.brmo.persistence.staging.MailRapportageProces}.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class MailRapportageProcesTest {

    private static final Logger logger = LoggerFactory.getLogger(MailRapportageProcesTest.class);

    private EntityManager entityManager;

    private static final String EEN_ADRES = "test@test.com";

    private static final String[] ADRESLIJST = {"test@test.com", "test.twee@test.com"};

    @Before
    public void setUp() throws Exception {
        final String persistenceUnit = System.getProperty("test.persistence.unit");
        logger.debug("Testing with: {}.", persistenceUnit);
        entityManager = Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager();
        entityManager.getTransaction().begin();
    }

    @After
    public void tearDown() throws Exception {
        entityManager.close();
    }

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
