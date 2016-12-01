/*
 * Copyright (C) 2016 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
    private Integer id;
    private String identificatie;
    private String brontype;
    private Date bronactualiteit;
    private String bronbeschrijving;
    private Double bronnauwkeurigheid;
    private Date objectBeginTijd;
    private Date objectEindTijd;
    private Long visualisatieCode;
    private String typeHoogte;
    private String referentieVlak;
    private Double hoogte;
    
    
    private Geometry geometrie;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdentificatie() {
        return identificatie;
    }

    public void setIdentificatie(String identificatie) {
        this.identificatie = identificatie;
    }

    public String getBrontype() {
        return brontype;
    }

    public void setBrontype(String brontype) {
        this.brontype = brontype;
    }

    public Date getBronactualiteit() {
        return bronactualiteit;
    }

    public void setBronactualiteit(Date bronactualiteit) {
        this.bronactualiteit = bronactualiteit;
    }

    public String getBronbeschrijving() {
        return bronbeschrijving;
    }

    public void setBronbeschrijving(String bronbeschrijving) {
        this.bronbeschrijving = bronbeschrijving;
    }

    public Double getBronnauwkeurigheid() {
        return bronnauwkeurigheid;
    }

    public void setBronnauwkeurigheid(Double bronnauwkeurigheid) {
        this.bronnauwkeurigheid = bronnauwkeurigheid;
    }

    public Date getObjectBeginTijd() {
        return objectBeginTijd;
    }

    public void setObjectBeginTijd(Date objectBeginTijd) {
        this.objectBeginTijd = objectBeginTijd;
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

    public Long getVisualisatieCode() {
        return visualisatieCode;
    }

    public void setVisualisatieCode(Long visualisatieCode) {
        this.visualisatieCode = visualisatieCode;
    }

    public Date getObjectEindTijd() {
        return objectEindTijd;
    }

    public void setObjectEindTijd(Date objectEindTijd) {
        this.objectEindTijd = objectEindTijd;
    }

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
    
    
}
