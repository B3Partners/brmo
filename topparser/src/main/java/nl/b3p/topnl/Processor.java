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
package nl.b3p.topnl;

import com.vividsolutions.jts.io.ParseException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;
import javax.xml.stream.Location;
import nl.b3p.topnl.converters.Converter;
import nl.b3p.topnl.converters.ConverterFactory;
import nl.b3p.topnl.entities.TopNLEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen
 * @author mprins
 */
public class Processor {
    protected final static Log log = LogFactory.getLog(Processor.class);


    private Database database;
    private ConverterFactory converterFactory;

    private STATUS status = STATUS.OK;
    
    public Processor(DataSource ds) throws JAXBException, SQLException{
        database = new Database(ds);
        converterFactory = new ConverterFactory();
    }
    
    public void importIntoDb(URL in, TopNLType type) throws JDOMException {
        XMLInputFactory xif = XMLInputFactory.newFactory();
        this.resetStatus();
        try {
            log.info("Importing file " + in.toExternalForm() + ", type: " + type.getType());
            Unmarshaller jaxbUnmarshaller = converterFactory.getContext(type).createUnmarshaller();

            XMLStreamReader xsr = xif.createXMLStreamReader(in.openStream());
            LocationListener ll = new LocationListener(xsr);
            jaxbUnmarshaller.setListener(ll);

            while (xsr.hasNext()) {
                int eventType = xsr.next();

                if (eventType == XMLStreamReader.START_ELEMENT) {
                    String localname = xsr.getLocalName();
                    if (xsr.getLocalName().equals("FeatureMember")) {
                        JAXBElement jb = null;
                        try {
                            jb = (JAXBElement) jaxbUnmarshaller.unmarshal(xsr);
                            Object obj = jb.getValue();
                            ArrayList list = new ArrayList();
                            list.add(obj);
                            List<TopNLEntity> entities = convert(list, type);
                            save(entities, type);
                        } catch (SQLException ex) {
                            log.error("Error inserting", ex);
                            this.status = STATUS.NOK;
                        } catch (JAXBException | IOException | SAXException | ParserConfigurationException | TransformerException | ParseException | IllegalArgumentException ex) {
                            log.error("Error parsing", ex);
                            this.status = STATUS.NOK;
                        } catch (ClassCastException cce) {
                            log.error(String.format(Locale.ROOT, "Verwerkingsfout van element %s locatie %s", localname, (jb == null ? "onbekend" : ll.getLocation(jb))), cce);
                        }
                    }
                }
            }

            xsr.close();
        } catch (XMLStreamException | IOException | JAXBException ex) {
            this.status = STATUS.NOK;
            log.error("cannot correctly stream xml file:", ex);
        } 
    }

    public List parse (URL in) throws  JAXBException,  IOException{
        List list = new ArrayList();
        try {
            TopNLType type = TopNLTypeFactory.getTopNLType(in);
            Unmarshaller jaxbUnmarshaller = converterFactory.getContext(type).createUnmarshaller();
            
            XMLInputFactory xif = XMLInputFactory.newFactory();
            
            XMLStreamReader xsr = xif.createXMLStreamReader(in.openStream());
            
            while (xsr.hasNext()) {
                int eventType = xsr.next();
                
                if (eventType == XMLStreamReader.START_ELEMENT) {
                    String localname = xsr.getLocalName();
                    if (xsr.getLocalName().equals("FeatureMember")) {
                        JAXBElement jb = (JAXBElement)jaxbUnmarshaller.unmarshal(xsr);
                        list.add(jb.getValue());
                    }
                }
            }
            
            xsr.close();
        } catch (XMLStreamException ex) {
            log.error("cannot correctly stream xml file:", ex);
        } catch (JDOMException ex) {
            log.error("Cannot retrieve topnltype: ",ex);
        }
        return list;
    }

    /**
     *
     * @throws ClassCastException Als er een onverwacht type geometrie in de
     * data zit, bijvoorbeeld een punt ipv een lijn
     */
    public List<TopNLEntity> convert(List listOfJaxbObjects, TopNLType type) throws IOException, SAXException,
            ParserConfigurationException, TransformerException, ClassCastException {

        Converter converter = converterFactory.getConverter(type);
        List<TopNLEntity> entity = converter.convert(listOfJaxbObjects);
        return entity;
    }
    
    public void save(TopNLEntity entity, TopNLType type) throws ParseException, SQLException {
        database.save(entity);
    }
    
    public void save(List<TopNLEntity> entities, TopNLType type) throws ParseException, SQLException {
        for (TopNLEntity entity : entities) {
            save(entity, type);
        }
    }

    /**
     * geeft de verwerkingsstatus. Voorafgaand aan de verwerking moet de status
     * reset worden als er een set losse GML bestanden wordt verwerkt (bij zip
     * files gaat dat vanzelf).
     *
     * @return status van de verwerking
     */
    public STATUS getStatus() {
        return this.status;
    }

    /**
     * verwerkingsstatus
     */
    public enum STATUS {
        OK, NOK
    }

    public void resetStatus() {
        this.status = STATUS.OK;
    }

    private class LocationListener extends Listener {
        private XMLStreamReader xsr;
        private Map<Object, Location> locations;

        public LocationListener(XMLStreamReader xsr) {
            this.xsr = xsr;
            this.locations = new HashMap<Object, Location>();
        }

        @Override
        public void beforeUnmarshal(Object target, Object parent) {
            locations.put(target, xsr.getLocation());
        }

        public Location getLocation(Object o) {
            return locations.get(o);
        }

    }
}
