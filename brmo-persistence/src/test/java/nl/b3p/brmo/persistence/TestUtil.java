/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

/**
 * utility methoden voor unit tests.
 *
 * @author mprins
 */
public abstract class TestUtil {

    /**
     * een lang tekst veld.
     */
    public static final String NAAM_BESCHIJVING = "Brizzle facilisi. Pot faucibizzle that's the shizzle neque. Vestibulizzle vulputate arcu izzle maurizzle. Dizzle shizznit odio i'm in the shizzle ipsizzle. Curabitur owned nibh vizzle owned. Fo shizzle mah nizzle fo rizzle, mah home g-dizzle laoreet, mi eget eleifend i'm in the shizzle, dolor sem bibendum orci, eu that's the shizzle quizzle maurizzle eget get down get down. Etizzle nizzle, lectizzle i saw beyonces tizzles and my pizzle went crizzle aliquet aliquam, tellizzle shizzle my nizzle crocodizzle lacinia orci, yippiyo sagittis nulla izzle things purus. Ghetto aliquet yo mamma break yo neck, yall. In pulvinizzle aliquet own yo'. Praesent gizzle enizzle, boom shackalack nec, away nizzle, fizzle my shizz, crackalackin. Mauris go to hizzle massa quis risizzle bizzle mammasay mammasa mamma oo sa. Vestibulum ullamcorpizzle shut the shizzle up doggy doggy tincidunt lobortis. Phasellizzle dawg mauris. Break it down lacinia. Rizzle fermentizzle da bomb enizzle. Pellentesque mi. Ma nizzle cool ma nizzle sit amet, sizzle dawg elizzle. Shiznit urna crazy, gangster cool, sagittizzle uhuh ... yih!, pharetra eu, neque. Lorem ipsizzle ma nizzle sizzle amizzle, consectetuer you son of a bizzle elizzle.";
    /**
     * een naam.
     */
    public static final String NAAM = "Gangsta's";
    /**
     * een directory.
     */
    public static final String DIR = "/home/mark/dev/projects/rsgb/brmo-persistence/";
    /**
     * een email adres.
     */
    public static final String EEN_ADRES = "test@test.com";
    /**
     * een lijst email adressen.
     */
    public static final String[] ADRESLIJST = {"test@test.com", "test.twee@test.com"};

    private static final Log LOG = LogFactory.getLog(TestUtil.class);

    protected EntityManager entityManager;

    /**
     * Log de naam van de test als deze begint.
     */
    @BeforeEach
    public void startTest(TestInfo testInfo) {
        LOG.info("==== Start test methode: " + testInfo.getDisplayName());
    }
    /**
     * Log de naam van de test als deze eindigt.
     */
    @AfterEach
    public void endTest(TestInfo testInfo) {
        LOG.info("==== Einde test methode: " + testInfo.getDisplayName());
    }

    /**
     * initialisatie van EntityManager {@link #entityManager} en starten
     * transactie.
     *
     * @see #entityManager
     */
    @BeforeEach
    public void setUp() {
        final String persistenceUnit = System.getProperty("test.persistence.unit");
        entityManager = Persistence.createEntityManagerFactory(persistenceUnit).createEntityManager();
        entityManager.getTransaction().begin();
    }

    /**
     * sluiten van van EntityManager {@link #entityManager}.
     *
     * @see #entityManager
     */
    @AfterEach
    public void close() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
    }
}
