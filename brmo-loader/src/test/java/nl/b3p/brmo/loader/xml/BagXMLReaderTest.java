package nl.b3p.brmo.loader.xml;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import nl.b3p.brmo.loader.entity.BagBericht;
import org.apache.commons.io.input.CloseShieldInputStream;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Testcases voor {@link nl.b3p.brmo.loader.xml.BagXMLReader}.
 *
 * @author mprins
 */
public class BagXMLReaderTest {

    private static final String mutSmallXml = "9999MUT01012015-02012015-000001.xml";
    // grep -o Nieuw 9999MUT01012015-02012015-000001 | wc -w
    private static final int mutSmallXmlNieuwCount = 2 / 2;

    private static final String mutLargeXML = "9999MUT07022015-08022015-000001.xml";
    private static final String mutLargeXMLReformatted = "9999MUT07022015-08022015-000001-reformatted.xml";
    private static final int mutLargeXmlNieuwCount = 26 / 2;

    private static final String mutZipName = "9999MUT02012015-03012015.zip";
    private static final int mutZipNameNieuwCount
            = 8998 / 2
            + 8680 / 2
            + 8676 / 2
            + 8672 / 2
            + 8668 / 2
            + 7742 / 2;
    private static final String[] mutXmlsInZip = {
        "9999MUT02012015-03012015-000001.xml",
        "9999MUT02012015-03012015-000002.xml",
        "9999MUT02012015-03012015-000003.xml",
        "9999MUT02012015-03012015-000004.xml",
        "9999MUT02012015-03012015-000005.xml",
        "9999MUT02012015-03012015-000006.xml"
    };

    private static final String lvcEmpty = "0197LIG01072014-01072014-000001.xml";
    private static final String lvcSmall = "0197STA01072014-01072014-000001.xml";

    /**
     * Test next() methode met klein mutatie bestand.
     *
     * @throws Exception if any
     */
    @Test
    public void mutTestSmallXML() throws Exception {
        BagXMLReader bReader;
        BagBericht bag = null;
        int total = 0;
        bReader = new BagXMLReader(BagXMLReaderTest.class.getResourceAsStream(mutSmallXml));
        assertTrue(bReader.hasNext());
        while (bReader.hasNext()) {
            bag = bReader.next();
            System.out.println(
                    "datum: " + bag.getDatum()
                    + "\tobj ref: " + bag.getObjectRef()
                    + "\tvolgordenummer: " + bag.getVolgordeNummer()
            );
            total++;
        }
        assertEquals(mutSmallXmlNieuwCount, total);
        assertEquals("PND:1901100000021963", bag.getObjectRef());
        
        LocalDateTime d = LocalDateTime.parse("2015-01-01T07:30:51.843495");
        Date d2 = Date.from(d.atZone(ZoneId.systemDefault()).toInstant());
        assertEquals(d2, bag.getDatum());
        assertEquals(new Integer(0), bag.getVolgordeNummer());
    }

    /**
     * Test next() methode met groter mutatie bestand en vergelijk parsen van file met
     * en zonder extra regeleinden.
     *
     * @throws Exception if any
     */
    @Test
    public void mutTestLargeXML() throws Exception {
        BagXMLReader bReader;
        BagBericht bag = null;
        BagBericht bagReformatted = null;

        int total = 0;
        bReader = new BagXMLReader(BagXMLReaderTest.class.getResourceAsStream(mutLargeXML));
        assertTrue(bReader.hasNext());
        while (bReader.hasNext()) {
            bag = bReader.next();
            total++;
        }
        assertEquals(mutLargeXmlNieuwCount, total);

        total = 0;
        bReader = new BagXMLReader(BagXMLReaderTest.class.getResourceAsStream(mutLargeXMLReformatted));
        assertTrue(bReader.hasNext());
        while (bReader.hasNext()) {
            bagReformatted = bReader.next();
            total++;
        }
        assertEquals(mutLargeXmlNieuwCount, total);

        // vergelijk resultaat van de twee readers
        assertEquals("van beide laatste mutaties moet ObjectRef hetzelfde zijn",
                bag.getObjectRef(), bagReformatted.getObjectRef());
        assertEquals("van beide laatste mutaties moet datum van laatste hetzelfde zijn",
                bag.getDatum(), bagReformatted.getDatum());
        assertEquals("van beide laatste mutaties moet VolgordeNummer hetzelfde zijn",
                bag.getVolgordeNummer(), bagReformatted.getVolgordeNummer());
    }

    /**
     * Test next() methode met grote xml bestanden in een zip met mutaties.
     *
     * @throws Exception if any
     */
    @Test
    public void mutTestZipFile() throws Exception {
        ZipInputStream zis = new ZipInputStream(
                BagXMLReaderTest.class.getResourceAsStream(mutZipName));
        int total = 0;
        try {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                BagXMLReader bagreader = new BagXMLReader(new CloseShieldInputStream(zis));

                while (bagreader.hasNext()) {
                    BagBericht bag = bagreader.next();
                    total++;
                }
                entry = zis.getNextEntry();
            }
        } finally {
            zis.close();
        }

        assertEquals("Verwacht dat het aantal 'Nieuw' mutaties gelijk is.", mutZipNameNieuwCount, total);
    }

    /**
     * Test next() methode met leeg levering bestand.
     *
     * @throws Exception if any
     */
    @Test
    public void testLvcEmptyXML() throws Exception {
        BagXMLReader bReader;
        bReader = new BagXMLReader(BagXMLReaderTest.class.getResourceAsStream(lvcEmpty));
        assertTrue(!bReader.hasNext());
    }

    /**
     * Test next() methode met leeg levering bestand.
     *
     * @throws Exception if any
     */
    @Test
    public void testLvcSmallXML() throws Exception {
        BagXMLReader bReader;
        bReader = new BagXMLReader(BagXMLReaderTest.class.getResourceAsStream(lvcSmall));
        assertTrue(bReader.hasNext());
        BagBericht bag = bReader.next();
        assertEquals("STA:0197030000035441", bag.getObjectRef());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2014-07-01"),
                bag.getDatum());
        assertTrue(bReader.hasNext());
        bag = bReader.next();
        assertEquals("STA:0197030000035442", bag.getObjectRef());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2014-07-01"),
                bag.getDatum());
    }
}
