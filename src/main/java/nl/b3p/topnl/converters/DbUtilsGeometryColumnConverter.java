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
package nl.b3p.topnl.converters;

import com.vividsolutions.jts.geom.Geometry;
import java.sql.ResultSet;
import java.sql.SQLException;
import nl.b3p.loader.jdbc.GeometryJdbcConverter;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class DbUtilsGeometryColumnConverter extends BeanProcessor{
    
    protected final static Log log = LogFactory.getLog(DbUtilsGeometryColumnConverter.class);
    private GeometryJdbcConverter gjc;
    public DbUtilsGeometryColumnConverter(GeometryJdbcConverter gjc){
        this.gjc = gjc;
    }
    
    @Override
    protected Object processColumn(ResultSet rs, int index, Class<?> propType) throws SQLException {
        if (Geometry.class.isAssignableFrom(propType)) {
            Object o = rs.getObject(index);
            return gjc.convertToJTSGeometryObject(o);
        } else {
            return super.processColumn(rs, index, propType);
        }
    }
}
