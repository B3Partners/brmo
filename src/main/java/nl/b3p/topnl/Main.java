
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
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
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
    
    public static void main (String[] args) throws IOException, JAXBException, ParseException, SQLException{
        try {
             /* BasicDataSource ds = new BasicDataSource();
            ds.setUrl("jdbc:postgresql://localhost:5432/rsgb_topnl");
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setUsername("rsgb");
            ds.setPassword("rsgb");
          */
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl("jdbc:oracle:thin:@b3p-demoserver:1521/ORCL");
            ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
            ds.setUsername("top50nl");
            ds.setPassword("top50nl");
            Processor p = new Processor(ds);
            process("top250NL.gml", TopNLType.TOP250NL, p);
            //process("Hoogte_top250nl.xml", TopNLType.TOP250NL, p);
            //process("Hoogte_top100nl.xml", TopNLType.TOP100NL, p);
          
        } catch (SAXException | ParserConfigurationException | TransformerException ex) {
            log.error("Cannot parse/convert/save entity: ", ex);
        }
    }

    private static void process(String file, TopNLType type, Processor p) throws ParseException, IOException, SAXException, ParserConfigurationException, JAXBException, TransformerException {
        InputStream in = Main.class.getResourceAsStream(file);
        p.importIntoDb(in, type);
       /* List obj = p.parse(in, type);
        List<TopNLEntity> entities = p.convert(obj, type);
        p.save(entities, type);*/
        int a = 0;
    }
}
