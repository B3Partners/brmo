/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.auth;

import java.util.Iterator;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import nl.b3p.brmo.persistence.TestUtil;
import nl.b3p.brmo.persistence.staging.BerichtTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class GroepTest {

    private static final Logger logger = LoggerFactory.getLogger(BerichtTest.class);

    private EntityManager entityManager;

    @Before
    public void setUp() throws Exception {
        final String persistenceUnit = System.getProperty("test.persistence.unit");
        logger.debug("Testing with: {}.", persistenceUnit);
        entityManager = Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager();
    }

    @After
    public void tearDown() {
        entityManager.close();
    }

    /**
     * tes aanmaken en opslaan van groep met een gebruiker.
     *
     * @throws Exception als dan..
     */
    @Test
    public void testGroepMetGebruiker() throws Exception {
        entityManager.getTransaction().begin();

        Gebruiker gebA = new Gebruiker();
        gebA.setGebruikersnaam("Gangsta Rapper");
        gebA.changePassword("topsecret");

        Groep g = new Groep();
        g.setBeschrijving(TestUtil.NAAM_BESCHIJVING);
        g.setNaam(TestUtil.NAAM);
        g.getLeden().add(gebA);

        entityManager.persist(g);
        entityManager.getTransaction().commit();

        Groep gg = entityManager.find(Groep.class, TestUtil.NAAM);
        assertEquals("Verwacht dezelfde naam voor de groep.", TestUtil.NAAM, gg.getNaam());

        Iterator<Gebruiker> leden = gg.getLeden().iterator();
        Gebruiker lid = null;
        while (leden.hasNext()) {
            lid = leden.next();
            logger.debug("Gevonden lid {}.", lid);
        }
        assertEquals("Verwacht de gebruiker in de groep.", gebA, lid);
    }
}
