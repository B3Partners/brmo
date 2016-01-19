package nl.b3p.brmo.loader.entity;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    private static XPathExpression idXPath = null;

    static {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        try {
            mutTijdstipVerwerking = xpath.compile("/*[local-name() = 'Mutatie-product']/*[local-name()= 'Verwerking' or local-name()='Verwerking']/*[local-name() = 'TijdstipVerwerking']/text()");
            mutVolgnr = xpath.compile("/*[local-name() = 'Mutatie-product']/*[local-name()= 'Verwerking' or local-name()='Verwerking']/*[local-name() = 'VolgnrVerwerking']/text()");
            mutObjectType = xpath.compile("/*[local-name() = 'Mutatie-product']/*[local-name()= 'Verwerking' or local-name()='Verwerking']/*[local-name() = 'ObjectType']/text()");
            idXPath = xpath.compile("//*[local-name() = 'identificatie']/text()");
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

            if(doc.getDocumentElement().getLocalName().equals(BagXMLReader.MUTATIE_PRODUCT)) {
                String d = mutTijdstipVerwerking.evaluate(doc);
                setDatumAsString(d);

                setVolgordeNummer(new Integer(mutVolgnr.evaluate(doc)));
                objectType = mutObjectType.evaluate(doc);

            } else if(BagXMLReader.lvcProductToObjectType.containsKey(doc.getDocumentElement().getLocalName())) {
                // Levering

                setVolgordeNummer(-1);

                String n = doc.getDocumentElement().getLocalName();

                objectType = BagXMLReader.lvcProductToObjectType.get(n);
                if(objectType == null) {
                    throw new IllegalArgumentException("Onbekend BAG product: " + n);
                }
            } else {
                throw new IllegalArgumentException("Onbekend BAG bericht: verwacht "
                        + BagXMLReader.MUTATIE_PRODUCT + " of " + BagXMLReader.lvcProductToObjectType.keySet().toString()
                        + " maar \"" + doc.getDocumentElement().getLocalName() + "\" gevonden");
            }

            String id = idXPath.evaluate(doc);
            setObjectRef(objectType + ":" + id);

        } catch (Exception e) {
            log.error("Error while getting bag referentie", e);
        }
    }

    public void setDatumAsString(String d) {
        if (d == null || d.isEmpty()) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'.'");
        try {
            setDatum(sdf.parse(d));
        } catch (ParseException pe) {
            log.error("Error while parsing date: " + d, pe);
        }
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
