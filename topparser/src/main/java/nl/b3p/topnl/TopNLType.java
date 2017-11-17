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
package nl.b3p.topnl;

/**
 *
 * @author Meine Toonen
 */
public enum TopNLType {
    TOP10NL ("top10nl"), 
    TOP50NL ("top50nl"), 
    TOP100NL("top100nl"), 
    TOP250NL("top250nl");
    
    private final String type;
    
    TopNLType(String type){
        this.type =  type;
    }

    public String getType() {
        return type;
    }
    
    public static boolean isTopNLType(String type){
        for (TopNLType value : TopNLType.values()) {
            if(value.getType().equalsIgnoreCase(type)){
                return true;
            }
        }
        return false;
    }
    
}
