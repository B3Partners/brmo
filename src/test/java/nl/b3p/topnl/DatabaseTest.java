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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import nl.b3p.topnl.converters.DbUtilsGeometryColumnConverter;
import nl.b3p.topnl.entities.Hoogte;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class DatabaseTest extends TestUtil{
    
    private Database instance = null;
    private WKTReader wkt= new WKTReader();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    public DatabaseTest() {
        
        this.useDB = true; 
    }
    

    @Before
    public void before() throws SQLException{
        instance = new Database(datasource);
    }

   // @Test
    public void testSaveHoogte50() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        testSave(TopNLType.TOP50NL);
    }
    
  //  @Test
    public void testSaveHoogte100() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        testSave(TopNLType.TOP100NL);
    }
    
    @Test
    public void testSaveHoogte250() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        testSave(TopNLType.TOP250NL);
    }
    
    private void testSave(TopNLType type)throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        System.out.println("save");
        Geometry p = wkt.read("POINT (1 2)");
        Hoogte e = new Hoogte();
        String identificatie = "1616161616";
        e.setIdentificatie(identificatie);
        e.setHoogte(16.06);
        e.setBronactualiteit(sdf.parse("2016-06-16"));
        e.setBronbeschrijving("beschrijving");
        e.setBrontype("typje");
        e.setTopnltype(type.getType());
        e.setGeometrie(p);
        e.setObjectBeginTijd(sdf.parse("2016-01-01"));
        e.setObjectEindTijd(sdf.parse("2016-01-02"));
        e.setReferentieVlak("uitgevlakt");
        e.setTypeHoogte("superhoog");
        e.setVisualisatieCode(166L);
        
        instance.save(e);

        QueryRunner run = new QueryRunner(datasource);

        ResultSetHandler<Hoogte> h = new BeanHandler<>(Hoogte.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        Hoogte real = run.query("SELECT * FROM "  + type.getType() + ".hoogte WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        assertEquals(e.getBronactualiteit(),real.getBronactualiteit());
        assertEquals(e.getBronbeschrijving(),real.getBronbeschrijving());
        assertEquals(e.getBronnauwkeurigheid(),real.getBronnauwkeurigheid());
        assertEquals(e.getBrontype(),real.getBrontype());
        assertEquals(e.getHoogte(),real.getHoogte());
        assertEquals(e.getIdentificatie(),real.getIdentificatie());
        assertEquals(e.getObjectBeginTijd(),real.getObjectBeginTijd());
        assertEquals(e.getObjectEindTijd(),real.getObjectEindTijd());
        assertEquals(e.getReferentieVlak(),real.getReferentieVlak());
        assertEquals(e.getTopnltype(),real.getTopnltype());
        assertEquals(e.getTypeHoogte(),real.getTypeHoogte());
        assertEquals(e.getVisualisatieCode(),real.getVisualisatieCode());
        assertEquals(p,real.getGeometrie());
    }
    
    
    
}
