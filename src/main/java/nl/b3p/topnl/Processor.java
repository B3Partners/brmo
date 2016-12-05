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
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.b3p.topnl.converters.Converter;
import nl.b3p.topnl.converters.ConverterFactory;
import nl.b3p.topnl.entities.TopNLEntity;
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen
 */
public class Processor {

    private Database database;
    private ConverterFactory converterFactory;

    
    public Processor(DataSource ds) throws JAXBException, SQLException{
        database = new Database(ds);
        converterFactory = new ConverterFactory();
    }
  
    public Object parse(InputStream in, TopNLType type) throws JAXBException {
        Unmarshaller jaxbUnmarshaller = converterFactory.getContext(type).createUnmarshaller();
        JAXBElement o = (JAXBElement) jaxbUnmarshaller.unmarshal(in);

        Object value = o.getValue();

        return value;
    }
    
    public List<TopNLEntity> convert(Object jaxbObject, TopNLType type)  throws IOException, SAXException, ParserConfigurationException, TransformerException{
        Converter converter = converterFactory.getConverter(type);
        List<TopNLEntity> entity = converter.convert(jaxbObject);
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
