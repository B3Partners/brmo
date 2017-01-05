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
package nl.b3p.topnl.entities;

import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class Terrein extends TopNLEntity{
    
    private String typeLandgebruik;
    private String naam;
    
    private Polygon geometrie;

    public String getTypeLandgebruik() {
        return typeLandgebruik;
    }

    public void setTypeLandgebruik(String typeLandgebruik) {
        this.typeLandgebruik = typeLandgebruik;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public Polygon getGeometrie() {
        return geometrie;
    }

    public void setGeometrie(Polygon geometrie) {
        this.geometrie = geometrie;
    }
    
}
