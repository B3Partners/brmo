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

import java.sql.SQLException;
import javax.sql.DataSource;
import nl.b3p.topnl.entities.Hoogte;
import nl.b3p.topnl.entities.TopNLEntity;
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
    
    public Database(DataSource ds){
        this.dataSource = ds;
    }
    
    public void save(TopNLEntity entity){
        
        try {
          
            if(entity instanceof Hoogte){
                saveHoogte(entity);
            }else{
                throw new IllegalArgumentException ("Type of entity not (yet) implemented.");
            }
            
        } catch (SQLException e) {
            log.error("Error inserting entity: ", e);
        }
    }

    private void saveHoogte(TopNLEntity entity) throws SQLException {
        Hoogte h = (Hoogte) entity;
        QueryRunner run = new QueryRunner(dataSource);
        
        ResultSetHandler<Hoogte> handler = new BeanHandler<>(Hoogte.class);
        
        Hoogte inserted = run.insert("INSERT INTO Hoogte (identificatie,topnltype,brontype,bronactualiteit,bronbeschrijving,bronnauwkeurigheid,objectBeginTijd,objectEindTijd,visualisatieCode,typeHoogte,referentieVlak,hoogte) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)", 
                handler, 
                h.getIdentificatie(),
                h.getTopnltype(),
                h.getBrontype(),
                h.getBronactualiteit() != null ? new java.sql.Date(h.getBronactualiteit().getTime()) : null,
                h.getBronbeschrijving(),
                h.getBronnauwkeurigheid(),
                h.getObjectBeginTijd() != null ? new java.sql.Date(h.getObjectBeginTijd().getTime()) : null,
                h.getObjectEindTijd() != null ? new java.sql.Date(h.getObjectEindTijd().getTime()) : null,
                h.getVisualisatieCode(),
                h.getTypeHoogte(),
                h.getReferentieVlak(),
                h.getHoogte());
        
      /*  int inserts = run.insert("INSERT INTO Hoogte (identificatie,topnltype,brontype,bronactualiteit,bronbeschrijving,bronnauwkeurigheid,objectBeginTijd,objectEindTijd,visualisatieCode,typeHoogte,referentieVlak,hoogte) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",h.getIdentificatie(),h.getTopnltype(),h.getBrontype(),h.getBronactualiteit(),
                h.getBronbeschrijving(),h.getBronnauwkeurigheid(),h.getObjectBeginTijd(),h.getObjectEindTijd(),h.getVisualisatieCode(),
                h.getTypeHoogte(),h.getReferentieVlak(),h.getHoogte());*/
        int a = 0;

    }
}
