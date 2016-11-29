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
import nl.b3p.topnl.entities.TopNLEntity;
import org.apache.commons.dbutils.QueryRunner;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class Database {
 
    private final DataSource dataSource;
    
    public Database(DataSource ds){
        this.dataSource = ds;
    }
    
    public void save(TopNLEntity entity){
        
        QueryRunner run = new QueryRunner(dataSource);
        try {
            // Execute the SQL update statement and return the number of
            // inserts that were made
            int inserts = run.update("INSERT INTO Person (name,height) VALUES (?,?)",
                    "John Doe", 1.82);
            // The line before uses varargs and autoboxing to simplify the code

            // Now it's time to rise to the occation...
            int updates = run.update("UPDATE Person SET height=? WHERE name=?",
                    2.05, "John Doe");
            // So does the line above
        } catch (SQLException sqle) {
            // Handle it
        }
    }
}
