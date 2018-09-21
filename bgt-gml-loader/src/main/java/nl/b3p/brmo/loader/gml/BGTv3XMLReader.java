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

import java.io.File;
import nl.b3p.brmo.loader.gml.bgt.BGTv3Object;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.stream.XMLInputFactory;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerConfigurationException;
import nl.b3p.brmo.bgt.util.XMLStreamGMLParser;
import nl.b3p.brmo.loader.gml.bgt.BGTv3Object.Type;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author matthijsln
 */
public class BGTv3XMLReader implements Iterable<BGTv3Object> {
    private final XMLStreamReader streamReader;
    private final XMLStreamGMLParser xmlGmlParser;

    private boolean mutaties = false;

    public BGTv3XMLReader(InputStream in)
            throws XMLStreamException,
            TransformerConfigurationException {

        xmlGmlParser = new XMLStreamGMLParser();
        streamReader = XMLInputFactory.newInstance().createXMLStreamReader(in);

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

    boolean checkedNext = false, finished = false;
    BGTv3Object theNext = null;

    public Iterator<BGTv3Object> iterator() {
        return new Iterator<BGTv3Object>() {
            @Override
            public boolean hasNext() {
                if(finished) {
                    return false;
                }

                if(!checkedNext) {
                    theNext = next();
                    checkedNext = true;
                }
                return theNext != null;
            }

            @Override
            public BGTv3Object next() {
                try {
                    return getNext();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
    }

    private BGTv3Object getNext() throws Exception {
        if(finished) {
            return null;
        }

        if(theNext != null) {
            BGTv3Object r = theNext;
            theNext = null;
            checkedNext = false;
            return r;
        }

        BGTv3Object o = new BGTv3Object();
        if(mutaties) {
            while(streamReader.hasNext() && streamReader.getEventType() != END_DOCUMENT && (!streamReader.isStartElement() || ArrayUtils.indexOf(new String[] {"toevoeging", "wijziging", "verwijdering"}, streamReader.getLocalName()) == -1)) {
                streamReader.next();
            }
            if(!streamReader.hasNext() || streamReader.getEventType() == END_DOCUMENT) {
                finished = true;
                return null;
            }

            o.setObjectType(streamReader.getAttributeValue(null, "objectType"));
            o.setObjectId(streamReader.getAttributeValue(null, "objectId"));

            if(streamReader.getLocalName().equals("toevoeging")) {
                o.setType(Type.TOEVOEGING);

                // Alleen wordt, sla elementen over tot object
                while(!streamReader.getLocalName().equals("cityObjectMember")) {
                    streamReader.nextTag();
                }

                o.readFromXMLStream(streamReader, xmlGmlParser);
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
                o.readFromXMLStream(streamReader, xmlGmlParser);


            } else if(streamReader.getLocalName().equals("verwijdering")) {
                o.setType(Type.VERWIJDERING);

                // Alleen was, objectType en Id voldoende, geen elementen lezen

                // Sla was over
                while(!(streamReader.getEventType() == END_ELEMENT && "was".equals(streamReader.getLocalName()))) {
                    streamReader.next();
                }
            }

        } else {
            throw new UnsupportedOperationException("Alleen parsen mutaties ondersteund");
/*
            o.setType(Type.TOEVOEGING);

            while(streamReader.hasNext() && !streamReader.isStartElement() || "cityObjectMember".equals(streamReader.getLocalName())) {
                streamReader.next();
            }

            if(!streamReader.hasNext()) {
                return null;
            }

            o.readFromXMLStream(streamReader);
*/
        }

        return o;
    }

    public static void f(String f, InputStream in) {
        try {
            //System.out.println(f);
            BGTv3XMLReader r = new BGTv3XMLReader(in/*new FileInputStream("/home/matthijsln/bgtv3_all_delta.xml)"*/);
            //System.out.println("Mutaties: " + r.isMutaties());
            int i = 0;
            int toevoeging = 0, wijziging = 0, verwijdering = 0, metEindTijd = 0, metEindRegistratie = 0;
            for(BGTv3Object o: r) {
                //if(o.getObjectId().equals("G0344.40e978d267eb477789fa3a9e39574e88")) {
                switch(o.getType()) {
                    case TOEVOEGING: toevoeging++; break;
                    case VERWIJDERING: verwijdering++; break;
                    case WIJZIGING: wijziging++; break;
                }
                System.out.println(o);
                //}
                if(o.getAttributes().containsKey("terminationDate")) {
                    metEindTijd++;
                }
                if(o.getAttributes().containsKey("eindRegistratie")) {
                    metEindRegistratie++;
                }
                i++;
            }
            System.out.printf("%62s, aantal: %6d, toev %6d, wijz %6d, verw %6d, met terminationDate: %6d, met eindRegistratie: %6d\n",f, i,toevoeging,wijziging,verwijdering,metEindTijd, metEindRegistratie);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void zip(File f) throws Exception {
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(f))) {
            ZipEntry entry = zip.getNextEntry();
            if(entry == null) {
                return;
            }
            do {
                String ext = FilenameUtils.getExtension(entry.getName());
                if(ext.equals("gml") || ext.equals("xml")) {
                    f(f.getName(), new CloseShieldInputStream(zip));
                }
                entry = zip.getNextEntry();
            } while(entry != null);
        }

    }

    public static void main(String[] args) throws Exception {

        File[] files = new File("/home/matthijsln/.netbeans/8.2/apache-tomcat-8.0.27.0_base/temp").listFiles();

        for(File file : files) {
            if(file.isFile() && file.getName().endsWith(".zip")) {
                if(file.getName().equals("bgtv3_citygml_delta_5ded1a9e-5aed-42df-8d7a-4e204fcd8e91.zip")) {
                    zip(file);
                }
            }
        }
    }
}
