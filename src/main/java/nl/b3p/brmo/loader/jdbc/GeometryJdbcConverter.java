
package nl.b3p.brmo.loader.jdbc;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.sql.SQLException;

/**
 *
 * @author Matthijs Laan
 */
public abstract class GeometryJdbcConverter {
    
    protected final WKTReader wkt= new WKTReader();
    //definieer placeholder als ? wanneer object naar native geometry wordt 
    //geconverteerd
    //defineer placeholder via native wkt-import functie als geometry als 
    //wkt-string wordt doorgegeven
    public abstract Object convertToNativeGeometryObject(Geometry param) throws SQLException, ParseException;
    public abstract Geometry convertToJTSGeometryObject(Object nativeObj);
    public abstract String createPSGeometryPlaceholder() throws SQLException;
    public abstract boolean isPmdKnownBroken();
    
    public abstract String getGeomTypeName();

}
