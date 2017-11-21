/*
 * Copyright (C) 2016 - 2017 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.topnl.converters;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.simplify.DouglasPeuckerSimplifier;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.gml3.ArcParameters;
import org.geotools.gml3.CircleRadiusTolerance;
import org.geotools.xml.Parser;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.XMLOutputter;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class GeometryConverter {

    protected final static Log log = LogFactory.getLog(GeometryConverter.class);

    private final static double DISTANCE_TOLERANCE = 0.001;
    protected final static double LINEARIZATION_TOLERANCE_MULTIPLIER = 0.01;//0.001;

    private Parser parser;
    private GeometryFactory geometryFactory;

    protected final static int SRID = 28992;

    public GeometryConverter() {
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), SRID);

      /*  GMLConfiguration gml3Config = new GMLConfiguration(true);
        gml3Config.setGeometryFactory(gf);
        gml3Config.setExtendedArcSurfaceSupport(true);*/


        ArcParameters arcParameters = new ArcParameters(new CircleRadiusTolerance(LINEARIZATION_TOLERANCE_MULTIPLIER));//new AbsoluteTolerance(LINEARIZATION_TOLERANCE));

        org.geotools.gml3.GMLConfiguration gml3Config = new org.geotools.gml3.GMLConfiguration();
        gml3Config.setExtendedArcSurfaceSupport(true);
        gml3Config.getContext().registerComponentInstance(gf);
        gml3Config.getContext().registerComponentInstance(arcParameters);
        parser = new Parser(gml3Config);
        this.geometryFactory = gf;
    }

    public Geometry convertGeometry(Element el)
            throws IOException, SAXException, ParserConfigurationException, TransformerException {
        if (!(el instanceof org.w3c.dom.Element)) {
            throw new IllegalArgumentException("gml org.w3c.node is not an org.w3c.Element");
        }
        // TODO: maybe convert node directly to a source / inputsource / reader / stream for parser.
        // instead of JDOM detour.
        org.jdom2.Element elem = new DOMBuilder().build((org.w3c.dom.Element) el);
        String gmlString = new XMLOutputter().outputString(elem);
        gmlString = gmlString.replaceAll("gml:", "");
        //tySstem.out.println("gmlString:" + gmlString);
     //   parser.getNamespaces().declarePrefix("gml", "http://www.opengis.net/gml/3.2");
        Object parsedObject = parser.parse(new StringReader(gmlString));
        if (parsedObject instanceof Geometry) {
            Geometry geom = (Geometry) parsedObject;
            if (!geom.isValid()) {
                geom = geom.buffer(0.0);
                log.debug("Geometry is invalid. Made valid by buffering with 0");
            }
            // arcs can have nodes that are on the same point (28992; 3 digit precision): simplify
            Geometry simplGeom = DouglasPeuckerSimplifier.simplify(geom, DISTANCE_TOLERANCE);
            return simplGeom;
        } else {
            throw new InvalidClassException(parsedObject.getClass().getCanonicalName(), "Parsed object not of class Geometry.");
        }

    }
}
