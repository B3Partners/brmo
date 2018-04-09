/*
 * Copyright (C) 2018 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.brmo.loader.xml;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.GbavBericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Testcase voor GbavXMLReader.
 *
 * @author Mark Prins
 */
public class GbavXMLReaderTest {

    private static final Log LOG = LogFactory.getLog(GbavXMLReaderTest.class);

    @Test
    public void berichtTest() throws Exception {
        GbavXMLReader r;
        GbavBericht b = null;

        r = new GbavXMLReader(GbavXMLReader.class.getResourceAsStream("gbav-voorbeeld.xml"));
        assertTrue(r.hasNext());
        int total = 0;
        List<String> objectRefs = new ArrayList();
        while (r.hasNext()) {
            b = r.next();
            objectRefs.add(b.getObjectRef());
            LOG.debug(String.format("Bericht #%d van %tF %2$tT, object ref %s",
                    b.getVolgordeNummer(),
                    b.getDatum(),
                    b.getObjectRef()
            ));
            total++;
            assertEquals("Soort komt niet overeen", BrmoFramework.BR_GBAV, b.getSoort());
            assertEquals("Volgordenummer komt niet overeen", 0, b.getVolgordeNummer().intValue());
            assertEquals("Datum komt niet overeen", new Date(2018 - 1900, 1, 1), b.getDatum());
        }

        assertEquals(1, total);
        assertArrayEquals(new String[]{GbavXMLReader.PREFIX + "b3e513383d0454de798b29c6900643130e3b2d6b",}, objectRefs.toArray(new String[]{}));
    }
}
