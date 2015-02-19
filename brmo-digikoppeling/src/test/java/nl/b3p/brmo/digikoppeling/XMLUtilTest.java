/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.digikoppeling;

import java.util.Date;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Testcase voor {@link nl.b3p.brmo.digikoppeling.XMLUtil}.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public final class XMLUtilTest {

    @Test
    public void testDates() {
        Date earlier = XMLUtil.getDate(XMLUtil.getNow());
        Date now = new Date();
        assertThat("Vroeger eerder is dan later.",now.after(earlier),is(true));
    }

}
