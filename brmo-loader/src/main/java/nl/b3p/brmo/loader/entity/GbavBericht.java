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
import java.util.*;
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
    private boolean hasAddedBSNHashes = false;

    private String bsn = null;

    private List<String> bsnList = new ArrayList<>();

    public GbavBericht(String brXml) {
        super(brXml);
        super.setSoort(BR_GBAV);
    }

    private void evaluateXPath() {
        if (xpathEvaluated) {
            return;
        }

        xpathEvaluated = true;

        if (datum != null && bsn != null && !bsnList.isEmpty()) {
            return;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(getBrXml())));

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            // probeer bestandsdatum te achterhalen
            XPathExpression expr = xpath.compile("persoon/categorieen/categorie[nummer='01']/rubrieken/rubriek[nummer='8220']/waarde/text()");
            this.setDatumAsString(expr.evaluate(doc));
            // kan ook op categorie 4 voorkomen
            if (this.getDatum() == null) {
                expr = xpath.compile("persoon/categorieen/categorie[nummer='04']/rubrieken/rubriek[nummer='8220']/waarde/text()");
                this.setDatumAsString(expr.evaluate(doc));
            }

            // van de verschillende personen BSN uitlezen en hashen
            expr = xpath.compile("persoon/categorieen/categorie[nummer='01']/rubrieken/rubriek[nummer='0120']/waarde/text()");
            this.bsn = expr.evaluate(doc);
            this.bsnList.add(bsn);
            // ouder 1
            expr = xpath.compile("persoon/categorieen/categorie[nummer='02']/rubrieken/rubriek[nummer='0120']/waarde/text()");
            String _bsn = expr.evaluate(doc);
            if (_bsn != null && _bsn != "") {
                this.bsnList.add(_bsn);
            }
            // ouder 2
            expr = xpath.compile("persoon/categorieen/categorie[nummer='03']/rubrieken/rubriek[nummer='0120']/waarde/text()");
            _bsn = expr.evaluate(doc);
            if (_bsn != null && _bsn != "") {
                this.bsnList.add(_bsn);
            }
            // partner
            expr = xpath.compile("persoon/categorieen/categorie[nummer='05']/rubrieken/rubriek[nummer='0120']/waarde/text()");
            _bsn = expr.evaluate(doc);
            if (_bsn != null && _bsn != "") {
                this.bsnList.add(_bsn);
            }
            // kind
            expr = xpath.compile("persoon/categorieen/categorie[nummer='09']/rubrieken/rubriek[nummer='0120']/waarde/text()");
            _bsn = expr.evaluate(doc);
            if (_bsn != null && _bsn != "") {
                this.bsnList.add(_bsn);
            }
        } catch (Exception e) {
            LOG.error("Fout tijdens parsen van gbav xml", e);
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

    public String getBsn() {
        this.evaluateXPath();
        return this.bsn;
    }

    public List<String> getBsnList() {
        this.evaluateXPath();
        return this.bsnList;
    }

    public void setBsnMap(Map<String, String> bsnHashes) {
        if (!hasAddedBSNHashes) {
            StringBuilder sb = new StringBuilder("<bsnhashes>\n");
            for (Map.Entry<String, String> entry : bsnHashes.entrySet()) {
                if (!entry.getKey().isEmpty() && !entry.getValue().isEmpty()) {
                    sb.append("<").append(GbavXMLReader.PREFIX).append(entry.getKey()).append('>')
                            .append(entry.getValue())
                            .append("</").append(GbavXMLReader.PREFIX).append(entry.getKey()).append(">\n");
                }
            }
            sb.append("</bsnhashes>\n");

            this.hasAddedBSNHashes = true;
            LOG.debug("toevoegen bsn hashes aan br_xml: " + sb);
            sb.insert(0, "<root>\n");

            int endOfProlog = this.getBrXml().indexOf("?>");
            if (endOfProlog > 0) {
                endOfProlog = endOfProlog + 2;
            } else {
                endOfProlog = 0;
            }
            StringBuilder br = new StringBuilder(this.getBrXml());
            br.insert(endOfProlog, sb).append("</root>");

            this.setBrXml(br.toString());
        }
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
