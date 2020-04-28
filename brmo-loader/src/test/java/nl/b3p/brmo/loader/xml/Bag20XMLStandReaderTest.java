package nl.b3p.brmo.loader.xml;

import nl.b3p.brmo.loader.entity.Bag20Bericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static nl.b3p.brmo.loader.BrmoFramework.BR_BAG20;
import static org.junit.Assert.*;

/**
 * Testcases voor {@link Bag20XMLReader}.
 * <p>
 * {@code mvn clean -pl :brmo-loader test -Dtest=Bag20XMLStandReaderTest > /tmp/mvn.log}
 *
 * @author mprins
 */
@RunWith(Parameterized.class)
public class Bag20XMLStandReaderTest {

    private static final Log LOG = LogFactory.getLog(Bag20XMLStandReaderTest.class);
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * test parameter.
     */
    private final String bestandNaam;
    /**
     * test parameter.
     */
    private final Date datum;
    /**
     * test parameter.
     */
    private final String objectRef;
    /**
     * test parameter.
     */
    private final int totalObj;

    /**
     * logging rule.
     */
    @Rule
    public TestName name = new TestName();

    /**
     * Log de naam van de test als deze begint.
     */
    @Before
    public void startTest() {
        LOG.info("==== Start test methode: " + name.getMethodName());
    }

    /**
     * Log de naam van de test als deze eindigt.
     */
    @After
    public void endTest() {
        LOG.info("==== Einde test methode: " + name.getMethodName());
    }

    public Bag20XMLStandReaderTest(String bestandNaam, String datum, String objectRef, int totalObj) throws ParseException {
        this.bestandNaam = bestandNaam;
        this.datum = SDF.parse(datum);
        this.objectRef = objectRef;
        this.totalObj = totalObj;
    }

    @Parameterized.Parameters(name = "{index}: bestand: {0}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
                // {"bestandNaam","datum", "objectRef",totalObj},
                {File.separator + "bag-2.0" + File.separator + "LIG.xml", "2020-03-01", "LIG:0479020000000202", 1},
                {File.separator + "bag-2.0" + File.separator + "NUM.xml", "2020-03-01", "NUM:0479200000015860", 1},
                {File.separator + "bag-2.0" + File.separator + "OPR.xml", "2020-03-01", "OPR:0479300000000002", 1},
                {File.separator + "bag-2.0" + File.separator + "PND.xml", "2020-03-01", "PND:0003100000142936", 1},
                {File.separator + "bag-2.0" + File.separator + "STA.xml", "2020-03-01", "STA:0479030000000001", 1},
                {File.separator + "bag-2.0" + File.separator + "VBO.xml", "2020-03-01", "VBO:0479010000081514", 1},
                {File.separator + "bag-2.0" + File.separator + "WPL.xml", "2020-03-01", "WPL:1878", 1},
        });
    }

    @Test
    public void testXML() throws ParserConfigurationException, XMLStreamException, TransformerException, SAXException, IOException {
        Bag20XMLReader bReader;
        Bag20Bericht bag = null;
        int total = 0;

        bReader = new Bag20XMLReader(Bag20XMLStandReaderTest.class.getResourceAsStream(bestandNaam));

        assertTrue(bReader.hasNext());
        while (bReader.hasNext()) {
            bag = bReader.next();
            total++;
        }

        assertNotNull(bag);
        assertEquals(totalObj, total);
        assertEquals(BR_BAG20, bag.getSoort());
        assertEquals(objectRef, bag.getObjectRef());
        assertEquals(-1, bag.getVolgordeNummer().intValue());
        assertEquals(datum, bag.getDatum());
    }

}
