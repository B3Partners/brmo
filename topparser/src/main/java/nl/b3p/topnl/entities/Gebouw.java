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

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class Gebouw extends TopNLEntity{
    private String typeGebouw;
    private String status;
    private String fysiekVoorkomen;
    private String hoogteklasse;
    private Double hoogte;
    private String soortnaam;
    private String naam;
    private String naamFries;
    
    
    private Geometry geometrie;

    public String getTypeGebouw() {
        return typeGebouw;
    }

    public void setTypeGebouw(String typeGebouw) {
        this.typeGebouw = typeGebouw;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Geometry getGeometrie() {
        return geometrie;
    }

    public void setGeometrie(Geometry geometrie) {
        this.geometrie = geometrie;
    }

    public String getFysiekVoorkomen() {
        return fysiekVoorkomen;
    }

    public void setFysiekVoorkomen(String fysiekVoorkomen) {
        this.fysiekVoorkomen = fysiekVoorkomen;
    }

    public String getHoogteklasse() {
        return hoogteklasse;
    }

    public void setHoogteklasse(String hoogteklasse) {
        this.hoogteklasse = hoogteklasse;
    }

    public Double getHoogte() {
        return hoogte;
    }

    public void setHoogte(Double hoogte) {
        this.hoogte = hoogte;
    }

    public String getSoortnaam() {
        return soortnaam;
    }

    public void setSoortnaam(String soortnaam) {
        this.soortnaam = soortnaam;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public String getNaamFries() {
        return naamFries;
    }

    public void setNaamFries(String naamFries) {
        this.naamFries = naamFries;
    }
    
    
}
