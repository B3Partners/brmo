package nl.b3p.brmo.stufbg204;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.brmo.stufbg204.util.StUFbg204Util;
import nl.egem.stuf.sector.bg._0204.AsynchroonAntwoordBericht;
import nl.egem.stuf.sector.bg._0204.KennisgevingsBericht;
import nl.egem.stuf.sector.bg._0204.VraagBericht;
import nl.egem.stuf.stuf0204.BevestigingsBericht;
import nl.egem.stuf.stuf0204.FoutBericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author mprins
 */
@WebService(
        serviceName = "StUFBGAsynchroon",
        portName = "StUFBGAsynchronePort",
        endpointInterface = "nl.egem.stuf.sector.bg._0204.StUFBGAsynchroonPortType",
        targetNamespace = "http://www.egem.nl/StUF/sector/bg/0204",
        wsdlLocation = "WEB-INF/wsdl/bg0204.wsdl"
)
@HandlerChain(file = "/handler-chain.xml")
public class StUFBGasynchroon {

    private static final Log LOG = LogFactory.getLog(StUFBGasynchroon.class);

    public BevestigingsBericht ontvangAsynchroneVraag(VraagBericht vraag) {
        LOG.debug("Er is een vraag ontvangen van soort: " + vraag.getStuurgegevens().getBerichtsoort());
        BevestigingsBericht b = new BevestigingsBericht();
        b.setStuurgegevens(StUFbg204Util.maakStuurgegevens(vraag.getStuurgegevens()));

        return b;
    }

    public BevestigingsBericht ontvangAsynchroonAntwoord(AsynchroonAntwoordBericht asynchroonAntwoord) {
        LOG.debug("Er is een antwoord ontvangen van soort: " + asynchroonAntwoord.getStuurgegevens().getBerichtsoort());
        BevestigingsBericht b = new BevestigingsBericht();
        b.setStuurgegevens(StUFbg204Util.maakStuurgegevens(asynchroonAntwoord.getStuurgegevens()));

        return b;
    }

    public BevestigingsBericht ontvangFout(FoutBericht fout) {
        LOG.debug("Er is een fout ontvangen van soort: " + fout.getStuurgegevens().getBerichtsoort());
        BevestigingsBericht b = new BevestigingsBericht();
        b.setStuurgegevens(StUFbg204Util.maakStuurgegevens(fout.getStuurgegevens()));

        return b;
    }

    /**
     *
     * 4.2 Stuurgegevens voor kennisgevingberichten. In paragraaf 2.3 zijn vier
     * varianten binnen een kennisgevingbericht onderkend:
     *
     * <h3>Toevoeging</h3>
     * Bij een toevoeging is in het zendende systeem een occurrence toegevoegd,
     * omdat is vastgesteld dat in de werkelijkheid een voor het zendende
     * systeem relevant object bestaat.
     *
     * <h3>Wijziging</h3>
     * Bij een wijziging is in het zendende systeem een occurrence gewijzigd,
     * omdat is vastgesteld dat er in de werkelijkheid eigenschappen (gegevens)
     * zijn veranderd van het object waar naar die occurrence verwijst.
     *
     * <h3>Verwijdering</h3>
     * Bij een verwijdering is in het zendende systeem een occurrence
     * verwijderd, omdat is vastgesteld dat in de werkelijkheid het object
     * waarnaar de occurrence verwijst, niet meer bestaat of niet meer relevant
     * is voor het zendende systeem.
     *
     * <h3>Correctie</h3>
     * Bij een correctie is in het zendende systeem een occurrence gewijzigd,
     * omdat is vastgesteld dat de vastgelegde waarden niet correct waren. Bij
     * een correctie is het object in de werkelijkheid waarnaar de occurrence
     * verwijst, zelf niet gewijzigd. Deze verschillende varianten worden als
     * volgt gecodeerd in het stuurgegeven mutatiesoort:
     * <ul>
     * <li>‘T’: Toevoeging
     * <li>‘W’: Wijziging
     * <li>‘V’: Verwijdering
     * <li>‘C’: Correctie
     * </ul>
     * Het is aan het ontvangende systeem om te interpreteren in hoeverre een
     * kennisgeving relevant is en of de kennisgeving het gevolg is van het
     * ontstaan of verdwijnen van een object c.q. van het relevant worden of
     * niet meer relevant zijn van het object voor het zendende systeem. Het
     * kennisgevingbericht bevat geen aanduiding van de gebeurtenis die
     * aanleiding gaf tot de wijziging van de gegevens (zie paragraaf 2.3).
     *
     * <br/>
     * Naast de mutatiesoort is in een kennisgevingbericht ook relevant hoe het
     * ontvangende systeem geacht wordt te reageren op het bericht. Een
     * kennisgevingbericht kan puur informatief bedoeld zijn: het ontvangende
     * systeem mag zelf beslissen of de kennisgeving al dan niet wordt verwerkt
     * in de eigen gegevens. Daarnaast kan het verplicht zijn voor het
     * ontvangende systeem om de gegevens over te nemen. Of een
     * kennisgevingbericht informatief is of verplicht over te nemen geeft het
     * stuurgegeven indicator overname aan met 'I' (informatief) respectievelijk
     * 'V' (verplicht). Aanvullende afspraken over de omgang met deze rubriek
     * kunnen worden vastgelegd in het sectormodel.
     *
     * @param kennisgeving te verwerken kennisgevingsbericht
     * @return een bevestiging van de kennisgeving
     */
    public BevestigingsBericht ontvangKennisgeving(KennisgevingsBericht kennisgeving) {
        LOG.debug("Er is een kennisgeving ontvangen van soort: " + kennisgeving.getStuurgegevens().getBerichtsoort()
                + " en mutatiesoort " + kennisgeving.getStuurgegevens().getKennisgeving().getMutatiesoort());
        BevestigingsBericht b = new BevestigingsBericht();
        b.setStuurgegevens(StUFbg204Util.maakStuurgegevens(kennisgeving.getStuurgegevens()));

        switch (kennisgeving.getStuurgegevens().getKennisgeving().getMutatiesoort()) {
            case T:
                saveBericht(kennisgeving, kennisgeving.getStuurgegevens().getTijdstipBericht());
                break;
            case W:
                // dit werkt -nog- niet omdat dit 2 PRS nodes bevat, een was en een wordt...
                saveBericht(kennisgeving, kennisgeving.getStuurgegevens().getKennisgeving().getTijdstipMutatie());
                break;
            case V:
            case C:
            default:
                LOG.warn("Onbekende mutatiesoort wordt niet verwerkt.");
                break;
        }
        return b;
    }

    private void saveBericht(Object body, String datum) {
        try {
            DataSource ds = ConfigUtil.getDataSourceStaging();
            BrmoFramework brmo = new BrmoFramework(ds, null);
            InputStream in = getXml(body);

            Date d = StUFbg204Util.sdf.parse(datum);
            String bestand_naam = "StUF-BG upload op " + StUFbg204Util.sdf.format(new Date());

            brmo.loadFromStream(BrmoFramework.BR_BRP, in, bestand_naam, d, null);
            brmo.closeBrmoFramework();
        } catch (BrmoException ex) {
            LOG.error("Fout tijdens laden van StUF-BG bericht", ex);
        } catch (JAXBException | ParseException ex) {
            LOG.error("Fout tijdens parsen van bericht", ex);
        }

    }

    private InputStream getXml(Object o) throws JAXBException {

        try {
            // maak van POJO een inputstream
            Marshaller jaxbMarshaller = StUFbg204Util.getStufJaxbContext().createMarshaller();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            jaxbMarshaller.marshal(o, baos);
            InputStream in = new ByteArrayInputStream(baos.toByteArray());

            // maak er een document van
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(in);
            List<SimpleEntry<String, String>> prefixes = new ArrayList<>();
            getPrefixesRecursive(doc.getDocumentElement(), prefixes);
            //haal de body eruit met xpath
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("//*[local-name() = 'body']/*");
            NodeList nodelist = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
            Node root = doc.createElement("root");

            for (int i = 0; i < nodelist.getLength(); i++) {
                Node n = nodelist.item(i);
                root.appendChild(n);
            }

            // Vertaal xml naar inputstream voor verwerking in brmo framework
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.transform(new DOMSource(root), new StreamResult(outputStream));
            InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

            return is;
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | TransformerException ex) {
            LOG.error("Cannot parse body", ex);
        }
        return null;
    }

    private static final String XMLNAMESPACE = "xmlns";

    public static void getPrefixesRecursive(Element element, List<SimpleEntry<String, String>> prefixes) {
        getPrefixes(element, prefixes);
        Node parent = element.getParentNode();
        if (parent instanceof Element) {
            getPrefixesRecursive((Element) parent, prefixes);
        }
    }

    /**
     * Get all prefixes defined on this element for the specified namespace.
     *
     * @param element dom element
     * @param prefixes lijst van namespace prefixes
     */
    public static void getPrefixes(Element element, List<SimpleEntry<String, String>> prefixes) {
        NamedNodeMap atts = element.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Node node = atts.item(i);
            String name = node.getNodeName();
            if (name != null && (XMLNAMESPACE.equals(name) || name.startsWith(XMLNAMESPACE + ":"))) {
                SimpleEntry s = new SimpleEntry(name, node.getNodeValue());
                prefixes.add(s);
            }
        }
    }

}
