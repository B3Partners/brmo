package nl.b3p.brmo.loader.xml;

import nl.b3p.brmo.loader.entity.Bag20Bericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Testcases voor {@link Bag20XMLReader}.
 * <p>
 * {@code mvn clean -pl :brmo-loader test -Dtest=Bag20XMLReaderTest > /tmp/mvn.log}
 *
 * @author mprins
 */
public class Bag20XMLReaderTest {

    private static final Log LOG = LogFactory.getLog(Bag20XMLReaderTest.class);
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Test next() methode met klein mutatie bestand.
     *
     * @throws Exception if any
     */
    @Test
    public void testMutatiesXML() throws Exception {
        Bag20XMLReader bReader;
        bReader = new Bag20XMLReader(Bag20XMLReaderTest.class.getResourceAsStream("/bag-2.0/9999MUT01012020-02012020-000001.xml"));

        assertTrue(bReader.hasNext());

        Bag20Bericht bag = null;
        int total = 0;
        while (bReader.hasNext()) {
            bag = bReader.next();
            LOG.debug(
                    "datum: " + bag.getDatum()
                            + "\tobj ref: " + bag.getObjectRef()
                            + "\tvolgordenummer: " + bag.getVolgordeNummer()
            );
            total++;
        }

        // grep -o wordt 9999MUT01012020-02012020-000001.xml | wc -w
        final int numBericht = 32 / 2;
        assertEquals(numBericht, total);

        // laatste mutatie uit bestand
        assertEquals("STA:0106030000000023", bag.getObjectRef());
        assertEquals(SDF.parse("2020-01-01"), bag.getDatum());
        assertEquals(Integer.valueOf(2), bag.getVolgordeNummer());
    }

    /**
     * Test next() methode met leeg levering bestand.
     *
     * @throws Exception if any
     */
    @Test
    public void testLeegXML() throws Exception {
        Bag20XMLReader bReader;
        bReader = new Bag20XMLReader(Bag20XMLReaderTest.class.getResourceAsStream("/bag-2.0/STA01042020_000001-leeg.xml"));
        assertTrue(!bReader.hasNext());
    }

}
