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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.egem.stuf.sector.bg._0204.PRSVraag;
import nl.egem.stuf.sector.bg._0204.VraagBericht;
import nl.egem.stuf.sector.bg._0204.VraagBericht.Body;

/**
 *
 * @author Meine Toonen
 */
public class CriteriaParser {

    private static Map<String, String> vraagToColumn = new HashMap();

    static {
        vraagToColumn.put("prsadrcor", "");
        vraagToColumn.put("prsadrins", "");
        vraagToColumn.put("prsadrvbl", "");
        vraagToColumn.put("prsidb", "");
        vraagToColumn.put("prsnat", "");
        vraagToColumn.put("prsprshuw", "");
        vraagToColumn.put("prsprsknd", "");
        vraagToColumn.put("prsprsoud", "");
        vraagToColumn.put("aNummer", "");
        vraagToColumn.put("bsnNummer", "");
        vraagToColumn.put("voornamen", "");
        vraagToColumn.put("voorletters", "");
        vraagToColumn.put("voorvoegselGeslachtsnaam", "");
        vraagToColumn.put("geslachtsnaam", "");
        vraagToColumn.put("geboortedatum", "");
        vraagToColumn.put("geboorteplaats", "");
        vraagToColumn.put("codeGeboorteland", "");
        vraagToColumn.put("geslachtsaanduiding", "");
        vraagToColumn.put("datumOverlijden", "");
        vraagToColumn.put("plaatsOverlijden", "");
        vraagToColumn.put("codeLandOverlijden", "");
        vraagToColumn.put("indicatieGeheim", "");
        vraagToColumn.put("academischeTitel", "");
        vraagToColumn.put("adellijkeTitelPredikaat", "");
        vraagToColumn.put("codeGemeenteVanInschrijving", "");
        vraagToColumn.put("datumInschrijvingGemeente", "");
        vraagToColumn.put("datumOpschortingBijhouding", "");
        vraagToColumn.put("omschrijvingRedenOpschortingBijhouding", "");
        vraagToColumn.put("codeLandEmigratie", "");
        vraagToColumn.put("datumVertrekUitNederland", "");
        vraagToColumn.put("codeLandImmigratie", "");
        vraagToColumn.put("datumVestigingInNederland", "");
        vraagToColumn.put("indicatieGezagMinderjarige", "");
        vraagToColumn.put("indicatieCuratelestelling", "");
        vraagToColumn.put("verblijfstitel", "");
        vraagToColumn.put("datumVerkrijgingVerblijfstitel", "");
        vraagToColumn.put("datumVerliesVerblijfstitel", "");
        vraagToColumn.put("aanduidingBijzonderNederlanderschap", "");
        vraagToColumn.put("burgerlijkeStaat", "");
        vraagToColumn.put("aanduidingNaamgebruik", "");
        vraagToColumn.put("bankgiroRekeningnummer", "");
        vraagToColumn.put("subjectnrAKR", "");
        vraagToColumn.put("eMailAdres", "");
        vraagToColumn.put("telefoonnummer", "");
        vraagToColumn.put("faxnummer", "");
        vraagToColumn.put("tijdvakGeldigheid", "");
        vraagToColumn.put("extraElementen", "");
    }

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
        PRSVraag.
        prs.getANummer().getName().getLocalPart()
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
prs.getANummer().getName().getLocalPart();
prs.getPRSADRCOR().getName().getLocalPart();
prs.getPRSADRINS().getName().getLocalPart();
prs.getPRSADRVBL().getName().getLocalPart();
prs.getPRSIDB().getName().getLocalPart();
prs.getPRSNAT().getName().getLocalPart();
prs.getPRSPRSHUW().getName().getLocalPart();
prs.getPRSPRSKND().getName().getLocalPart();
prs.getPRSPRSOUD().getName().getLocalPart();
prs.getANummer().getName().getLocalPart();
prs.getBsnNummer().getName().getLocalPart();
prs.getVoornamen().getName().getLocalPart();
prs.getVoorletters().getName().getLocalPart();
prs.getVoorvoegselGeslachtsnaam().getName().getLocalPart();
prs.getGeslachtsnaam().getName().getLocalPart();
prs.getGeboortedatum().getName().getLocalPart();
prs.getGeboorteplaats().getName().getLocalPart();
prs.getCodeGeboorteland().getName().getLocalPart();
prs.getGeslachtsaanduiding().getName().getLocalPart();
prs.getDatumOverlijden().getName().getLocalPart();
prs.getPlaatsOverlijden().getName().getLocalPart();
prs.getCodeLandOverlijden().getName().getLocalPart();
prs.getIndicatieGeheim().getName().getLocalPart();
prs.getAcademischeTitel().getName().getLocalPart();
prs.getAdellijkeTitelPredikaat().getName().getLocalPart();
prs.getCodeGemeenteVanInschrijving().getName().getLocalPart();
prs.getDatumInschrijvingGemeente().getName().getLocalPart();
prs.getDatumOpschortingBijhouding().getName().getLocalPart();
prs.getOmschrijvingRedenOpschortingBijhouding().getName().getLocalPart();
prs.getCodeLandEmigratie().getName().getLocalPart();
prs.getDatumVertrekUitNederland().getName().getLocalPart();
prs.getCodeLandImmigratie().getName().getLocalPart();
prs.getDatumVestigingInNederland().getName().getLocalPart();
prs.getIndicatieGezagMinderjarige().getName().getLocalPart();
prs.getIndicatieCuratelestelling().getName().getLocalPart();
prs.getVerblijfstitel().getName().getLocalPart();
prs.getDatumVerkrijgingVerblijfstitel().getName().getLocalPart();
prs.getDatumVerliesVerblijfstitel().getName().getLocalPart();
prs.getAanduidingBijzonderNederlanderschap().getName().getLocalPart();
prs.getBurgerlijkeStaat().getName().getLocalPart();
prs.getAanduidingNaamgebruik().getName().getLocalPart();
prs.getBankgiroRekeningnummer().getName().getLocalPart();
prs.getSubjectnrAKR().getName().getLocalPart();
prs.getEMailAdres().getName().getLocalPart();
prs.getTelefoonnummer().getName().getLocalPart();
prs.getFaxnummer().getName().getLocalPart();
prs.getTijdvakGeldigheid().getName().getLocalPart();
prs.getExtraElementen().getName().getLocalPart();
        return "";
    }

}
