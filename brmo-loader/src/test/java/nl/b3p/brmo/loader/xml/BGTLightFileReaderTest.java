/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.xml;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author Mark Prins
 */
@RunWith(Parameterized.class)
public class BGTLightFileReaderTest {

    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");

    /**
     * test parameter.
     */
    private final String bestandNaam;
    /**
     * test parameter.
     */
    private final String datum;
    /**
     * test expectation.
     */
    private final String gebied;

    @Parameters
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"name","date", "gebied"},
            {"/home/mark/dev/projects/brmo/bgt-gml-loader/src/test/resources/gmllight/zips/data/38468_0-20160429.zip", "20160429", "grid 38468 aggrlevel 0"},
            {"38468_0-20160429.zip", "20160429", "grid 38468 aggrlevel 0"}
        });
    }

    public BGTLightFileReaderTest(String bestandNaam, String datum, String gebied) {
        this.bestandNaam = bestandNaam;
        this.datum = datum;
        this.gebied = gebied;
    }

    @Test
    public void testSetBestandsNaam() throws Exception {
        BGTLightFileReader r = new BGTLightFileReader(null);
        r.setBestandsNaam(bestandNaam);

        assertEquals("Naam moet identiek zijn", bestandNaam, r.getBestandsNaam());
        assertEquals("Gebied moet overeen komen", gebied, r.getGebied());
        assertEquals("Datum moet overeen komen", fmt.parse(datum), r.getBestandsDatum());
    }

    @Test
    public void testBGTLightXMLReader() throws Exception {
        BGTLightFileReader r = new BGTLightFileReader(bestandNaam);

        assertEquals("Naam moet identiek zijn", bestandNaam, r.getBestandsNaam());
        assertEquals("Gebied moet overeen komen", gebied, r.getGebied());
        assertEquals("Datum moet overeen komen", fmt.parse(datum), r.getBestandsDatum());
    }
}
