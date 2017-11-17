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
public class Wegdeel extends TopNLEntity {

    private String typeInfrastructuur;
    private String typeWeg;
    private String hoofdverkeersgebruik;
    private String fysiekVoorkomen;
    private String verhardingsbreedteklasse;
    private Boolean gescheidenRijbaan;
    private String verhardingstype;
    private Long aantalRijstroken;
    private Long hoogteniveau;
    private String status;
    private String naam;
    private Boolean isBAGnaam;
    private String aWegnummer;
    private String nWegnummer;
    private String eWegnummer;
    private String sWegnummer;
    private String afritnummer;
    private String afritnaam;
    private String knooppuntnaam;
    private String brugnaam;
    private String tunnelnaam;

    private Geometry geometrie;
    private Geometry hartGeometrie;

    public String getTypeInfrastructuur() {
        return typeInfrastructuur;
    }

    public void setTypeInfrastructuur(String typeInfrastructuur) {
        this.typeInfrastructuur = typeInfrastructuur;
    }

    public String getTypeWeg() {
        return typeWeg;
    }

    public void setTypeWeg(String typeWeg) {
        this.typeWeg = typeWeg;
    }

    public String getHoofdverkeersgebruik() {
        return hoofdverkeersgebruik;
    }

    public void setHoofdverkeersgebruik(String hoofdverkeersgebruik) {
        this.hoofdverkeersgebruik = hoofdverkeersgebruik;
    }

    public String getFysiekVoorkomen() {
        return fysiekVoorkomen;
    }

    public void setFysiekVoorkomen(String fysiekVoorkomen) {
        this.fysiekVoorkomen = fysiekVoorkomen;
    }

    public String getVerhardingsbreedteklasse() {
        return verhardingsbreedteklasse;
    }

    public void setVerhardingsbreedteklasse(String verhardingsbreedteklasse) {
        this.verhardingsbreedteklasse = verhardingsbreedteklasse;
    }

    public Boolean getGescheidenRijbaan() {
        return gescheidenRijbaan;
    }

    public void setGescheidenRijbaan(Boolean gescheidenRijbaan) {
        this.gescheidenRijbaan = gescheidenRijbaan;
    }

    public String getVerhardingstype() {
        return verhardingstype;
    }

    public void setVerhardingstype(String verhardingstype) {
        this.verhardingstype = verhardingstype;
    }

    public Long getAantalRijstroken() {
        return aantalRijstroken;
    }

    public void setAantalRijstroken(Long aantalRijstroken) {
        this.aantalRijstroken = aantalRijstroken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public Boolean getIsBAGnaam() {
        return isBAGnaam;
    }

    public void setIsBAGnaam(Boolean isBAGnaam) {
        this.isBAGnaam = isBAGnaam;
    }

    public String getaWegnummer() {
        return aWegnummer;
    }

    public void setaWegnummer(String aWegnummer) {
        this.aWegnummer = aWegnummer;
    }

    public String getnWegnummer() {
        return nWegnummer;
    }

    public void setnWegnummer(String nWegnummer) {
        this.nWegnummer = nWegnummer;
    }

    public String geteWegnummer() {
        return eWegnummer;
    }

    public void seteWegnummer(String eWegnummer) {
        this.eWegnummer = eWegnummer;
    }

    public String getsWegnummer() {
        return sWegnummer;
    }

    public void setsWegnummer(String sWegnummer) {
        this.sWegnummer = sWegnummer;
    }

    public String getAfritnummer() {
        return afritnummer;
    }

    public void setAfritnummer(String afritnummer) {
        this.afritnummer = afritnummer;
    }

    public String getAfritnaam() {
        return afritnaam;
    }

    public void setAfritnaam(String afritnaam) {
        this.afritnaam = afritnaam;
    }

    public String getKnooppuntnaam() {
        return knooppuntnaam;
    }

    public void setKnooppuntnaam(String knooppuntnaam) {
        this.knooppuntnaam = knooppuntnaam;
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

    public Geometry getHartGeometrie() {
        return hartGeometrie;
    }

    public void setHartGeometrie(Geometry hartGeometrie) {
        this.hartGeometrie = hartGeometrie;
    }
}
