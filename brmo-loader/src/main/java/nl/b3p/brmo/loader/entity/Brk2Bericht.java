/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */
package nl.b3p.brmo.loader.entity;

import nl.b3p.brmo.loader.BrmoFramework;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/** @author mprins */
public class Brk2Bericht extends Bericht {

    private static final Log log = LogFactory.getLog(Brk2Bericht.class);
    // private final String soort = BrmoFramework.BR_BRK2;
    private boolean xpathEvaluated = false;

    public Brk2Bericht(String brXml) {
        super(brXml);

        setSoort(BrmoFramework.BR_BRK2);
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

        if (objectRef != null && datum != null) {
            // al uit de header gehaald
            return;
        }

        // dit moet dus een standbericht zijn
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(new InputSource(new StringReader(getBrXml())));

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();

            XPathExpression expr =
                    xpath.compile(
                            "/KadastraalObjectSnapshot/*[local-name()= 'Perceel' or local-name()='Appartementsrecht']/identificatie/@domein");
            objectRef = expr.evaluate(doc);

            expr =
                    xpath.compile(
                            "/KadastraalObjectSnapshot/*[local-name()= 'Perceel' or local-name()='Appartementsrecht']/identificatie/text()");
            objectRef += ":" + expr.evaluate(doc);

            expr =
                    xpath.compile(
                            "/KadastraalObjectSnapshot/*[local-name()= 'toestandsdatum' or local-name()='toestandsdatum']/text()");
            setDatumAsString(expr.evaluate(doc));
        } catch (Exception e) {
            log.error("Probleem met uitlezen van BRK 2 referentie", e);
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
            log.error("Probleem tijdens parsen van de datum: " + datum, pe);
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

    public String getRestoredFileName(Date bestanddatum, Integer volgordenummer) {
        final String brkdatum = new SimpleDateFormat("yyyyMMdd").format(bestanddatum);
        final String prefix = "MUTKX02";

        if (volgordenummer <= 0) {
            return "stand levering " + brkdatum;
        }

        try {

            String kadGemCode;
            String perceelnummer;
            String sectie;
            String appartementsrechtVolgnummer;

            String basePath =
                    "/KadastraalObjectSnapshot/*[local-name()= 'Perceel' or local-name()='Appartementsrecht']/kadastraleAanduiding/";
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(this.getBrXml())));

            // in geval van een verwijderbericht is de "wordt" leeg, db_xml <empty/>
            if (this.getBrXml().contains("<empty/>")
                    && StringUtils.isNotBlank(this.getBrOrgineelXml())) {
                // dan proberen om de db_origineel_xml te gebruiken.
                basePath =
                        "/Mutatie/kadastraalObject/AanduidingKadastraalObject/kadastraleAanduiding/";
                doc = builder.parse(new InputSource(new StringReader(this.getBrOrgineelXml())));
            }

            XPathExpression expr =
                    xpath.compile(basePath + "akrKadastraleGemeenteCode/waarde/text()");
            kadGemCode = expr.evaluate(doc);

            expr = xpath.compile(basePath + "sectie/text()");
            sectie = expr.evaluate(doc);

            expr = xpath.compile(basePath + "perceelnummer/text()");
            perceelnummer = expr.evaluate(doc);

            expr = xpath.compile(basePath + "appartementsrechtVolgnummer/text()");
            appartementsrechtVolgnummer = expr.evaluate(doc);
            if (StringUtils.isNotBlank(appartementsrechtVolgnummer)) {
                appartementsrechtVolgnummer = "A" + appartementsrechtVolgnummer;
            }

            String aanduiding = kadGemCode + sectie + perceelnummer + appartementsrechtVolgnummer;
            log.debug("gevonden aanduiding voor herstelde bestandsnaam: " + aanduiding);
            String filename = "bestandsnaam kon niet worden hersteld";
            if (StringUtils.isNotBlank(aanduiding)) {
                filename =
                        prefix
                                + "-"
                                + aanduiding
                                + "-"
                                + brkdatum
                                + "-"
                                + volgordenummer.toString()
                                + ".zip";
            }
            return filename;
        } catch (ParserConfigurationException
                | XPathExpressionException
                | SAXException
                | IOException ex) {
            log.error("Kan geen bestandsnaam maken van xml: ", ex);
            return "";
        }
    }
}
