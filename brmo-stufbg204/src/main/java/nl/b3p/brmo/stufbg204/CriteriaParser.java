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

import java.util.List;
import nl.egem.stuf.sector.bg._0204.PRSVraag;
import nl.egem.stuf.sector.bg._0204.VraagBericht;
import nl.egem.stuf.sector.bg._0204.VraagBericht.Body;

/**
 *
 * @author Meine Toonen
 */
public class CriteriaParser {

    public CriteriaParser() {

    }

    public String getCriteria(VraagBericht bericht) {
        String q = "";
        String entiteitType = bericht.getStuurgegevens().getEntiteittype();
        Body b = bericht.getBody();
        switch (entiteitType) {
            case "PRS": {
                q = getPRSCriterias(b);
                break;
            }
            default:
                throw new IllegalArgumentException("Entiteitstype niet ondersteund: " + entiteitType);
        }
        return q;
    }

    private String getPRSCriterias(Body b) {
        String q = "";
        List<PRSVraag> prs = b.getPRS();
        
        String from = getPRSCriteria(prs.get(0));
        String to = getPRSCriteria(prs.get(1));

        return q;
    }
    
    private String getPRSCriteria(PRSVraag prs){
prs.getPRSADRCOR();
prs.getPRSADRINS();
prs.getPRSADRVBL();
prs.getPRSIDB();
prs.getPRSNAT();
prs.getPRSPRSHUW();
prs.getPRSPRSKND();
prs.getPRSPRSOUD();
prs.getANummer();
prs.getBsnNummer();
prs.getVoornamen();
prs.getVoorletters();
prs.getVoorvoegselGeslachtsnaam();
prs.getGeslachtsnaam();
prs.getGeboortedatum();
prs.getGeboorteplaats();
prs.getCodeGeboorteland();
prs.getGeslachtsaanduiding();
prs.getDatumOverlijden();
prs.getPlaatsOverlijden();
prs.getCodeLandOverlijden();
prs.getIndicatieGeheim();
prs.getAcademischeTitel();
prs.getAdellijkeTitelPredikaat();
prs.getCodeGemeenteVanInschrijving();
prs.getDatumInschrijvingGemeente();
prs.getDatumOpschortingBijhouding();
prs.getOmschrijvingRedenOpschortingBijhouding();
prs.getCodeLandEmigratie();
prs.getDatumVertrekUitNederland();
prs.getCodeLandImmigratie();
prs.getDatumVestigingInNederland();
prs.getIndicatieGezagMinderjarige();
prs.getIndicatieCuratelestelling();
prs.getVerblijfstitel();
prs.getDatumVerkrijgingVerblijfstitel();
prs.getDatumVerliesVerblijfstitel();
prs.getAanduidingBijzonderNederlanderschap();
prs.getBurgerlijkeStaat();
prs.getAanduidingNaamgebruik();
prs.getBankgiroRekeningnummer();
prs.getSubjectnrAKR();
prs.getEMailAdres();
prs.getTelefoonnummer();
prs.getFaxnummer();
prs.getTijdvakGeldigheid();
prs.getExtraElementen();
        return "";
    }

}
