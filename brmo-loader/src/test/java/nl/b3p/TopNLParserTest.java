package nl.b3p;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;
import javax.xml.bind.JAXBException;
import nl.b3p.topnl.Processor;
import nl.b3p.topnl.TopNLType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Deze test dient om onaangekondigde aanpassingen in het schema te vangen, pdok heeft dat nu een
 * paar keer gedaan. Draaien met: {@code mvn -Dit.test=TopNLParserIntegrationTest
 * -Dtest.onlyITs=true -Ppostgresql verify -pl brmo-loader > mvn.log}
 *
 * @author Mark Prins
 */
public class TopNLParserTest {

  private static final Log LOG = LogFactory.getLog(TopNLParserTest.class);

  static Stream<Arguments> argumentsProvider() {
    return Stream.of(
        // {"bestandType","filename"},
        arguments(TopNLType.TOP10NL, "/topnl/top10nl_functioneelgebied.gml"),
        arguments(TopNLType.TOP10NL, "/topnl/top10nl_gebouw.gml"),
        arguments(TopNLType.TOP10NL, "/topnl/top10nl_geografischgebied.gml"),
        arguments(TopNLType.TOP10NL, "/topnl/top10nl_hoogte.gml"),
        arguments(TopNLType.TOP10NL, "/topnl/top10nl_inrichtingselement.gml"),
        arguments(TopNLType.TOP10NL, "/topnl/top10nl_plaats.gml"),
        // leeg bestand
        // arguments(TopNLType.TOP10NL, "/topnl/top10nl_plantopografie.gml"),
        arguments(TopNLType.TOP10NL, "/topnl/top10nl_registratiefgebied.gml"),
        arguments(TopNLType.TOP10NL, "/topnl/top10nl_relief.gml"),
        arguments(TopNLType.TOP10NL, "/topnl/top10nl_spoorbaandeel.gml"),
        arguments(TopNLType.TOP10NL, "/topnl/top10nl_terrein.gml"),
        arguments(TopNLType.TOP10NL, "/topnl/top10nl_waterdeel.gml"),
        arguments(TopNLType.TOP10NL, "/topnl/top10nl_wegdeel.gml"),
        arguments(TopNLType.TOP50NL, "/topnl/top50nl_functioneelgebied.gml"),
        arguments(TopNLType.TOP50NL, "/topnl/top50nl_gebouw.gml"),
        arguments(TopNLType.TOP50NL, "/topnl/top50nl_hoogte.gml"),
        arguments(TopNLType.TOP50NL, "/topnl/top50nl_inrichtingselement.gml"),
        arguments(TopNLType.TOP50NL, "/topnl/top50nl_registratiefgebied.gml"),
        arguments(TopNLType.TOP50NL, "/topnl/top50nl_relief.gml"),
        arguments(TopNLType.TOP50NL, "/topnl/top50nl_spoorbaandeel.gml"),
        arguments(TopNLType.TOP50NL, "/topnl/top50nl_terrein.gml"),
        arguments(TopNLType.TOP50NL, "/topnl/top50nl_waterdeel.gml"),
        arguments(TopNLType.TOP50NL, "/topnl/top50nl_wegdeel.gml"),
        arguments(TopNLType.TOP100NL, "/topnl/top100nl_functioneelgebied.gml"),
        arguments(TopNLType.TOP100NL, "/topnl/top100nl_gebouw.gml"),
        arguments(TopNLType.TOP100NL, "/topnl/top100nl_hoogte.gml"),
        arguments(TopNLType.TOP100NL, "/topnl/top100nl_inrichtingselement.gml"),
        arguments(TopNLType.TOP100NL, "/topnl/top100nl_registratiefgebied.gml"),
        arguments(TopNLType.TOP100NL, "/topnl/top100nl_relief.gml"),
        arguments(TopNLType.TOP100NL, "/topnl/top100nl_spoorbaandeel.gml"),
        arguments(TopNLType.TOP100NL, "/topnl/top100nl_terrein.gml"),
        arguments(TopNLType.TOP100NL, "/topnl/top100nl_waterdeel.gml"),
        arguments(TopNLType.TOP100NL, "/topnl/top100nl_wegdeel.gml"),
        arguments(TopNLType.TOP250NL, "/topnl/top250nl_functioneelgebied.gml"),
        arguments(TopNLType.TOP250NL, "/topnl/top250nl_gebouw.gml"),
        arguments(TopNLType.TOP250NL, "/topnl/top250nl_geografischgebied.gml"),
        arguments(TopNLType.TOP250NL, "/topnl/top250nl_hoogte.gml"),
        arguments(TopNLType.TOP250NL, "/topnl/top250nl_inrichtingselement.gml"),
        arguments(TopNLType.TOP250NL, "/topnl/top250nl_plaats.gml"),
        arguments(TopNLType.TOP250NL, "/topnl/top250nl_registratiefgebied.gml"),
        arguments(TopNLType.TOP250NL, "/topnl/top250nl_relief.gml"),
        arguments(TopNLType.TOP250NL, "/topnl/top250nl_spoorbaandeel.gml"),
        arguments(TopNLType.TOP250NL, "/topnl/top250nl_terrein.gml"),
        arguments(TopNLType.TOP250NL, "/topnl/top250nl_waterdeel.gml"),
        arguments(TopNLType.TOP250NL, "/topnl/top250nl_wegdeel.gml"));
  }

  /** test object voor parsen van bestand. */
  private Processor instance;

  /** Log de naam van de test als deze begint. */
  @BeforeEach
  public void startTest(TestInfo testInfo) {
    LOG.info("==== Start test methode: " + testInfo.getDisplayName());
  }

  /** Log de naam van de test als deze eindigt. */
  @AfterEach
  public void endTest(TestInfo testInfo) {
    LOG.info("==== Einde test methode: " + testInfo.getDisplayName());
  }

  @BeforeEach
  public void setUp() throws JAXBException, SQLException {
    instance = new Processor(null);
  }

  @AfterEach
  public void cleanup() {
    instance = null;
  }

  @DisplayName("Parser test")
  @ParameterizedTest(name = "argumenten rij {index}: type: {0}, bestand: {1}")
  @MethodSource("argumentsProvider")
  public void testParse(final TopNLType bestandType, final String bestandNaam) throws Exception {

    // omdat soms de bestandsnaam wordt aangepast moet dit een harde test fout zijn, dus geen
    // assumeNotNull
    assertNotNull(
        TopNLParserTest.class.getResource(bestandNaam),
        "Het opgehaalde test bestand moet er zijn in deze omgeving.");

    URL in = TopNLParserTest.class.getResource(bestandNaam);
    List jaxb = instance.parse(in);
    assertNotNull(jaxb);
    assertTrue(jaxb.size() > 1, "Er moet meer dan 1 element in de parse lijst zitten");

    switch (bestandType) {
      case TOP10NL:
        assertTrue(
            jaxb.get(0) instanceof nl.b3p.topnl.top10nl.FeatureMemberType,
            "FeatureMemberType is niet correct");
        break;
      case TOP50NL:
        assertTrue(
            jaxb.get(0) instanceof nl.b3p.topnl.top50nl.FeatureMemberType,
            "FeatureMemberType is niet correct");
        break;
      case TOP100NL:
        assertTrue(
            jaxb.get(0) instanceof nl.b3p.topnl.top100nl.FeatureMemberType,
            "FeatureMemberType is niet correct");
        break;
      case TOP250NL:
        assertTrue(
            jaxb.get(0) instanceof nl.b3p.topnl.top250nl.FeatureMemberType,
            "FeatureMemberType is niet correct");
        break;
      default:
        fail("onbekend bestandtype " + bestandType);
    }
  }
}
