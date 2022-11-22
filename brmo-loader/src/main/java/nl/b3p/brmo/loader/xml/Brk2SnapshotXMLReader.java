/*
 * Copyright (C) 2022 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */
package nl.b3p.brmo.loader.xml;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Brk2Bericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * reader voor BRK2 stand berichten.
 *
 * @author mprins
 */
public class Brk2SnapshotXMLReader extends BrmoXMLReader {
    private static final Log log = LogFactory.getLog(Brk2SnapshotXMLReader.class);
    private final XMLInputFactory factory = XMLInputFactory.newInstance();
    private final XMLStreamReader streamReader;
    private final Transformer transformer;
    private static final String KAD_OBJ_SNAP = "KadastraalObjectSnapshot";
    private static final String BRK_DATUM = "brkDatum";
    private static final String MUTATIE = "Mutatie";

    private static final String VOLGNUMMER = "volgnummerKadastraalObjectDatum";
    private static final String NS_MUTATIE = "http://www.kadaster.nl/schemas/brk-levering/product-mutatie/v20200721";
    private static final String NS_XLINK = "http://www.w3.org/1999/xlink";
    private static final String KENMERKNAAM = "kenmerknaam";
    private static final String KENMERKWAARDE = "kenmerkwaarde";
    private static final String PRODUCTSPECIFICATIE = "productSpecificatie";

    private static class MutatieGegevens {
        boolean inMutatieObjectRef = false;
        String mutatieObjectRef = null;
        boolean isMutatieZonderWordt = true;
        Integer volgnummerKadastraalObjectDatum = null;
    }
    private MutatieGegevens mutatieGegevens = null;

    public Brk2SnapshotXMLReader(InputStream in)
            throws XMLStreamException,
            TransformerConfigurationException {

        streamReader = factory.createXMLStreamReader(in);
        TransformerFactory tf = TransformerFactory.newInstance();
        transformer = tf.newTransformer();

        init();
    }

    @Override
    public void init() throws XMLStreamException {
        soort = BrmoFramework.BR_BRK2;
        positionToNext();
    }

    /**
     * Positioneer de XML stream aan het start element voor een
     * KadastraalObjectSnapshot (als child van een GemeenteGebaseerdeStand:stand
     * of als child van een Mutatie:wordt) of aan het end element voor een Mutatie:Mutatie
     * zonder Mutatie:wordt (vervallen perceel).
     *
     * @throws XMLStreamException if any
     */
    private void positionToNext() throws XMLStreamException {
        boolean inMutatie = false;
        mutatieGegevens = null;
        boolean inWas = false;
        boolean inProductSpecificatie = false;
        String inProductSpecificatieNaam = null;
        while (streamReader.hasNext()) {
            if (streamReader.isStartElement()) {
                if (!inWas && streamReader.getLocalName().equals(KAD_OBJ_SNAP)) {
                    // KadastraalObjectSnapshot gevonden niet in "was" van mutatie,
                    // hou streamReader op dit start element voor next()
                    break;
                }
                if (streamReader.getLocalName().equals(BRK_DATUM)) {
                    setDatumAsString(streamReader.getElementText());
                } else if (streamReader.getLocalName().equals(PRODUCTSPECIFICATIE)) {
                    inProductSpecificatie = true;
                } else if (inProductSpecificatie && streamReader.getLocalName().equals(KENMERKNAAM)) {
                    inProductSpecificatieNaam = streamReader.getElementText();
                } else if (inProductSpecificatie && streamReader.getLocalName().equals(KENMERKWAARDE)) {
                    if ("Peildatum".equals(inProductSpecificatieNaam)) {
                        setDatumAsString(streamReader.getElementText());
                    } else if ("Gebiednummer".equals(inProductSpecificatieNaam)) {
                        setGebied(streamReader.getElementText());
                    }
                } else if(streamReader.getLocalName().equals(MUTATIE)) {
                    inMutatie = true;
                    mutatieGegevens = new MutatieGegevens();
                } else if(inMutatie && streamReader.getLocalName().equals(VOLGNUMMER)) {
                    mutatieGegevens.volgnummerKadastraalObjectDatum = Integer.parseInt(streamReader.getElementText());
                } else if(inMutatie && streamReader.getLocalName().equals("kadastraalObject") && streamReader.getNamespaceURI().equals(NS_MUTATIE)) {
                    // Mutatie:AanduidingKadastraalObject -> Mutatie:kadastraalObject -> (AppartementsrechtRef | PerceelRef)
                    mutatieGegevens.inMutatieObjectRef = true;
                } else if(inMutatie && mutatieGegevens.inMutatieObjectRef) {
                    // Haal id op van Appartementsrecht of PerceelRef
                    mutatieGegevens.mutatieObjectRef = streamReader.getAttributeValue(NS_XLINK, "href");
                    if(mutatieGegevens.mutatieObjectRef != null) {
                        int i = mutatieGegevens.mutatieObjectRef.lastIndexOf('.');
                        if(i != -1) {
                            mutatieGegevens.mutatieObjectRef = mutatieGegevens.mutatieObjectRef.substring(0,i) + ':' + mutatieGegevens.mutatieObjectRef.substring(i+1);
                        }
                    }
                    mutatieGegevens.inMutatieObjectRef = false;
                } else if(inMutatie && streamReader.getLocalName().equals("was")) {
                    // Bij mutatie "was" skippen
                    inWas = true;
                } else if(inMutatie && streamReader.getLocalName().equals("wordt")) {
                    mutatieGegevens.isMutatieZonderWordt = false;
                }
            } else if (streamReader.isEndElement()) {
                if(streamReader.getLocalName().equals(PRODUCTSPECIFICATIE)) {
                    inProductSpecificatie = false;
                    inProductSpecificatieNaam = null;
                }
                if(streamReader.getLocalName().equals("was")) {
                    inWas = false;
                }
                if(inMutatie && streamReader.getLocalName().equals(MUTATIE)) {
                    break;
                }
            }

            streamReader.next();
        }
    }

    @Override
    public boolean hasNext() {
        try {
            return mutatieGegevens != null && mutatieGegevens.isMutatieZonderWordt || streamReader.hasNext();
        } catch (XMLStreamException ex) {
            log.error("Fout tijdens streaming XML", ex);
        }
        return false;
    }

    @Override
    public Brk2Bericht next() throws TransformerException, XMLStreamException {
        Brk2Bericht b;

        if(mutatieGegevens != null && mutatieGegevens.isMutatieZonderWordt) {
            // Vervallen perceel
            b = new Brk2Bericht("<empty/>");

            b.setVervallenInfo(mutatieGegevens.mutatieObjectRef, getBestandsDatum());
            b.setVolgordeNummer(mutatieGegevens.volgnummerKadastraalObjectDatum);
        } else {
            StringWriter sw = new StringWriter();

            // Vanwege splitsing is repairen van namespaces nodig, anders ontbreekt
            // de namespace prefix declaratie xmlns:xlink="http://www.w3.org/1999/xlink"
            // in de output en mislukt de XSL transformatie
            XMLOutputFactory of = XMLOutputFactory.newInstance();
            of.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
            XMLStreamWriter writer = of.createXMLStreamWriter(sw);

            transformer.transform(new StAXSource(streamReader), new StAXResult(writer));
            b = new Brk2Bericht(sw.toString());
            //volgende gegevens komen uit de header, in bericht worden gegevens verder aangevuld.
            b.setVolgordeNummer(mutatieGegevens != null ? mutatieGegevens.volgnummerKadastraalObjectDatum : -1);
            b.setDatum(getBestandsDatum());
            b.setObjectRef(mutatieGegevens != null ? mutatieGegevens.mutatieObjectRef : null);
        }
        positionToNext();
        return b;
    }
}
