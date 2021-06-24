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
        // topsecret
        gebA.setWachtwoord("8fa631010049979238a9cd090e389b43$100000$eb75b4879b4e159289091933a86ce7901eca6d0067f04ebabc5cb96af84e3ba1");

        Groep g = new Groep();
        g.setBeschrijving(TestUtil.NAAM_BESCHIJVING);
        g.setNaam(TestUtil.NAAM);
        g.getLeden().add(gebA);

        entityManager.persist(g);

        Groep gg = entityManager.find(Groep.class, TestUtil.NAAM);
        assertEquals(TestUtil.NAAM, gg.getNaam(), "Verwacht dezelfde naam voor de groep.");

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
