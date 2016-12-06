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
import java.util.Arrays;
import java.util.Collection;
import nl.b3p.topnl.converters.DbUtilsGeometryColumnConverter;
import nl.b3p.topnl.entities.FunctioneelGebied;
import nl.b3p.topnl.entities.Hoogte;
import nl.b3p.topnl.entities.TopNLEntity;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */

@RunWith(Parameterized.class)
public class DatabaseTest extends TestUtil{
    
    private Database instance = null;
    private final WKTReader wkt= new WKTReader();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private final String identificatie = "1616161616";
    private TopNLType type;
    

    @Parameters(name="Type: {0}")
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]
            {
                {TopNLType.TOP50NL},
                {TopNLType.TOP100NL},
                {TopNLType.TOP250NL}
            }
        );
    }
    
    public DatabaseTest(TopNLType type) {
        this.type = type;
        this.useDB = true; 
    }
    

    @Before
    public void before() throws SQLException{
        instance = new Database(datasource);
    }

    @Test
    public void testSaveHoogte()throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        System.out.println("save");
        Geometry p = wkt.read("POINT (1 2)");
        Hoogte e = new Hoogte();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setReferentieVlak("uitgevlakt");
        e.setTypeHoogte("superhoog");
        e.setHoogte(16.06);
        
        instance.save(e);

        QueryRunner run = new QueryRunner(datasource);

        ResultSetHandler<Hoogte> h = new BeanHandler<>(Hoogte.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        Hoogte real = run.query("SELECT * FROM "  + type.getType() + ".hoogte WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);
        
        assertEquals(e.getReferentieVlak(),real.getReferentieVlak());
        assertEquals(e.getTypeHoogte(),real.getTypeHoogte());
        assertEquals(e.getHoogte(),real.getHoogte());
        assertEquals(p,real.getGeometrie());
    }
    
    @Test
    public void testSaveFunctioneelgebied()throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        System.out.println("save");
        Geometry p = wkt.read("POINT (1 2)");
        FunctioneelGebied e = new FunctioneelGebied();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setNaamFries("boers");
        e.setNaamNL("normaal");
        e.setSoortnaam("iets");
        e.setTypeFunctioneelGebied("typerdepiep");
        
        instance.save(e);

        QueryRunner run = new QueryRunner(datasource);

        ResultSetHandler<FunctioneelGebied> h = new BeanHandler<>(FunctioneelGebied.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        FunctioneelGebied real = run.query("SELECT * FROM "  + type.getType() + ".FunctioneelGebied WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);
        
        assertEquals(p,real.getGeometrie());
        assertEquals(e.getNaamFries(),real.getNaamFries());
        assertEquals(e.getNaamNL(),real.getNaamNL());
        assertEquals(e.getTypeFunctioneelGebied(),real.getTypeFunctioneelGebied());
        assertEquals(e.getSoortnaam(),real.getSoortnaam());
    }
    
    public void getStandardTestTopNLEntity(TopNLEntity e,TopNLType type) throws ParseException {
        e.setIdentificatie(identificatie);
        e.setBronactualiteit(sdf.parse("2016-06-16"));
        e.setBronbeschrijving("beschrijving");
        e.setBrontype("typje");
        e.setObjectBeginTijd(sdf.parse("2016-01-01"));
        e.setObjectEindTijd(sdf.parse("2016-01-02"));
        e.setVisualisatieCode(166L);
        e.setTopnltype(type.getType());
    }
    
    public void testStandardTopNLEntity(TopNLEntity real, TopNLEntity e) {
        assertEquals(e.getBronactualiteit(), real.getBronactualiteit());
        assertEquals(e.getBronbeschrijving(), real.getBronbeschrijving());
        assertEquals(e.getBronnauwkeurigheid(), real.getBronnauwkeurigheid());
        assertEquals(e.getBrontype(), real.getBrontype());
        assertEquals(e.getIdentificatie(), real.getIdentificatie());
        assertEquals(e.getObjectBeginTijd(), real.getObjectBeginTijd());
        assertEquals(e.getObjectEindTijd(), real.getObjectEindTijd());
        assertEquals(e.getTopnltype(), real.getTopnltype());
        assertEquals(e.getVisualisatieCode(), real.getVisualisatieCode());
    }
}
