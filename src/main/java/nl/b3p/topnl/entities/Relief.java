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

import com.vividsolutions.jts.geom.LineString;
import java.math.BigInteger;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class Relief extends TopNLEntity{
    private String typeRelief;
    private String hoogteklasse;
    private Long hoogteniveau;
    private LineString geometrie;
    private LineString taludLageZijde;
    private LineString taludHogeZijde;

    public String getTypeRelief() {
        return typeRelief;
    }

    public void setTypeRelief(String typeRelief) {
        this.typeRelief = typeRelief;
    }

    public String getHoogteklasse() {
        return hoogteklasse;
    }

    public void setHoogteklasse(String hoogteklasse) {
        this.hoogteklasse = hoogteklasse;
    }

    public LineString getGeometrie() {
        return geometrie;
    }

    public void setGeometrie(LineString geometrie) {
        this.geometrie = geometrie;
    }

    public Long getHoogteniveau() {
        return hoogteniveau;
    }

    public void setHoogteniveau(Long hoogteniveau) {
        this.hoogteniveau = hoogteniveau;
    }

    public LineString getTaludLageZijde() {
        return taludLageZijde;
    }

    public void setTaludLageZijde(LineString taludLageZijde) {
        this.taludLageZijde = taludLageZijde;
    }

    public LineString getTaludHogeZijde() {
        return taludHogeZijde;
    }

    public void setTaludHogeZijde(LineString taludHogeZijde) {
        this.taludHogeZijde = taludHogeZijde;
    }
    
    
}
