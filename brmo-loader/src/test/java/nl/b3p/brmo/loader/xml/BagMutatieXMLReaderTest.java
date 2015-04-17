package nl.b3p.brmo.loader.xml;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import nl.b3p.brmo.loader.entity.BagBericht;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Testcases voor {@link nl.b3p.brmo.loader.xml.BagMutatieXMLReader}.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class BagMutatieXMLReaderTest {

    private static final String smallXml = "9999MUT01012015-02012015-000001.xml";
    // grep -o Nieuw 9999MUT01012015-02012015-000001 | wc -w
    private static final int smallXmlNieuwCount = 2 / 2;

    private static final String largeXML = "9999MUT07022015-08022015-000001.xml";
    private static final String largeXMLReformatted = "9999MUT07022015-08022015-000001-reformatted.xml";
    private static final int largeXmlNieuwCount = 26 / 2;

    private static final String zipName = "9999MUT02012015-03012015.zip";
    private static final int zipNameNieuwCount
            = 8998 / 2
            + 8680 / 2
            + 8676 / 2
            + 8672 / 2
            + 8668 / 2
            + 7742 / 2;
    private static final String[] xmlsInZip = {
        "9999MUT02012015-03012015-000001.xml",
        "9999MUT02012015-03012015-000002.xml",
        "9999MUT02012015-03012015-000003.xml",
        "9999MUT02012015-03012015-000004.xml",
        "9999MUT02012015-03012015-000005.xml",
        "9999MUT02012015-03012015-000006.xml"
    };

    /**
     * Test next() methode met klein bestand.
     *
     * @throws Exception if any
     */
    @Test
    public void testSmallXML() throws Exception {
        BagMutatieXMLReader bReader;
        BagBericht bag = null;
        int total = 0;
        bReader = new BagMutatieXMLReader(BagMutatieXMLReaderTest.class.getResourceAsStream(smallXml));
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
        assertEquals(smallXmlNieuwCount, total);
        assertEquals("PND:1901100000021963", bag.getObjectRef());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'").parse("2015-01-01T07:30:51.843495"),
                bag.getDatum());
        assertEquals(new Integer(0), bag.getVolgordeNummer());
    }

    /**
     * Test next() methode met groter bestand en vergelijk parsen van file met
     * en zonder extra regeleinden.
     *
     * @throws Exception if any
     */
    @Test
    public void testLargeXML() throws Exception {
        BagMutatieXMLReader bReader;
        BagBericht bag = null;
        BagBericht bagReformatted = null;

        int total = 0;
        bReader = new BagMutatieXMLReader(BagMutatieXMLReaderTest.class.getResourceAsStream(largeXML));
        assertTrue(bReader.hasNext());
        while (bReader.hasNext()) {
            bag = bReader.next();
            total++;
        }
        assertEquals(largeXmlNieuwCount, total);

        total = 0;
        bReader = new BagMutatieXMLReader(BagMutatieXMLReaderTest.class.getResourceAsStream(largeXMLReformatted));
        assertTrue(bReader.hasNext());
        while (bReader.hasNext()) {
            bagReformatted = bReader.next();
            total++;
        }
        assertEquals(largeXmlNieuwCount, total);

        // vergelijk resultaat van de twee readers
        assertEquals("van beide laatste mutaties moet ObjectRef hetzelfde zijn",
                bag.getObjectRef(), bagReformatted.getObjectRef());
        assertEquals("van beide laatste mutaties moet datum van laatste hetzelfde zijn",
                bag.getDatum(), bagReformatted.getDatum());
        assertEquals("van beide laatste mutaties moet VolgordeNummer hetzelfde zijn",
                bag.getVolgordeNummer(), bagReformatted.getVolgordeNummer());
    }

    /**
     * Test next() methode met grote xml bestanden in een zip.
     *
     * @throws Exception if any
     */
    @Test
    public void testZipFile() throws Exception {
        ZipInputStream zis = new ZipInputStream(
                BagMutatieXMLReaderTest.class.getResourceAsStream(zipName));
        int total = 0;
        try {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                // bagreader met een byte[] voeden om voortijdig sluiten van de (zip)inputstream te voorkomen
                byte[] xml = IOUtils.toByteArray(zis);
                BagMutatieXMLReader bagreader = new BagMutatieXMLReader(new ByteArrayInputStream(xml));

                while (bagreader.hasNext()) {
                    BagBericht bag = bagreader.next();
                    total++;
                }
                entry = zis.getNextEntry();
            }
        } finally {
            zis.close();
        }

        assertEquals("Verwacht dat het aantal 'Nieuw' mutaties gelijk is.", zipNameNieuwCount, total);
    }

}
