/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.digikoppeling;

import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * XML/JAXB serialisatie utilities.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public final class XMLUtil {

    /**
     * private constructor.
     */
    private XMLUtil() {
    }

    /**
     *
     * @return huidige datum/tijd als XMLGregorianCalendar.
     */
    public static XMLGregorianCalendar getNow() {
        GregorianCalendar c = (GregorianCalendar) GregorianCalendar.getInstance();
        c.setTime(new Date());
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException ex) {
            return null;
        }
    }

    /**
     *
     * @param xcal
     * @return geconverteerde datum
     */
    public static Date getDate(XMLGregorianCalendar xcal) {
        return xcal.toGregorianCalendar().getTime();
    }
}
