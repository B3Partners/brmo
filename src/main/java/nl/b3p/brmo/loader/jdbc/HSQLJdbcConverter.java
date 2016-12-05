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
package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.b3p.topnl.converters.DbUtilsGeometryColumnConverter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class HSQLJdbcConverter  extends GeometryJdbcConverter {

    protected final static Log log = LogFactory.getLog(HSQLJdbcConverter.class);
    
    private WKTReader wkt= new WKTReader();
    @Override
    public Object convertToNativeGeometryObject(String param) throws SQLException, ParseException {
        return param;
    }

    @Override
    public String createPSGeometryPlaceholder() throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getGeomTypeName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Geometry convertToJTSGeometryObject(Object nativeObj) {

        try {
            Clob c = (Clob)nativeObj;
            InputStream in = c.getAsciiStream();
            StringWriter w = new StringWriter();
            IOUtils.copy(in, w);
            Geometry g;
            
            g = wkt.read(w.toString());
            
            return g;
        } catch (IOException | ParseException | SQLException ex) {
            log.error("Error parsing clob to geometry", ex);
            return null;
        }
    }

}
