/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.sql.PostGISCopyInsertBatch;
import nl.b3p.brmo.sql.QueryBatch;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMInputCursor;
import org.geotools.gml.stream.XmlStreamGeometryReader;
import org.locationtech.jts.geom.Geometry;
import org.opengis.referencing.FactoryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static nl.b3p.brmo.bgt.loader.Utils.formatTimeSince;

public class BAG2GMLObjectStream {
    private static final Log log = LogFactory.getLog(BAG2GMLObjectStream.class);

    private static final String NS_BAG_EXTRACT = "http://www.kadaster.nl/schemas/lvbag/extract-deelbestand-lvc/v20200601";
    private static final String NS_STANDLEVERING = "http://www.kadaster.nl/schemas/standlevering-generiek/1.0";

    private static final int SRID = 28992;

    private final XmlStreamGeometryReader geometryReader;

    private final SMInputCursor cursor;

    public BAG2GMLObjectStream(InputStream in) throws XMLStreamException {
        this.cursor = initCursor(buildSMInputFactory().rootElementCursor(in));
        this.geometryReader = buildGeometryReader();
    }

    protected SMInputFactory buildSMInputFactory() {
        // Using alternative StAX parsers explicitly:
        // final XMLInputFactory stax = new WstxInputFactory(); // Woodstox
        // final XMLInputFactory stax = new com.fasterxml.aalto.stax.InputFactoryImpl(); // Aalto
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory(); // JRE Default, depends on JAR's present or javax.xml.stream.XMLInputFactory property, can be SJSXP
        log.trace("StAX XMLInputFactory: " + xmlInputFactory.getClass().getName());

        xmlInputFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE); // Coalesce characters
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE); // No XML entity expansions or external entities

        return new SMInputFactory(xmlInputFactory);
    }

    protected XmlStreamGeometryReader buildGeometryReader() {
        return new XmlStreamGeometryReader(this.cursor.getStreamReader());
    }

    private SMInputCursor initCursor(SMInputCursor cursor) throws XMLStreamException {
        QName root = cursor.advance().getQName();

        if (!root.equals(new QName(NS_BAG_EXTRACT, "bagStand"))) {
            throw new IllegalArgumentException("XML root element moet bagStand zijn");
        }

        cursor = cursor.childElementCursor(new QName(NS_STANDLEVERING, "standBestand")).advance()
                .childElementCursor(new QName(NS_STANDLEVERING, "stand"));
        return cursor;
    }

    public int load() throws Exception {
        Instant start = Instant.now();
        Connection connection = DriverManager.getConnection("jdbc:postgresql:bag?sslmode=disable&reWriteBatchedInserts=true", "bag", "bag");

        QueryBatch queryBatch = new PostGISCopyInsertBatch(connection, "copy pand (identificatie,documentnummer,\"oorspronkelijkBouwjaar\",geconstateerd,documentdatum,status,geometrie) from stdin", 2500, new PostGISDialect(), false, false);

        int count = 0;

        while(cursor.getNext() != null) {
            SMInputCursor bagObjectCursor = cursor.childElementCursor(new QName(NS_BAG_EXTRACT, "bagObject")).advance().childElementCursor().advance();

            String name = bagObjectCursor.getLocalName();
            Map<String,Object> attributes = new HashMap<>();
            SMInputCursor attributeCursor = bagObjectCursor.childElementCursor();
            while(attributeCursor.getNext() != null) {
                String attributeName = attributeCursor.getLocalName();
                Object attributeValue;
                if (attributeName.equals("geometrie")) {
                    attributeCursor.childElementCursor().advance();
                    attributeValue = geometryReader.readGeometry();
                    ((Geometry)attributeValue).setSRID(28992);
                } else {
                    attributeValue = attributeCursor.collectDescendantText();
                }
                attributes.put(attributeName, attributeValue);
            }
            BAG2Object object = new BAG2Object(name, attributes);

            Object[] params = new Object[] {
                    attributes.get("identificatie"),
                    attributes.get("documentnummer"),
                    attributes.get("oorspronkelijkBouwjaar"),
                    attributes.get("geconstateerd"),
                    attributes.get("documentdatum"),
                    attributes.get("status"),
                    attributes.get("geometrie"),
            };
            queryBatch.addBatch(params);
            count++;
        }
        queryBatch.executeBatch();
        queryBatch.close();
        System.out.printf("Loaded %s in %s\n", count, formatTimeSince(start));
        return count;
    }
}
