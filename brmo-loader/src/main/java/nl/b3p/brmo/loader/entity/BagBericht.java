package nl.b3p.brmo.loader.entity;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import nl.b3p.brmo.loader.xml.BagXMLReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class BagBericht extends Bericht {

    private Document doc;
    private boolean xpathEvaluated = false;

    private static final Log log = LogFactory.getLog(BagBericht.class);

    private static XPathExpression mutTijdstipVerwerking = null, mutVolgnr = null, mutObjectType = null;
    private static XPathExpression idXPath = null, mutIdXPath = null;

    static {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        try {
            mutTijdstipVerwerking = xpath.compile("/*[local-name() = 'Mutatie-product']/*[local-name()= 'Verwerking']/*[local-name() = 'TijdstipVerwerking']/text()");
            mutVolgnr = xpath.compile("/*[local-name() = 'Mutatie-product']/*[local-name()= 'Verwerking']/*[local-name() = 'VolgnrVerwerking']/text()");
            mutObjectType = xpath.compile("/*[local-name() = 'Mutatie-product']/*[local-name()= 'Verwerking']/*[local-name() = 'ObjectType']/text()");
            mutIdXPath = xpath.compile("/*[local-name() = 'Mutatie-product']/*[local-name() = 'Nieuw']/*/*[local-name() = 'identificatie']/text()");
            idXPath = xpath.compile("/*/*[local-name() = 'identificatie']/text()");
        } catch (XPathExpressionException ex) {
            log.fatal("Fout bij initialiseren XPath expressies", ex);
        }
    }

    public BagBericht(String brXml) {
        super(brXml);
        setSoort("bag");
    }

    public BagBericht(String brXml, Document doc) {
        super(brXml);
        setSoort("bag");
        this.doc = doc;
    }

    /**
     * Deze methode haalt een aantal parameters uit (meestal header van) het bericht.
     * Deze methode vult alleen waarden in indien nog geen waarde bekend is,
     * Soms wordt een waarde, zoals datum, al eerder gezet (overruled).
     */
    private void evaluateXPath() {
        if (xpathEvaluated) {
            return;
        }

        xpathEvaluated = true;
        try {
            String objectType;

            if(doc == null) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                doc = builder.parse(new InputSource(new StringReader(getBrXml())));
            }
            
            String id = null;
            
            if(doc.getDocumentElement().getLocalName().equals(BagXMLReader.MUTATIE_PRODUCT)) {
                if (super.getDatum()==null) {
                    String d = mutTijdstipVerwerking.evaluate(doc);
                    setDatumAsString(d);
                }

                if (super.getVolgordeNummer()==null) {
                    setVolgordeNummer(new Integer(mutVolgnr.evaluate(doc)));
                }
                
                objectType = mutObjectType.evaluate(doc);
                id = mutIdXPath.evaluate(doc);

            } else if(BagXMLReader.lvcProductToObjectType.containsKey(doc.getDocumentElement().getLocalName())) {
                // datum en volgordenr worden elders toegevoegd
                // Levering
                objectType = BagXMLReader.lvcProductToObjectType.get(doc.getDocumentElement().getLocalName());
                id = idXPath.evaluate(doc);
                
            } else {
                throw new IllegalArgumentException("Onbekend BAG bericht: verwacht "
                        + BagXMLReader.MUTATIE_PRODUCT + " of " + BagXMLReader.lvcProductToObjectType.keySet().toString()
                        + " maar \"" + doc.getDocumentElement().getLocalName() + "\" gevonden");
            }

            if (super.getObjectRef()==null) {
                setObjectRef(objectType + ":" + id);
            }

        } catch (Exception e) {
            log.error("Error while getting bag referentie", e);
        }
    }

    public void setDatumAsString(String d) {
        if (d == null || d.isEmpty()) {
            return;
        }

        LocalDateTime date = LocalDateTime.parse(d);
        Date date2 = Date.from(date.atZone(ZoneId.systemDefault()).toInstant());

        setDatum(date2);
    }

    @Override
    public String getObjectRef() {
        evaluateXPath();

        return super.getObjectRef();
    }

    @Override
    public Date getDatum() {
        evaluateXPath();

        return super.getDatum();
    }

    @Override
    public Integer getVolgordeNummer() {
        evaluateXPath();

        return super.getVolgordeNummer();
    }
}
