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
import com.vividsolutions.jts.geom.Point;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class Spoorbaandeel extends TopNLEntity {

    private String typeInfrastructuur;
    private String typeSpoorbaan;
    private String fysiekVoorkomen;
    private String spoorbreedte;
    private String aantalSporen;
    private String vervoerfunctie;
    private Boolean elektrificatie;
    private String status;
    private String brugnaam;
    private String tunnelnaam;
    private String baanvaknaam;
    private Long hoogteniveau;
    
    private LineString geometrie;
    private Point puntGeometrie;

    public String getTypeInfrastructuur() {
        return typeInfrastructuur;
    }

    public void setTypeInfrastructuur(String typeInfrastructuur) {
        this.typeInfrastructuur = typeInfrastructuur;
    }

    public String getTypeSpoorbaan() {
        return typeSpoorbaan;
    }

    public void setTypeSpoorbaan(String typeSpoorbaan) {
        this.typeSpoorbaan = typeSpoorbaan;
    }

    public String getFysiekVoorkomen() {
        return fysiekVoorkomen;
    }

    public void setFysiekVoorkomen(String fysiekVoorkomen) {
        this.fysiekVoorkomen = fysiekVoorkomen;
    }

    public String getSpoorbreedte() {
        return spoorbreedte;
    }

    public void setSpoorbreedte(String spoorbreedte) {
        this.spoorbreedte = spoorbreedte;
    }

    public String getAantalSporen() {
        return aantalSporen;
    }

    public void setAantalSporen(String aantalSporen) {
        this.aantalSporen = aantalSporen;
    }

    public String getVervoerfunctie() {
        return vervoerfunctie;
    }

    public void setVervoerfunctie(String vervoerfunctie) {
        this.vervoerfunctie = vervoerfunctie;
    }

    public Boolean getElektrificatie() {
        return elektrificatie;
    }

    public void setElektrificatie(Boolean elektrificatie) {
        this.elektrificatie = elektrificatie;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBrugnaam() {
        return brugnaam;
    }

    public void setBrugnaam(String brugnaam) {
        this.brugnaam = brugnaam;
    }

    public String getTunnelnaam() {
        return tunnelnaam;
    }

    public void setTunnelnaam(String tunnelnaam) {
        this.tunnelnaam = tunnelnaam;
    }

    public String getBaanvaknaam() {
        return baanvaknaam;
    }

    public void setBaanvaknaam(String baanvaknaam) {
        this.baanvaknaam = baanvaknaam;
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

    public Point getPuntGeometrie() {
        return puntGeometrie;
    }

    public void setPuntGeometrie(Point puntGeometrie) {
        this.puntGeometrie = puntGeometrie;
    }
}
