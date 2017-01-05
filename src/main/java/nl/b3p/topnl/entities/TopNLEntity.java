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

import java.util.Date;

/**
 *
 * @author Meine Toonen
 */
public abstract class TopNLEntity {
    
    protected String topnltype;
    private Integer id;
    private String identificatie;
    private String brontype;
    private Date bronactualiteit;
    private String bronbeschrijving;
    private Double bronnauwkeurigheid;
    private Date objectBeginTijd;
    private Date objectEindTijd;
    private Long visualisatieCode;
    
    public String getTopnltype() {
        return topnltype;
    }

    public void setTopnltype(String topnltype) {
        this.topnltype = topnltype;
    }

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
}
