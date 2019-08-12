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
package nl.b3p.brmo.bgt.util;

import org.geotools.xsd.Parser;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import org.geotools.gml3.GMLConfiguration;

/**
 *
 * @author matthijsln
 */
public class XMLStreamGMLParser {
    private final TransformerFactory tf = TransformerFactory.newInstance();
    private final GeometryFactory gf;
    private final Parser gmlParser;

    public XMLStreamGMLParser() {
        GMLConfiguration gml = new org.geotools.gml3.GMLConfiguration();
        gf = new GeometryFactory(new PrecisionModel(), 28992);
        gml.getContext().registerComponentInstance(gf);
        gmlParser = new Parser(gml);
    }

    public Geometry parse(XMLStreamReader reader) throws Exception {
        Transformer transformer = tf.newTransformer();
        StringWriter sw = new StringWriter();
        transformer.transform(new StAXSource(reader), new StreamResult(sw));

        Geometry geom = (Geometry) gmlParser.parse(new StringReader(sw.toString()));
        // Elke POLYGON omzetten naar MULTIPOLYGON
        if (geom instanceof Polygon) {
            Polygon[] polygons = new Polygon[] {(Polygon)geom};
             geom = gf.createMultiPolygon(polygons);
        }
        return geom;
    }
}
