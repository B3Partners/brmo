/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.digikoppeling;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 * Testcase voor {@link nl.b3p.brmo.digikoppeling.XMLUtil}.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public final class XMLUtilTest {

    /**
     * round-trip unit test.
     */
    @Test
    public void testDates() {
        Date earlier = XMLUtil.getDate(XMLUtil.getNow());
        Date now = new Date();
        assertThat("Vroeger eerder is dan later.", now.after(earlier), is(true));

        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        assertThat("Timestamps moeten nagenoeg hetzelfde zijn", formatter.format(earlier), equalTo(formatter.format(now)));
    }

}
