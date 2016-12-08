/*
 * Copyright (C) 2016 B3Partners B.V.
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
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;
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
 */
public class Processor {
    protected final static Log log = LogFactory.getLog(Processor.class);


    private Database database;
    private ConverterFactory converterFactory;

    
    public Processor(DataSource ds) throws JAXBException, SQLException{
        database = new Database(ds);
        converterFactory = new ConverterFactory();
    }
    
    public void importIntoDb(URL in, TopNLType type) throws JDOMException {
        XMLInputFactory xif = XMLInputFactory.newFactory();
        try {
            log.info("Importing file " + in.toExternalForm() + ", type: " + type.getType());
            Unmarshaller jaxbUnmarshaller = converterFactory.getContext(type).createUnmarshaller();

            XMLStreamReader xsr = xif.createXMLStreamReader(in.openStream());

            while (xsr.hasNext()) {
                int eventType = xsr.next();

                if (eventType == XMLStreamReader.START_ELEMENT) {
                    String localname = xsr.getLocalName();
                    if (xsr.getLocalName().equals("FeatureMember")) {
                        try {
                            JAXBElement jb = (JAXBElement) jaxbUnmarshaller.unmarshal(xsr);
                            Object obj = jb.getValue();
                            ArrayList list = new ArrayList();
                            list.add(obj);
                            List<TopNLEntity> entities = convert(list, type);
                            save(entities, type);
                        } catch (JAXBException | IOException | SAXException | ParserConfigurationException | TransformerException | ParseException ex) {
                            log.error("Error parsing", ex);
                        }
                    }
                }
            }

            xsr.close();
        } catch (XMLStreamException | IOException | JAXBException ex) {
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
    
    public List<TopNLEntity> convert(List listOfJaxbObjects, TopNLType type)  throws IOException, SAXException, ParserConfigurationException, TransformerException{
        Converter converter = converterFactory.getConverter(type);
        List<TopNLEntity> entity = converter.convert(listOfJaxbObjects);
        return entity;
    }
    
    public void save(TopNLEntity entity, TopNLType type) throws ParseException{
        database.save(entity);
    }
    
    public void save(List<TopNLEntity> entities, TopNLType type) throws ParseException {
        for (TopNLEntity entity : entities) {
            save(entity, type);
        }
    }

}
