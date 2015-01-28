/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.servlet;

import javax.persistence.EntityManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import static nl.b3p.brmo.persistence.Manager.getEntityManager;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class EntityManagerContextListener implements ServletContextListener {

    /**
     * initialsatie van de JPA entitymanager.
     *
     * @param sce
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().log("Start initialisatie van de entity manager.");

        //EntityManager em = getEntityManager();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
