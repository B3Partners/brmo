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

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.GbavBericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

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
        GbavBericht b;
        assumeFalse(null == GbavXMLReaderTest.class.getResourceAsStream("gbav-voorbeeld.xml"), "testdata moet aanwezig zijn");
        r = new GbavXMLReader(GbavXMLReader.class.getResourceAsStream("gbav-voorbeeld.xml"));
        assertTrue(r.hasNext());
        int total = 0;
        List<String> objectRefs = new ArrayList<>();
        while (r.hasNext()) {
            b = r.next();
            objectRefs.add(b.getObjectRef());
            LOG.debug(String.format("Bericht #%d van %tF %2$tT, object ref %s",
                    b.getVolgordeNummer(),
                    b.getDatum(),
                    b.getObjectRef()
            ));
            total++;
            assertEquals(BrmoFramework.BR_GBAV, b.getSoort(), "Soort komt niet overeen");
            assertEquals(0, b.getVolgordeNummer().intValue(), "Volgordenummer komt niet overeen");
            assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2018-02-01"), b.getDatum(),
                    "Datum komt niet overeen");
        }

        assertEquals(1, total);
        assertArrayEquals(new String[]{GbavXMLReader.PREFIX + "b3e513383d0454de798b29c6900643130e3b2d6b",}, objectRefs.toArray(new String[]{}));
    }

    @Test
    public void meerderePersonenTest() throws Exception {
        GbavXMLReader r;
        GbavBericht b;

        assumeFalse(null == GbavXMLReaderTest.class.getResourceAsStream("persoonslijst.xml"), "testdata moet aanwezig zijn");
        r = new GbavXMLReader(GbavXMLReaderTest.class.getResourceAsStream("persoonslijst.xml"));
        assertTrue(r.hasNext());
        int total = 0;
        List<String> objectRefs = new ArrayList<>();
        while (r.hasNext()) {
            b = r.next();
            objectRefs.add(b.getObjectRef());
            LOG.debug(String.format("Bericht #%d van %tF %2$tT, object ref %s",
                    b.getVolgordeNummer(),
                    b.getDatum(),
                    b.getObjectRef()
            ));
            assertEquals(BrmoFramework.BR_GBAV, b.getSoort(), "Soort komt niet overeen");
            assertEquals(total, b.getVolgordeNummer().intValue(), "Volgordenummer komt niet overeen");
            total++;
        }

        assertEquals(4, total);
        assertArrayEquals(new String[]{
                        GbavXMLReader.PREFIX + "a1672c578792e79cadd113ae5ccfd3a867dde805",
                        GbavXMLReader.PREFIX + "200ba4cd285de672506eea37179f25e988511188",
                        GbavXMLReader.PREFIX + "b8600b644b291dd67909c54ac87b46e6c1a38a1f",
                        GbavXMLReader.PREFIX + "9d0ca6e9a6d67bf098ffa88d2df6281805b5b0a0"
                },
                objectRefs.toArray(new String[]{})
        );
    }
}
