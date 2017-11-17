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
public class Waterdeel extends TopNLEntity {

    private String typeWater;
    private String breedteklasse;
    private String fysiekVoorkomen;
    private String voorkomen;
    private Boolean getijdeinvloed;
    private String vaarwegklasse;
    private String naamOfficieel;
    private String naamNL;
    private String naamFries;
    private Boolean isBAGnaam;
    private String sluisnaam;
    private String brugnaam;
    private Long hoogteniveau;
    private String functie;
    private boolean hoofdAfwatering;
    
    private Geometry geometrie;
    
    public String getTypeWater() {
        return typeWater;
    }

    public void setTypeWater(String typeWater) {
        this.typeWater = typeWater;
    }

    public String getBreedteklasse() {
        return breedteklasse;
    }

    public void setBreedteklasse(String breedteklasse) {
        this.breedteklasse = breedteklasse;
    }

    public String getFysiekVoorkomen() {
        return fysiekVoorkomen;
    }

    public void setFysiekVoorkomen(String fysiekVoorkomen) {
        this.fysiekVoorkomen = fysiekVoorkomen;
    }

    public String getVoorkomen() {
        return voorkomen;
    }

    public void setVoorkomen(String voorkomen) {
        this.voorkomen = voorkomen;
    }

    public Boolean getGetijdeinvloed() {
        return getijdeinvloed;
    }

    public void setGetijdeinvloed(Boolean getijdeinvloed) {
        this.getijdeinvloed = getijdeinvloed;
    }

    public String getVaarwegklasse() {
        return vaarwegklasse;
    }

    public void setVaarwegklasse(String vaarwegklasse) {
        this.vaarwegklasse = vaarwegklasse;
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

    public Boolean getIsBAGnaam() {
        return isBAGnaam;
    }

    public void setIsBAGnaam(Boolean isBAGnaam) {
        this.isBAGnaam = isBAGnaam;
    }

    public String getSluisnaam() {
        return sluisnaam;
    }

    public void setSluisnaam(String sluisnaam) {
        this.sluisnaam = sluisnaam;
    }

    public String getBrugnaam() {
        return brugnaam;
    }

    public void setBrugnaam(String brugnaam) {
        this.brugnaam = brugnaam;
    }

    public Geometry getGeometrie() {
        return geometrie;
    }

    public void setGeometrie(Geometry geometrie) {
        this.geometrie = geometrie;
    }

    public Long getHoogteniveau() {
        return hoogteniveau;
    }

    public void setHoogteniveau(Long hoogteniveau) {
        this.hoogteniveau = hoogteniveau;
    }

    public boolean isHoofdAfwatering() {
        return hoofdAfwatering;
    }

    public void setHoofdAfwatering(boolean hoofdAfwatering) {
        this.hoofdAfwatering = hoofdAfwatering;
    }

    public String getFunctie() {
        return functie;
    }

    public void setFunctie(String functie) {
        this.functie = functie;
    }
    
}
