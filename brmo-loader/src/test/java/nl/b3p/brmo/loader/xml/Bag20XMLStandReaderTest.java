package nl.b3p.brmo.loader.xml;

import nl.b3p.brmo.loader.entity.Bag20Bericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.stream.Stream;

import static nl.b3p.brmo.loader.BrmoFramework.BR_BAG20;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Testcases voor {@link Bag20XMLReader}.
 * <p>
 * {@code mvn clean -pl :brmo-loader test -Dtest=Bag20XMLStandReaderTest > /tmp/mvn.log}
 *
 * @author mprins
 */
public class Bag20XMLStandReaderTest {

    private static final Log LOG = LogFactory.getLog(Bag20XMLStandReaderTest.class);
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // ("bestandNaam","datum", "objectRef",totalObj),
                arguments(File.separator + "bag-2.0" + File.separator + "LIG.xml", "2020-03-01", "LIG:0479020000000202",
                        1),
                arguments(File.separator + "bag-2.0" + File.separator + "NUM.xml", "2020-03-01", "NUM:0479200000015860",
                        1),
                arguments(File.separator + "bag-2.0" + File.separator + "OPR.xml", "2020-03-01", "OPR:0479300000000002",
                        1),
                arguments(File.separator + "bag-2.0" + File.separator + "PND.xml", "2020-03-01", "PND:0003100000142936",
                        1),
                arguments(File.separator + "bag-2.0" + File.separator + "STA.xml", "2020-03-01", "STA:0479030000000001",
                        1),
                arguments(File.separator + "bag-2.0" + File.separator + "VBO.xml", "2020-03-01", "VBO:0479010000081514",
                        1),
                arguments(File.separator + "bag-2.0" + File.separator + "WPL.xml", "2020-03-01", "WPL:1878", 1),
                // leeg stand bestand
                arguments(File.separator + "bag-2.0" + File.separator + "STA01042020_000001-leeg.xml", "2020-04-01",
                        null, 0),
                // grep -o "Objecten:Pand" 0106PND01012020_000001.xml | wc -w / 2
                arguments(File.separator + "bag-2.0" + File.separator + "0106PND01012020_000001.xml", "2020-01-01",
                        null, 10000 / 2)
        );
    }

    /**
     * Log de naam van de test als deze begint.
     */
    @BeforeEach
    public void startTest(TestInfo testInfo) {
        LOG.info("==== Start test methode: " + testInfo.getDisplayName());
    }

    /**
     * Log de naam van de test als deze eindigt.
     */
    @AfterEach
    public void endTest(TestInfo testInfo) {
        LOG.info("==== Einde test methode: " + testInfo.getDisplayName());
    }

    @DisplayName("BAG 2.0 stand XML berichten")
    @ParameterizedTest(name = "{index}: bestand: {0}")
    @MethodSource("argumentsProvider")
    public void testXML(String bestandNaam, String datum, String objectRef, int totalObj)
            throws Exception {
        Bag20XMLReader bReader;
        Bag20Bericht bag = null;
        int total = 0;

        bReader = new Bag20XMLReader(Bag20XMLStandReaderTest.class.getResourceAsStream(bestandNaam));

        if (totalObj >= 1) {
            assertTrue(bReader.hasNext());
            while (bReader.hasNext()) {
                bag = bReader.next();
                total++;
            }

            assertNotNull(bag);
            assertEquals(BR_BAG20, bag.getSoort());
            if (totalObj == 1) {
                assertEquals(objectRef, bag.getObjectRef());
                assertEquals(-1, bag.getVolgordeNummer().intValue());
                assertEquals(datum, SDF.format(bag.getDatum()));
            }
        }
        assertEquals(totalObj, total);
    }

}
