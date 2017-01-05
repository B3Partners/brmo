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

import com.vividsolutions.jts.geom.Geometry;
import java.math.BigInteger;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class Plaats extends TopNLEntity{
    
    private String typeGebied;
    private Long aantalInwoners;
    private String naamOfficieel;
    private String naamNL;
    private String naamFries;
    
    private Geometry geometrie;

    public String getTypeGebied() {
        return typeGebied;
    }

    public void setTypeGebied(String typeGebied) {
        this.typeGebied = typeGebied;
    }

    public Long getAantalInwoners() {
        return aantalInwoners;
    }

    public void setAantalInwoners(Long aantalInwoners) {
        this.aantalInwoners = aantalInwoners;
    }

    public String getNaamOfficieel() {
        return naamOfficieel;
    }

    public void setNaamOfficieel(String naamOfficieel) {
        this.naamOfficieel = naamOfficieel;
    }

    public String getNaamNL() {
        return naamNL;
    }

    public void setNaamNL(String naamNL) {
        this.naamNL = naamNL;
    }

    public String getNaamFries() {
        return naamFries;
    }

    public void setNaamFries(String naamFries) {
        this.naamFries = naamFries;
    }

    public Geometry getGeometrie() {
        return geometrie;
    }

    public void setGeometrie(Geometry geometrie) {
        this.geometrie = geometrie;
    }
    
    
}
