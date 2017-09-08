/*
 * Copyright (C) 2017 B3Partners B.V.
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
package nl.b3p.brmo.stufbg204;

import nl.egem.stuf.sector.bg._0204.ACDTabel;
import nl.egem.stuf.sector.bg._0204.ADLTabel;
import nl.egem.stuf.sector.bg._0204.AcademischeTitelPositieTovNaam;
import nl.egem.stuf.sector.bg._0204.AdellijkeTitelSoort;
import nl.egem.stuf.sector.bg._0204.ObjectFactory;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht.Body;
import nl.egem.stuf.sector.bg._0204.VraagBericht;
import nl.egem.stuf.stuf0204.ExtraElement;
import nl.egem.stuf.stuf0204.ExtraElementen;
import nl.egem.stuf.stuf0204.NoValue;
import nl.egem.stuf.stuf0204.Verwerkingssoort;

/**
 *
 * @author Meine Toonen
 */
public class AntwoordBodyFactory {

    private static ObjectFactory objFac = new ObjectFactory();

    public AntwoordBodyFactory() {

    }

    public static Body getBody(VraagBericht bericht) {
        Body b = new Body();
        String entiteitsType = bericht.getStuurgegevens().getEntiteittype();
        switch (entiteitsType) {
            case "ACD": {
                ACDTabel t = new ACDTabel();
                ACDTabel.Code c = new ACDTabel.Code();
                c.setExact(true);
                c.setKerngegeven(true);
                c.setNoValue(NoValue.GEEN_WAARDE);
                c.setValue("pietje");

                ACDTabel.Omschrijving o = new ACDTabel.Omschrijving();
                o.setValue("Professor");
                o.setExact(Boolean.FALSE);

                ACDTabel.PositieTovNaam p = new ACDTabel.PositieTovNaam();
                p.setValue(AcademischeTitelPositieTovNaam.V);

                ExtraElementen e = new ExtraElementen();
                ExtraElement ex = new ExtraElement();
                ex.setNaam("ex1");
                ex.setValue("val1");
                e.getExtraElement().add(ex);

                t.setNoValue(NoValue.NIET_ONDERSTEUND);
                t.setSoortEntiteit(entiteitsType);
                t.setCode(objFac.createACDTabelCode(c));
                t.setOmschrijving(objFac.createACDTabelOmschrijving(o));
                t.setVerwerkingssoort(Verwerkingssoort.V);
                t.setPositieTovNaam(objFac.createACDTabelPositieTovNaam(p));
                t.setExtraElementen(e);

                b.getACD().add(t);
                break;
            }
            case "ADL": {
                ADLTabel t = new ADLTabel();
                ADLTabel.Soort s = new ADLTabel.Soort();
                s.setValue(AdellijkeTitelSoort.A);
               
                
                ADLTabel.Omschrijving o = new ADLTabel.Omschrijving();
                o.setValue("Koning");
                o.setExact(Boolean.FALSE);

                t.setNoValue(NoValue.GEEN_WAARDE);
                t.setOmschrijving(objFac.createADLTabelOmschrijving(o));
                t.setSoort(objFac.createADLTabelSoort(s));
                t.setVerwerkingssoort(Verwerkingssoort.R);
                t.setSoortEntiteit(entiteitsType);
                b.getADL().add(t);
                break;
            }
            default:
                throw new IllegalArgumentException("Entiteitstype niet ondersteund: " + entiteitsType);
        }
        return b;
    }

}
