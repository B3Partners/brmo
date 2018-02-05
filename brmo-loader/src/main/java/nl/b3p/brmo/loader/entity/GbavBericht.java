/*
 * Copyright (C) 2018 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
import static nl.b3p.brmo.loader.BrmoFramework.BR_GBAV;
import nl.b3p.brmo.loader.xml.GbavXMLReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 *
 * @author Mark Prins
 */
public class GbavBericht extends Bericht {

    private static final Log LOG = LogFactory.getLog(GbavXMLReader.class);

    private boolean xpathEvaluated = false;

    public GbavBericht(String brXml) {
        super(brXml);
        super.setSoort(BR_GBAV);
    }

    private void evaluateXPath() {
        if (xpathEvaluated) {
            return;
        }

        xpathEvaluated = true;

        if (datum != null) {
            return;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(getBrXml())));

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            // probeel bestandsdataum te achterhalen
            XPathExpression expr = xpath.compile("persoon/categorieen/categorie[nummer='01']/rubrieken/rubriek[nummer='8220']/waarde/text()");
            this.setDatumAsString(expr.evaluate(doc));
            // kan ook op categorie 4 voorkomen
            if (this.getDatum() == null) {
                expr = xpath.compile("persoon/categorieen/categorie[nummer='04']/rubrieken/rubriek[nummer='8220']/waarde/text()");
                this.setDatumAsString(expr.evaluate(doc));
            }
        } catch (Exception e) {
            LOG.error("Error while getting gbav datum", e);
        }
    }

    /**
     * probeer datum uit bericht te halen via xpath.
     *
     * @return datum of {@code null}
     */
    public Date parseDatum() {
        this.evaluateXPath();
        return this.datum;
    }

    public void setDatumAsString(String d) {
        if (d == null || d.isEmpty()) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            this.datum = sdf.parse(d);
        } catch (ParseException pe) {
            LOG.error("Error while parsing date: " + datum, pe);
        }
    }
}
