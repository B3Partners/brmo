/*
 * Copyright (C) 2017 B3Partners B.V.
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
package nl.b3p.brmo.loader.xml;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Meine Toonen
 */
public class BRPXMLReader extends BrmoXMLReader{

    private InputStream in;
    private boolean read = false;
    
    public BRPXMLReader(InputStream in, Date d){
        this.in = in;
        setBestandsDatum(d);
    }
    
    @Override
    public void init() throws Exception {
        soort = BrmoFramework.BR_BRP;
    }

    @Override
    public boolean hasNext() throws Exception {
        return !read;
    }

    @Override
    public Bericht next() throws Exception {
        read =true;
        StringWriter sw = new StringWriter();
        IOUtils.copy(in, sw, "UTF-8");
        in.close();
        String brXML = sw.toString();
        Bericht b = new Bericht(brXML);
        b.setSoort(BrmoFramework.BR_BRP);
        b.setDatum(new Date());
        return b;
    }
    
}
