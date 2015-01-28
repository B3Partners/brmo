package nl.b3p.brmo.loader.entity;

import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author Boy de Wit
 */
public class BagBericht extends Bericht {

    private final String soort = "bag";
    private String objectRef;
    private Date datum;
    private Integer volgordeNummer;

    private boolean xpathEvaluated = false;

    private static final Log log = LogFactory.getLog(BagBericht.class);

    public BagBericht(String brXml) {
        super(brXml);
    }

    private void evaluateXPath() {
        if (xpathEvaluated) {
            return;
        }

        xpathEvaluated = true;
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(getBrXml())));

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            XPathExpression expr = xpath.compile("/Mutatie-product/*[local-name()= 'Verwerking' or local-name()='Verwerking']/TijdstipVerwerking/text()");
            String datum = expr.evaluate(doc);
            setDatumAsString(datum);

            expr = xpath.compile("/Mutatie-product/*[local-name()= 'Verwerking' or local-name()='Verwerking']/VolgnrVerwerking/text()");
            volgordeNummer = new Integer(expr.evaluate(doc));

            expr = xpath.compile("/Mutatie-product/*[local-name()= 'Verwerking' or local-name()='Verwerking']/ObjectType/text()");
            objectRef = expr.evaluate(doc);

            expr = xpath.compile("/Mutatie-product/*[local-name()= 'Nieuw/*' or local-name()='Nieuw']/*/identificatie/text()");
            String nieuw = expr.evaluate(doc);

            expr = xpath.compile("/Mutatie-product/*[local-name()= 'Wijziging/*' or local-name()='Wijziging']/*/identificatie/text()");
            String wijz = expr.evaluate(doc);

            expr = xpath.compile("/Mutatie-product/*[local-name()= 'Origineel/*' or local-name()='Origineel']/*/identificatie/text()");
            String org = expr.evaluate(doc);

            if (nieuw != null && !nieuw.isEmpty()) {
                objectRef += ":" + nieuw;
            } else if (wijz != null && !wijz.isEmpty()) {
                objectRef += ":" + wijz;
            } else if (org != null && !org.isEmpty()) {
                objectRef += ":" + org;
            }

        } catch (Exception e) {
            log.error("Error while getting bag referentie", e);
        }
    }

    public void setDatumAsString(String d) {
        if (d == null || d.isEmpty()) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'");
        try {
            datum = sdf.parse(d);
        } catch (ParseException pe) {
            log.error("Error while parsing date: " + datum, pe);
        }
    }

    @Override
    public String getObjectRef() {
        evaluateXPath();

        return objectRef;
    }

    @Override
    public Date getDatum() {
        evaluateXPath();

        return datum;
    }

    @Override
    public Integer getVolgordeNummer() {
        evaluateXPath();

        return volgordeNummer;
    }
}
