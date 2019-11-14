/*
 * Copyright (C) 2019 B3Partners B.V.
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
package nl.b3p.brmo.persistence.staging;

import javax.persistence.Entity;

/**
 *
 * @author mprins
 */
@Entity
public class AfgifteNummerScannerProces extends AutomatischProces {

    private static final String CONTRACTNUMMER = "contractnummer";
    private static final String AFGIFTENUMMERTYPE = "afgiftenummertype";
    private static final String MISSINGNUMBERSFOUND = "ontbrekendenummersgevonden";

    /**
     * Zoekt het geconfigureerde contractnummer op.
     *
     * @return contractnummer of null
     */
    public String getContracNummer() {
        return ClobElement.nullSafeGet(this.getConfig().get(CONTRACTNUMMER));
    }

    /**
     * set contractnummer.
     *
     * @param contractnummer of null
     */
    public void setContracNummer(String contractnummer) {
        if (contractnummer == null) {
            this.getConfig().put(CONTRACTNUMMER, null);
        } else {
            this.getConfig().put(CONTRACTNUMMER, new ClobElement(contractnummer));
        }
    }

    /**
     * Zoekt het geconfigureerde afgifte nummer type op.
     *
     * @return afgiftenummertype of een lege String
     */
    public String getAfgifteNummerType() {
        return ClobElement.nullSafeGet(this.getConfig().get(AFGIFTENUMMERTYPE));
    }

    /**
     * set afgifte nummer type.
     *
     * @param afgiftenummertype of null
     */
    public void setAfgifteNummerType(String afgiftenummertype) {
        if (afgiftenummertype == null) {
            this.getConfig().put(AFGIFTENUMMERTYPE, null);
        } else {
            this.getConfig().put(AFGIFTENUMMERTYPE, new ClobElement(afgiftenummertype));
        }
    }

    public void setOntbrekendeNummersGevonden(boolean gevonden) {
        this.getConfig().put(MISSINGNUMBERSFOUND, new ClobElement(Boolean.toString(gevonden)));
    }

    public boolean getOntbrekendeNummersGevonden() {
        String s = ClobElement.nullSafeGet(this.getConfig().get(MISSINGNUMBERSFOUND));
        return (s == null ? false : Boolean.parseBoolean(s));
    }
}
