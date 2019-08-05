package nl.b3p;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import nl.b3p.brmo.test.util.database.JTDSDriverBasedFailures;
import nl.b3p.brmo.test.util.database.MSSqlServerDriverBasedFailures;
import nl.b3p.topnl.Processor;
import nl.b3p.topnl.TopNLType;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.experimental.categories.Category;

/**
 * Deze test dient om onaangekondigde aanpassingen in het schema te vangen, pdok
 * heeft dat nu een paar keer gedaan. Draaien met:
 * {@code mvn -Dit.test=TopNLParserIntegrationTest -Dtest.onlyITs=true -Ppostgresql verify > target/TopNLParserIntegrationTest.log}
 *
 * @author Mark Prins
 */
@RunWith(Parameterized.class)
// overslaan voor mssql profielen
@Category({JTDSDriverBasedFailures.class, MSSqlServerDriverBasedFailures.class})
public class TopNLParserIntegrationTest {

    private static final Log LOG = LogFactory.getLog(TopNLParserIntegrationTest.class);

    @Parameterized.Parameters(name = "argumenten rij {index}: type: {0}, bestand: {1}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"bestandType","filename"},
            {TopNLType.TOP10NL, "/topnl/TOP10NL_07W.gml"},
            {TopNLType.TOP50NL, "/topnl/Top50NL_07W.gml"},
            {TopNLType.TOP100NL, "/topnl/Top100NL_000001.gml"},
            {TopNLType.TOP250NL, "/topnl/TOP250NL.gml"}
        });
    }

    /**
     * test parameter.
     */
    private final String bestandNaam;
    /**
     * test parameter.
     */
    private final TopNLType bestandType;
    /**
     * logging rule.
     */
    @Rule
    public TestName name = new TestName();

    /**
     * test object voor parsen van bestand.
     */
    private Processor instance;

    public TopNLParserIntegrationTest(TopNLType bestandType, String bestandNaam) {
        this.bestandType = bestandType;
        this.bestandNaam = bestandNaam;
    }

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

    @Before
    public void setUp() throws JAXBException, SQLException {
        instance = new Processor(null);
    }

    @After
    public void cleanup() {
        instance = null;
    }

    @Test
    public void testParse() throws JAXBException, IOException {

        // omdat soms de bestandsnaam wordt aangepast moet dit een harde test fout zijn, dus geen assumeNotNull
        assertNotNull("Het opgehaalde test bestand moet er zijn in deze omgeving.", TopNLParserIntegrationTest.class.getResource(bestandNaam));

        URL in = TopNLParserIntegrationTest.class.getResource(bestandNaam);
        List jaxb = instance.parse(in);
        assertNotNull(jaxb);
        assertTrue("Er moet meer dan 1 element in de parse lijst zitten", jaxb.size() > 1);

        switch (this.bestandType) {
            case TOP10NL:
                assertTrue("FeatureMemberType is niet correct", jaxb.get(0) instanceof nl.b3p.topnl.top10nl.FeatureMemberType);
                break;
            case TOP50NL:
                assertTrue("FeatureMemberType is niet correct", jaxb.get(0) instanceof nl.b3p.topnl.top50nl.FeatureMemberType);
                break;
            case TOP100NL:
                assertTrue("FeatureMemberType is niet correct", jaxb.get(0) instanceof nl.b3p.topnl.top100nl.FeatureMemberType);
                break;
            case TOP250NL:
                assertTrue("FeatureMemberType is niet correct", jaxb.get(0) instanceof nl.b3p.topnl.top250nl.FeatureMemberType);
                break;
            default:
                fail("onbekend bestandtype");
        }
    }
}
