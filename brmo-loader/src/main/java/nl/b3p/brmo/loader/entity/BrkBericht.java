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
public class BrkBericht extends Bericht {

    private final String soort = "brk";
    private boolean xpathEvaluated = false;

    private static final Log log = LogFactory.getLog(BrkBericht.class);

    public BrkBericht(String brXml) {
        super(brXml);
    }

    public void setVervallenInfo(String objectRef, Date datum) {
        this.objectRef = objectRef;
        this.datum = datum;

        xpathEvaluated = true;
    }

    private void evaluateXPath() {
        if (xpathEvaluated) {
            return;
        }

        xpathEvaluated = true;
        
        if (objectRef!=null && datum!=null) {
            //al uit de header gehaald
            return;
        }

        // dit moet dus een standberciht zijn
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(getBrXml())));

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            
            XPathExpression expr = xpath.compile("/KadastraalObjectSnapshot/*[local-name()= 'Perceel' or local-name()='Appartementsrecht']/identificatie/namespace/text()");
            objectRef = expr.evaluate(doc);

            expr = xpath.compile("/KadastraalObjectSnapshot/*[local-name()= 'Perceel' or local-name()='Appartementsrecht']/identificatie/lokaalId/text()");
            objectRef += ":" + expr.evaluate(doc);

            expr = xpath.compile("/KadastraalObjectSnapshot/*[local-name()= 'toestandsdatum' or local-name()='toestandsdatum']/text()");
            setDatumAsString(expr.evaluate(doc));
        } catch (Exception e) {
            log.error("Error while getting brk referentie", e);
        }
    }

    public void setDatumAsString(String d) {
        if (d == null || d.isEmpty()) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
}
