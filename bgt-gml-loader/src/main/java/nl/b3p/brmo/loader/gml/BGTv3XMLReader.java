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
package nl.b3p.brmo.loader.gml;

import nl.b3p.brmo.loader.gml.bgt.BGTv3Object;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import nl.b3p.brmo.loader.gml.bgt.BGTv3Object.Type;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.xml.Parser;

/**
 *
 * @author matthijsln
 */
public class BGTv3XMLReader {
    private final TransformerFactory tf = TransformerFactory.newInstance();
    private final XMLInputFactory xif = XMLInputFactory.newInstance();
    private final GeometryFactory gf;
    private final Parser gmlParser;
    private final XMLStreamReader streamReader;

    private boolean mutaties = false;

    public BGTv3XMLReader(InputStream in)
            throws XMLStreamException,
            TransformerConfigurationException {

        GMLConfiguration gml = new org.geotools.gml3.GMLConfiguration();
        gf = new GeometryFactory(new PrecisionModel(), 28992);
        gml.getContext().registerComponentInstance(gf);
        gmlParser = new Parser(gml);

        streamReader = XMLInputFactory.newInstance().createXMLStreamReader(in);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        lookAtRoot();
    }

    private void lookAtRoot() throws XMLStreamException {
        streamReader.nextTag();
        String name = streamReader.getLocalName();
        if(name.equals("CityModel")) {
            mutaties = false;
        } else if(name.equals("bgtMutaties") || name.equals("bgtv3Mutaties")) {
            streamReader.nextTag(); // mutatieBericht
            streamReader.nextTag();
            String dataset = streamReader.getElementText();
            if(!dataset.equals("bgt") && !dataset.equals("bgtv3")) {
                throw new IllegalArgumentException("Unexpected dataset: " + dataset + ", expected bgt or bgtv3");
            }
            streamReader.nextTag(); // inhoud

            // TODO: evt parsen inhoud

            // Skip naar volgende mutatieGroep of einde

            while(!(streamReader.getEventType() == END_ELEMENT && "inhoud".equals(streamReader.getLocalName())) || !streamReader.hasNext()) {
                streamReader.next();
            }
            if(streamReader.hasNext()) {
                streamReader.nextTag();
                if(!streamReader.getLocalName().equals("mutatieGroep")) {
                    throw new IllegalArgumentException("Expected mutatieGroep");
                }
            }
            mutaties = true;
        } else {
            throw new IllegalArgumentException("Unexpected element: " + name + ", expected CityModel or bgtMutaties");
        }
    }

    private boolean isMutaties() {
        return mutaties;
    }

    BGTv3Object theNext = null;


    public boolean hasNext() throws XMLStreamException {
        theNext = next();
        return theNext != null;
    }

    public BGTv3Object next() throws XMLStreamException {

        if(theNext != null) {
            BGTv3Object r = theNext;
            theNext = null;
            return r;
        }

        if(!streamReader.isStartElement()) {
            return null;
        }

        BGTv3Object o = new BGTv3Object();
        if(mutaties) {
            streamReader.nextTag();

            o.setObjectType(streamReader.getAttributeValue(null, "objectType"));
            o.setObjectId(streamReader.getAttributeValue(null, "objectId"));

            if(streamReader.getLocalName().equals("toevoeging")) {
                o.setType(Type.TOEVOEGING);

                // Alleen wordt, sla elementen over tot object
                while(!streamReader.getLocalName().equals("cityObjectMember")) {
                    streamReader.nextTag();
                }

                o.readFromXMLStream(streamReader);
            } else if(streamReader.getLocalName().equals("wijziging")) {
                o.setType(Type.WIJZIGING);

                // Sla was over
                while(!(streamReader.getEventType() == END_ELEMENT && "was".equals(streamReader.getLocalName()))) {
                    streamReader.next();
                }

                // In wordt, sla elementen over tot object
                while(!streamReader.getLocalName().equals("cityObjectMember")) {
                    streamReader.nextTag();
                }
                o.readFromXMLStream(streamReader);


            } else if(streamReader.getLocalName().equals("verwijdering")) {
                o.setType(Type.VERWIJDERING);

                // Alleen was, objectType en Id voldoende, geen elementen lezen

            }

            while(!(streamReader.getEventType() == END_ELEMENT && "mutatieGroep".equals(streamReader.getLocalName()))) {
                streamReader.next();
            }
            streamReader.nextTag();

        } else {
            o.setType(Type.TOEVOEGING);

            o.readFromXMLStream(streamReader);
        }

        return o;
    }

    public static void main(String[] args) throws Exception {
        BGTv3XMLReader r = new BGTv3XMLReader(new FileInputStream("/home/matthijsln/bgtv3_all.xml"));
        System.out.println("Mutaties: " + r.isMutaties());
        int i = 0;
        while(r.hasNext()) {
            BGTv3Object o = r.next();
            if(o.getObjectId().equals("G0344.40e978d267eb477789fa3a9e39574e88")) {
                System.out.println(o);
            }
            i++;
        }
        System.out.println("Count: " + i);
    }
}
