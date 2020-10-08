/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.auth;

import nl.b3p.brmo.persistence.TestUtil;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * testcases voor {@link nl.b3p.brmo.persistence.auth.Groep }.
 *
 * @author mprins
 */
public class GroepTest extends TestUtil {

    /**
     * test aanmaken en opslaan van groep met een gebruiker.
     *
     * @throws Exception als dan..
     */
    @Test
    public void testGroepMetGebruiker() throws Exception {
        Gebruiker gebA = new Gebruiker();
        gebA.setGebruikersnaam("Gangsta Rapper");
        gebA.changePassword("topsecret");

        Groep g = new Groep();
        g.setBeschrijving(TestUtil.NAAM_BESCHIJVING);
        g.setNaam(TestUtil.NAAM);
        g.getLeden().add(gebA);

        entityManager.persist(g);

        Groep gg = entityManager.find(Groep.class, TestUtil.NAAM);
        assertEquals("Verwacht dezelfde naam voor de groep.", TestUtil.NAAM, gg.getNaam());

        Iterator<Gebruiker> leden = gg.getLeden().iterator();
        Gebruiker lid = null;
        while (leden.hasNext()) {
            lid = leden.next();
        }
        assertEquals(gebA, lid, "Verwacht de gebruiker in de groep.");

        entityManager.remove(g);
        entityManager.getTransaction().commit();
    }
}
