/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.MailRapportageProces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class MailRapportage extends AbstractExecutableProces {

    private static final Log log = LogFactory.getLog(MailRapportage.class);
    MailRapportageProces config;

    public MailRapportage(MailRapportageProces config) {
        this.config = config;
    }

    @Override
    public void execute() throws BrmoException {
        throw new UnsupportedOperationException("Het sturen van mail moet nog gebouwd worden.");
    }

}
