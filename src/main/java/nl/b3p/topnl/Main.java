
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.b3p.topnl.entities.TopNLEntity;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class Main {
    protected final static Log log = LogFactory.getLog(Main.class);
    
    public static void main (String[] args) throws IOException, JAXBException{
        try {
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl("jdbc:postgresql://localhost:5432/rsgb_topnl");
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setUsername("rsgb");
            ds.setPassword("rsgb");
            
            Processor p = new Processor(ds);
            
            InputStream in = Main.class.getResourceAsStream("Hoogte.xml");
            Object obj = p.parse(in);
            List<TopNLEntity> entities = p.convert(obj, TopNLType.TOP250NL);
            p.save(entities, TopNLType.TOP250NL);
            int a = 0;
        } catch (SAXException | ParserConfigurationException | TransformerException ex) {
            log.error("Cannot parse/convert/save entity: ", ex);
        }
    }
}
