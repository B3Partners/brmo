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
import java.sql.SQLException;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.brmo.loader.jdbc.GeometryJdbcConverterFactory;
import nl.b3p.topnl.converters.DbUtilsGeometryColumnConverter;
import nl.b3p.topnl.entities.FunctioneelGebied;
import nl.b3p.topnl.entities.Hoogte;
import nl.b3p.topnl.entities.TopNLEntity;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class Database {
    protected final static Log log = LogFactory.getLog(Database.class);
 
    private final DataSource dataSource;
    private GeometryJdbcConverter gjc;
    
    public Database(DataSource ds) throws SQLException{
        this.dataSource = ds;
        if(ds !=null){
            gjc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(dataSource.getConnection());
        }
    }
    
    public void save(TopNLEntity entity) throws ParseException{
        
        try {
          
            if(entity instanceof Hoogte){
                saveHoogte(entity);
            }else if(entity instanceof FunctioneelGebied){
                saveFunctioneelGebied(entity);
            } else{
                throw new IllegalArgumentException ("Type of entity not (yet) implemented.");
            }
            
        } catch (SQLException e) {
            log.error("Error inserting entity: ", e);
        }
    }
    
    private FunctioneelGebied saveFunctioneelGebied(TopNLEntity entity) throws SQLException, ParseException {
        FunctioneelGebied h = (FunctioneelGebied) entity;
        QueryRunner run = new QueryRunner(dataSource);

        ResultSetHandler<FunctioneelGebied> handler = new BeanHandler(FunctioneelGebied.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie().toText());
        Object[] args = getVarargs(entity,
                h.getTypeFunctioneelGebied(),
                h.getSoortnaam(),
                h.getNaamNL(),
                h.getNaamFries(),
                nativeGeom);
        FunctioneelGebied inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".functioneelgebied (" + getTopNLEntityColumns() + ",typeFunctioneelGebied,soortnaam,naamNL,naamFries,geometrie) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?,?,?)",
                handler,
                args);

        return inserted;
    }

    private Hoogte saveHoogte(TopNLEntity entity) throws SQLException, ParseException {
        Hoogte h = (Hoogte) entity;
        QueryRunner run = new QueryRunner(dataSource);
        
        ResultSetHandler<Hoogte> handler = new BeanHandler(Hoogte.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie().toText());
        Object [] args = getVarargs(entity,
                h.getTypeHoogte(),
                h.getReferentieVlak(),
                h.getHoogte(),
                nativeGeom);
        Hoogte inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".hoogte ("+ getTopNLEntityColumns() +",typeHoogte,referentieVlak,hoogte, geometrie) VALUES ("+ getTopNLEntityReplacementChars() + ",?,?,?,?)", 
                handler, 
                args);

        return inserted;
    }

    private String getTopNLEntityColumns(){
        return "identificatie,topnltype,brontype,bronactualiteit,bronbeschrijving,bronnauwkeurigheid,objectBeginTijd,objectEindTijd,visualisatieCode";
    }
    
    private String getTopNLEntityReplacementChars(){
        return "?,?,?,?,?,?,?,?,?";
    }
    
    private Object[] getVarargs(TopNLEntity entity, Object ... specificArgs) {
        Object[] genericArgs = {
            entity.getIdentificatie(),
            entity.getTopnltype(),
            entity.getBrontype(),
            entity.getBronactualiteit() != null ? new java.sql.Date(entity.getBronactualiteit().getTime()) : null,
            entity.getBronbeschrijving(),
            entity.getBronnauwkeurigheid(),
            entity.getObjectBeginTijd() != null ? new java.sql.Date(entity.getObjectBeginTijd().getTime()) : null,
            entity.getObjectEindTijd() != null ? new java.sql.Date(entity.getObjectEindTijd().getTime()) : null,
            entity.getVisualisatieCode()
        };
        int numGeneric = genericArgs.length;
        Object[] completeArgs = new Object[numGeneric + specificArgs.length];
        System.arraycopy(genericArgs, 0, completeArgs, 0, numGeneric);
        
        for (int i = 0; i < specificArgs.length; i++) {
            Object specificArg = specificArgs[i];
            completeArgs [numGeneric+ i] = specificArg;
        }

        return completeArgs;
    }
    
    public GeometryJdbcConverter getGjc() {
        return gjc;
    }    
    
}
