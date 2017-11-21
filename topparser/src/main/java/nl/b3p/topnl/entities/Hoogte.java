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
import java.util.Date;

/**
 *
 * @author meine
 */
public class Hoogte extends TopNLEntity{
    private String typeHoogte;
    private Geometry geometrie;
    private String referentieVlak;
    private Double hoogte;
    

    public String getReferentieVlak() {
        return referentieVlak;
    }

    public void setReferentieVlak(String referentieVlak) {
        this.referentieVlak = referentieVlak;
    }

    public Double getHoogte() {
        return hoogte;
    }

    public void setHoogte(Double hoogte) {
        this.hoogte = hoogte;
    }
    
    public String getTypeHoogte() {
        return typeHoogte;
    }

    public void setTypeHoogte(String typeHoogte) {
        this.typeHoogte = typeHoogte;
    }

    public Geometry getGeometrie() {
        return geometrie;
    }

    public void setGeometrie(Geometry geometrie) {
        this.geometrie = geometrie;
    }

    
}
