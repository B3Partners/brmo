package nl.b3p.brmo.loader.entity;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.xml.Bag20XMLReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Bag20Bericht extends Bericht {

    private static final Log log = LogFactory.getLog(Bag20Bericht.class);
    private static XPathExpression mutTijdstipVerwerking = null, mutVolgnr = null, mutObjectType = null;
    private static XPathExpression idXPath = null, mutIdXPath = null;

    static {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        try {
            mutTijdstipVerwerking = xpath.compile("/*[local-name() = 'mutatieGroep']/*/*[local-name()= 'voorkomen']/*[local-name()= 'Voorkomen']/*[local-name() = 'tijdstipRegistratie']/text()");
            mutVolgnr = xpath.compile("/*[local-name() = 'mutatieGroep']/*/*[local-name()= 'voorkomen']/*[local-name()= 'Voorkomen']/*[local-name() = 'voorkomenidentificatie']/text()");
            mutObjectType = xpath.compile("/*[local-name() = 'mutatieGroep']/*/*[local-name()= 'bagObject']/*[local-name() = 'ObjectType']/text()");
            mutIdXPath = xpath.compile("/*[local-name() = 'mutatieGroep']/*[local-name() = 'toevoeging']/*/*[local-name() = 'identificatie']/*[local-name()='lokaalID']/text()");
            idXPath = xpath.compile("/*/*[local-name() = 'identificatie']/*[local-name()='lokaalID']/text()");
        } catch (XPathExpressionException ex) {
            log.fatal("Fout bij initialiseren XPath expressies", ex);
        }
    }

    private Document doc;
    private boolean xpathEvaluated = false;

    public Bag20Bericht(String brXml) {
        super(brXml);
        setSoort("bag20");
    }

    public Bag20Bericht(String brXml, Document doc) {
        super(brXml);
        setSoort(BrmoFramework.BR_BAG20);
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

            if (doc == null) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                doc = builder.parse(new InputSource(new StringReader(getBrXml())));
            }

            String id;

            if (doc.getDocumentElement().getLocalName().equals(Bag20XMLReader.MUTATIE_PRODUCT)) {
                if (super.getDatum() == null) {
                    String d = mutTijdstipVerwerking.evaluate(doc);
                    setDatumAsString(d);
                }

                if (super.getVolgordeNummer() == null) {
                    String _volgordeNr = mutVolgnr.evaluate(doc);
                    if (StringUtils.isBlank(_volgordeNr)){
                        _volgordeNr = "1";
                    }
                    setVolgordeNummer(new Integer(_volgordeNr));
                }

                objectType = mutObjectType.evaluate(doc);
                id = mutIdXPath.evaluate(doc);

            } else if (Bag20XMLReader.lvcProductToObjectType.containsKey(doc.getDocumentElement().getLocalName())) {
                // datum en volgordenr worden elders toegevoegd
                // Levering
                objectType = Bag20XMLReader.lvcProductToObjectType.get(doc.getDocumentElement().getLocalName());
                id = idXPath.evaluate(doc);

            } else {
                throw new IllegalArgumentException("Onbekend BAG bericht: verwacht "
                        + Bag20XMLReader.MUTATIE_PRODUCT + " of " + Bag20XMLReader.lvcProductToObjectType.keySet().toString()
                        + " maar \"" + doc.getDocumentElement().getLocalName() + "\" gevonden");
            }

            if (super.getObjectRef() == null) {
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
