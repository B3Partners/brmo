/*
 *  Copyright (C) 2019  B3Partners B.V.

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.brmo.loader.checks;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Meine Toonen
 */
public class AfgifteChecker {
    
    private List<Afgifte> afgiftes;
    public void init(String input) throws IOException{
        // parse csv/excel
        AfgiftelijstParser ap = new AfgiftelijstParser();
        afgiftes = ap.parse(input);
    }
    
    public void check(){
        // ga per afgifte langs of het bericht in de staging zit
        for (Afgifte afgifte : afgiftes) {
            check(afgifte);
        }
    }
    
    private void check(Afgifte afgifte){
        // kijk of afgifte bestaat in staging
            // zo ja, haal status op en schrijf naar afgifte
            // zo nee, herstel bestandsnamen, en kijk of het dan bestaat
                // zo ja, haal status op en schrijf naar afgifte
                // zo nee, schrijf status weg naar afgifte
                
    }
    
    public String getResults(){
        return null;
    }
    
}
