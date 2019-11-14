/*
 *  Copyright (C) 2019  B3Partners B.V.

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.brmo.loader.checks;

import java.util.HashMap;
import java.util.Map;
import nl.b3p.brmo.loader.entity.Bericht;

/**
 *
 * @author meine
 */
public class Afgifte {
    
    private String klantnummer;
    private String contractnummer;
    private String datum;
    private String bestandsnaam;
    private boolean rapport;
    private boolean geleverd;
    
    private boolean foundInStaging;
    private Map<Bericht.STATUS, Integer> statussen = new HashMap<>();

    public String getKlantnummer() {
        return klantnummer;
    }

    public void setKlantnummer(String klantnummer) {
        this.klantnummer = klantnummer;
    }

    public String getContractnummer() {
        return contractnummer;
    }

    public void setContractnummer(String contractnummer) {
        this.contractnummer = contractnummer;
    }

    public String getDatum() {
        return datum;
    }

    public void setDatum(String datum) {
        this.datum = datum;
    }

    public String getBestandsnaam() {
        return bestandsnaam;
    }

    public void setBestandsnaam(String bestandsnaam) {
        this.bestandsnaam = bestandsnaam;
    }

    public boolean isRapport() {
        return rapport;
    }

    public void setRapport(boolean rapport) {
        this.rapport = rapport;
    }

    public boolean isGeleverd() {
        return geleverd;
    }

    public void setGeleverd(boolean geleverd) {
        this.geleverd = geleverd;
    }

    public boolean isFoundInStaging() {
        return foundInStaging;
    }

    public void setFoundInStaging(boolean foundInStaging) {
        this.foundInStaging = foundInStaging;
    }

    public Map<Bericht.STATUS, Integer> getStatussen() {
        return statussen;
    }

    public void setStatussen(Map<Bericht.STATUS, Integer> statussen) {
        this.statussen = statussen;
    }
    
    
    @Override
    public String toString(){
        String res = "Afgifte [" + bestandsnaam + ", " + datum+ ", Found: " + foundInStaging + "]";
        if(!statussen.keySet().isEmpty()){
            for (Bericht.STATUS status : statussen.keySet()) {
                res += "\n" + status.name() + ": "+ statussen.get(status);
            }
        }
        return res;
    }
}
