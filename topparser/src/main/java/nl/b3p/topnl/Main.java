
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
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.JDOMException;
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class Main {
    protected final static Log log = LogFactory.getLog(Main.class);
    
    public static void main (String[] args) throws IOException, JAXBException, ParseException, SQLException, JDOMException{
        try {
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl("jdbc:postgresql://localhost:5432/topnl");
            ds.setDriverClassName("org.postgresql.Driver");
            ds.setUsername("rsgb");
            ds.setPassword("rsgb");
          
           /* BasicDataSource ds = new BasicDataSource();
            ds.setUrl("jdbc:oracle:thin:@b3p-demoserver:1521/ORCL");
            ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
            ds.setUsername("top50nl");
            ds.setPassword("top50nl");*/
            Processor p = new Processor(ds);
         //   loadtopnl("/mnt/data/Documents/TopNL/Top50NL/TOP50NL_GML_Filechunks_november_2016/TOP50NL_GML_Filechunks", p,  TopNLType.TOP50NL);
            //loadtopnl("/mnt/data/Documents/TopNL/Top10NL/TOP10NL_GML_Filechuncks_november_2016/TOP10NL_GML_Filechuncks", p,  TopNLType.TOP10NL);
            loadtopnl("/mnt/data/Documents/TopNL/Tynaarlo/Top10NL", p,  TopNLType.TOP10NL);
            //loadtopnl("/mnt/data/Documents/TopNL/TOP100NL_GML_Filechunks_november_2016/TOP100NL_GML_Filechunks", p,  TopNLType.TOP100NL);
            //process("top250NL.gml", p);
            //process("Hoogte_top250nl.xml", TopNLType.TOP250NL, p);
            //process("Hoogte_top100nl.xml", TopNLType.TOP100NL, p);
          
        } catch (SAXException | ParserConfigurationException | TransformerException ex) {
            log.error("Cannot parse/convert/save entity: ", ex);
        }
    }
    
    private static void loadtopnl(String dir, Processor p, TopNLType type)  throws ParseException, IOException, SAXException, ParserConfigurationException, JAXBException, TransformerException, JDOMException {
        File f = new File (dir);
        FilenameFilter filter = new  FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".gml");
            }
        };
        /*File f = new File("/mnt/data/Documents/TopNL/TOP100NL_GML_Filechunks_november_2016/TOP100NL_GML_Filechunks/Top100NL_000002.gml");
        p.importIntoDb(f.toURL(), TopNLType.TOP100NL);*/
        File[] files = f.listFiles(filter);
        for (File file : files) {
            
            String fileString = file.getCanonicalPath();
            p.importIntoDb(file.toURL(), type);
            
        }
    }

    private static void process(String file,  Processor p, TopNLType type) throws ParseException, IOException, SAXException, ParserConfigurationException, JAXBException, TransformerException, JDOMException {
        URL in = Main.class.getResource(file);
        p.importIntoDb(in, type);
       /* List obj = p.parse(in, type);
        List<TopNLEntity> entities = p.convert(obj, type);
        p.save(entities, type);*/
        int a = 0;
    }
}
