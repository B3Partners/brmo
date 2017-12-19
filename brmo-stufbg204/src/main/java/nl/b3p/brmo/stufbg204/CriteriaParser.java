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

    private static final Map<String, String> vraagToColumn = new HashMap();

    static {
        //nat_prs
        vraagToColumn.put("voornamen", "nm_voornamen");
        vraagToColumn.put("voorletters", "na_voorletters_aanschrijving");
        vraagToColumn.put("voorvoegselGeslachtsnaam", "nm_voorvoegsel_geslachtsnaam");
        vraagToColumn.put("geslachtsnaam", "nm_geslachtsnaam");
        vraagToColumn.put("adellijkeTitelPredikaat", "nm_adellijke_titel_predikaat");
        vraagToColumn.put("geslachtsaanduiding", "geslachtsaand");
        vraagToColumn.put("academischeTitel", "fk_2acd_code");
        vraagToColumn.put("aanduidingNaamgebruik", "aand_naamgebruik");

        // ingeschr_nat_prs
        vraagToColumn.put("aNummer", "a_nummer");
        vraagToColumn.put("bsn-nummer", "bsn");
        vraagToColumn.put("burgerlijkeStaat", "burgerlijke_staat");
        vraagToColumn.put("geboortedatum", "gb_geboortedatum");
        vraagToColumn.put("datumInschrijvingGemeente", "datum_inschrijving_in_gemeente");
        vraagToColumn.put("datumVerkrijgingVerblijfstitel", "datum_verkr_nation");
        vraagToColumn.put("datumVerliesVerblijfstitel", "datum_verlies_nation");
        vraagToColumn.put("datumVertrekUitNederland", "datum_vertrek_uit_nederland");
        vraagToColumn.put("datumVestigingInNederland", "datum_vestg_in_nederland");
        vraagToColumn.put("codeGemeenteVanInschrijving", "gemeente_van_inschrijving");
        vraagToColumn.put("geboorteplaats", "gb_geboorteplaats");
        vraagToColumn.put("codeGeboorteland", "fk_gb_lnd_code_iso");
        vraagToColumn.put("datumOverlijden", "ol_overlijdensdatum");
        vraagToColumn.put("plaatsOverlijden", "fk_gb_lnd_code_iso");
        vraagToColumn.put("codeLandOverlijden", "fk_ol_lnd_code_iso");
        vraagToColumn.put("datumOpschortingBijhouding", "datum_opschorting_bijhouding");
        vraagToColumn.put("omschrijvingRedenOpschortingBijhouding", "reden_opschorting_bijhouding");

        //subject
        vraagToColumn.put("eMailAdres", "emailadres");
        vraagToColumn.put("telefoonnummer", "telefoonnummer");
        vraagToColumn.put("bankgiroRekeningnummer", "rn_bankrekeningnummer");
        vraagToColumn.put("faxnummer", "fax_nummer");

        vraagToColumn.put("indicatieGeheim", "");
        vraagToColumn.put("prsadrcor", "");
        vraagToColumn.put("prsadrins", "");
        vraagToColumn.put("prsadrvbl", "");
        vraagToColumn.put("prsidb", "");
        vraagToColumn.put("prsnat", "");
        vraagToColumn.put("prsprshuw", "");
        vraagToColumn.put("prsprsknd", "");
        vraagToColumn.put("prsprsoud", "");
        vraagToColumn.put("codeLandEmigratie", "");
        vraagToColumn.put("codeLandImmigratie", "");
        vraagToColumn.put("indicatieGezagMinderjarige", "");
        vraagToColumn.put("indicatieCuratelestelling", "");
        vraagToColumn.put("verblijfstitel", "");
        vraagToColumn.put("aanduidingBijzonderNederlanderschap", "");
        vraagToColumn.put("subjectnrAKR", "");
        vraagToColumn.put("tijdvakGeldigheid", "");
        vraagToColumn.put("extraElementen", "");
    }

    public CriteriaParser() {

    }

    public String getCriteria(VraagBericht bericht) throws IllegalArgumentException,UnsupportedOperationException {
        String q;
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
        if (q != null) {
            q = "WHERE " + q;
        }
        return q;
    }

    private String getPRSCriterias(Body b) throws IllegalArgumentException,UnsupportedOperationException {
        String q;
        List<PRSVraag> prs = b.getPRS();

        String from = getPRSCriteria(prs.get(0), true);
        String toCheck = getPRSCriteria(prs.get(1), true); // maak even een tweede filter aan met dezelfde operator, om te checken of er op hetzelfde gecheckt wordt == equals 
        String to = getPRSCriteria(prs.get(1), false);
        if (from == null && to == null) {
            return null;
        }
        if (from.equals(toCheck)) {
            q = from.replace(">", "=");
        } else {
            q = from + " AND " + to;
        }

        return q;
    }

    private String getPRSCriteria(PRSVraag prs, boolean first) throws IllegalArgumentException, UnsupportedOperationException {
        String vraag = null;
        Object value = null;
        boolean exact = true;

        if (prs.getANummer() != null) {
            vraag = prs.getANummer().getName().getLocalPart();
            value = prs.getANummer().getValue().getValue();
            exact = prs.getANummer().getValue().isExact();
        } else if (prs.getBsnNummer() != null) {
            vraag = prs.getBsnNummer().getName().getLocalPart();
            value = prs.getBsnNummer().getValue().getValue();
            exact = prs.getBsnNummer().getValue().isExact();
        } else if (prs.getGeboortedatum() != null) {
            vraag = prs.getGeboortedatum().getName().getLocalPart();
            value = prs.getGeboortedatum().getValue().getValue();
            exact = prs.getGeboortedatum().getValue().isExact();
        } else if (prs.getVoornamen() != null) {
            vraag = prs.getVoornamen().getName().getLocalPart();
            value = prs.getVoornamen().getValue().getValue();
            exact = prs.getVoornamen().getValue().isExact();
        } else if (prs.getVoorletters() != null) {
            vraag = prs.getVoorletters().getName().getLocalPart();
            value = prs.getVoorletters().getValue().getValue();
            exact = prs.getVoorletters().getValue().isExact();
        } else if (prs.getVoorvoegselGeslachtsnaam() != null) {
            vraag = prs.getVoorvoegselGeslachtsnaam().getName().getLocalPart();
            value = prs.getVoorvoegselGeslachtsnaam().getValue().getValue();
            exact = prs.getVoorvoegselGeslachtsnaam().getValue().isExact();
        } else if (prs.getGeslachtsnaam() != null) {
            vraag = prs.getGeslachtsnaam().getName().getLocalPart();
            value = prs.getGeslachtsnaam().getValue().getValue();
            exact = prs.getGeslachtsnaam().getValue().isExact();
        } else if (prs.getGeboorteplaats() != null) {
            vraag = prs.getGeboorteplaats().getName().getLocalPart();
            value = prs.getGeboorteplaats().getValue().getValue();
            exact = prs.getGeboorteplaats().getValue().isExact();
        } else if (prs.getCodeGeboorteland() != null) {
            vraag = prs.getCodeGeboorteland().getName().getLocalPart();
            value = prs.getCodeGeboorteland().getValue().getValue();
            exact = prs.getCodeGeboorteland().getValue().isExact();
        } else if (prs.getGeslachtsaanduiding() != null) {
            vraag = prs.getGeslachtsaanduiding().getName().getLocalPart();
            value = prs.getGeslachtsaanduiding().getValue().getValue();
            exact = prs.getGeslachtsaanduiding().getValue().isExact();
        } else if (prs.getDatumOverlijden() != null) {
            vraag = prs.getDatumOverlijden().getName().getLocalPart();
            value = prs.getDatumOverlijden().getValue().getValue();
            exact = prs.getDatumOverlijden().getValue().isExact();
        } else if (prs.getPlaatsOverlijden() != null) {
            vraag = prs.getPlaatsOverlijden().getName().getLocalPart();
            value = prs.getPlaatsOverlijden().getValue().getValue();
            exact = prs.getPlaatsOverlijden().getValue().isExact();
        } else if (prs.getCodeLandOverlijden() != null) {
            vraag = prs.getCodeLandOverlijden().getName().getLocalPart();
            value = prs.getCodeLandOverlijden().getValue().getValue();
            exact = prs.getCodeLandOverlijden().getValue().isExact();
        } else if (prs.getIndicatieGeheim() != null) {
            vraag = prs.getIndicatieGeheim().getName().getLocalPart();
            value = prs.getIndicatieGeheim().getValue().getValue();
            exact = prs.getIndicatieGeheim().getValue().isExact();
        } else if (prs.getAdellijkeTitelPredikaat() != null) {
            vraag = prs.getAdellijkeTitelPredikaat().getName().getLocalPart();
            value = prs.getAdellijkeTitelPredikaat().getValue().getValue();
            exact = prs.getAdellijkeTitelPredikaat().getValue().isExact();
        } else if (prs.getCodeGemeenteVanInschrijving() != null) {
            vraag = prs.getCodeGemeenteVanInschrijving().getName().getLocalPart();
            value = prs.getCodeGemeenteVanInschrijving().getValue().getValue();
            exact = prs.getCodeGemeenteVanInschrijving().getValue().isExact();
        } else if (prs.getDatumInschrijvingGemeente() != null) {
            vraag = prs.getDatumInschrijvingGemeente().getName().getLocalPart();
            value = prs.getDatumInschrijvingGemeente().getValue().getValue();
            exact = prs.getDatumInschrijvingGemeente().getValue().isExact();
        } else if (prs.getDatumOpschortingBijhouding() != null) {
            vraag = prs.getDatumOpschortingBijhouding().getName().getLocalPart();
            value = prs.getDatumOpschortingBijhouding().getValue().getValue();
            exact = prs.getDatumOpschortingBijhouding().getValue().isExact();
        } else if (prs.getOmschrijvingRedenOpschortingBijhouding() != null) {
            vraag = prs.getOmschrijvingRedenOpschortingBijhouding().getName().getLocalPart();
            value = prs.getOmschrijvingRedenOpschortingBijhouding().getValue().getValue();
            exact = prs.getOmschrijvingRedenOpschortingBijhouding().getValue().isExact();
        } else if (prs.getCodeLandEmigratie() != null) {
            vraag = prs.getCodeLandEmigratie().getName().getLocalPart();
            value = prs.getCodeLandEmigratie().getValue().getValue();
            exact = prs.getCodeLandEmigratie().getValue().isExact();
        } else if (prs.getDatumVertrekUitNederland() != null) {
            vraag = prs.getDatumVertrekUitNederland().getName().getLocalPart();
            value = prs.getDatumVertrekUitNederland().getValue().getValue();
            exact = prs.getDatumVertrekUitNederland().getValue().isExact();
        } else if (prs.getCodeLandImmigratie() != null) {
            vraag = prs.getCodeLandImmigratie().getName().getLocalPart();
            value = prs.getCodeLandImmigratie().getValue().getValue();
            exact = prs.getCodeLandImmigratie().getValue().isExact();
        } else if (prs.getDatumVestigingInNederland() != null) {
            vraag = prs.getDatumVestigingInNederland().getName().getLocalPart();
            value = prs.getDatumVestigingInNederland().getValue().getValue();
            exact = prs.getDatumVestigingInNederland().getValue().isExact();
        } else if (prs.getIndicatieGezagMinderjarige() != null) {
            vraag = prs.getIndicatieGezagMinderjarige().getName().getLocalPart();
            value = prs.getIndicatieGezagMinderjarige().getValue().getValue();
            exact = prs.getIndicatieGezagMinderjarige().getValue().isExact();
        } else if (prs.getIndicatieCuratelestelling() != null) {
            vraag = prs.getIndicatieCuratelestelling().getName().getLocalPart();
            value = prs.getIndicatieCuratelestelling().getValue().getValue();
            exact = prs.getIndicatieCuratelestelling().getValue().isExact();
        } else if (prs.getVerblijfstitel() != null) {
            vraag = prs.getVerblijfstitel().getName().getLocalPart();
            value = prs.getVerblijfstitel().getValue().getValue();
            exact = prs.getVerblijfstitel().getValue().isExact();
        } else if (prs.getDatumVerkrijgingVerblijfstitel() != null) {
            vraag = prs.getDatumVerkrijgingVerblijfstitel().getName().getLocalPart();
            value = prs.getDatumVerkrijgingVerblijfstitel().getValue().getValue();
            exact = prs.getDatumVerkrijgingVerblijfstitel().getValue().isExact();
        } else if (prs.getDatumVerliesVerblijfstitel() != null) {
            vraag = prs.getDatumVerliesVerblijfstitel().getName().getLocalPart();
            value = prs.getDatumVerliesVerblijfstitel().getValue().getValue();
            exact = prs.getDatumVerliesVerblijfstitel().getValue().isExact();
        } else if (prs.getAanduidingBijzonderNederlanderschap() != null) {
            vraag = prs.getAanduidingBijzonderNederlanderschap().getName().getLocalPart();
            value = prs.getAanduidingBijzonderNederlanderschap().getValue().getValue();
            exact = prs.getAanduidingBijzonderNederlanderschap().getValue().isExact();
        } else if (prs.getBurgerlijkeStaat() != null) {
            vraag = prs.getBurgerlijkeStaat().getName().getLocalPart();
            value = prs.getBurgerlijkeStaat().getValue().getValue();
            exact = prs.getBurgerlijkeStaat().getValue().isExact();
        } else if (prs.getAanduidingNaamgebruik() != null) {
            vraag = prs.getAanduidingNaamgebruik().getName().getLocalPart();
            value = prs.getAanduidingNaamgebruik().getValue().getValue();
            exact = prs.getAanduidingNaamgebruik().getValue().isExact();
        }

        /*
        if (prs.getPRSADRCOR() != null) {
            vraag = prs.getPRSADRCOR().getName().getLocalPart();
            value = prs.getPRSADRCOR().getValue().getValue();
        }
        if (prs.getPRSADRINS() != null) {
            vraag = prs.getPRSADRINS().getName().getLocalPart();
            value = prs.getPRSADRINS().getValue().getValue();
        }
        if (prs.getPRSADRVBL() != null) {
            vraag = prs.getPRSADRVBL().getName().getLocalPart();
            value = prs.getPRSADRVBL().getValue().getValue();
        }
        if (prs.getPRSIDB() != null) {
            vraag = prs.getPRSIDB().getName().getLocalPart();
            value = prs.getPRSIDB().getValue().getValue();
        }
        if (prs.getPRSNAT() != null) {
            vraag = prs.getPRSNAT().getName().getLocalPart();
            value = prs.getPRSNAT().getValue().getValue();
        }
        if (prs.getPRSPRSHUW() != null) {
            vraag = prs.getPRSPRSHUW().getName().getLocalPart();
            value = prs.getPRSPRSHUW().getValue().getValue();
        }
        if (prs.getPRSPRSKND() != null) {
            vraag = prs.getPRSPRSKND().getName().getLocalPart();
            value = prs.getPRSPRSKND().getValue().getValue();
        }
        if (prs.getPRSPRSOUD() != null) {
            vraag = prs.getPRSPRSOUD().getName().getLocalPart();
            value = prs.getPRSPRSOUD().getValue().getValue();
        }
        if (prs.getBankgiroRekeningnummer() != null) {
            vraag = prs.getBankgiroRekeningnummer().getName().getLocalPart();
            value = prs.getBankgiroRekeningnummer().getValue().getValue();
        }
        if (prs.getSubjectnrAKR() != null) {
            vraag = prs.getSubjectnrAKR().getName().getLocalPart();
            value = prs.getSubjectnrAKR().getValue().getValue();
        }
        if (prs.getEMailAdres() != null) {
            vraag = prs.getEMailAdres().getName().getLocalPart();
            value = prs.getEMailAdres().getValue().getValue();
        }
        if (prs.getTelefoonnummer() != null) {
            vraag = prs.getTelefoonnummer().getName().getLocalPart();
            value = prs.getTelefoonnummer().getValue().getValue();
        }
        if (prs.getFaxnummer() != null) {
            vraag = prs.getFaxnummer().getName().getLocalPart();
            value = prs.getFaxnummer().getValue().getValue();
        }
        if (prs.getTijdvakGeldigheid() != null) {
            vraag = prs.getTijdvakGeldigheid().getName().getLocalPart();
            value = prs.getTijdvakGeldigheid().getValue().getValue();
        }
        if (prs.getExtraElementen() != null) {
            vraag = prs.getExtraElementen().getName().getLocalPart();
            value = prs.getExtraElementen().getValue().getValue();
        }
        if (prs.getAcademischeTitel() != null) {
            vraag = prs.getAcademischeTitel().getName().getLocalPart();
            value = prs.getAcademischeTitel().getValue().getValue();
        }*/
        String column = vraagToColumn.get(vraag);
        if (vraag == null) {
            return null;
        }
        if (column == null) {
            throw new IllegalArgumentException("Request filtercolumn not supported: " + vraag);
        }

        String operator = null;
        String quote = "";
        String wildcard = "";
        boolean valueIsString = value instanceof String;
        if (valueIsString) {
            quote = "\'";
        }
        if(!exact && !valueIsString){
            throw new UnsupportedOperationException("Inexacte datums niet ondersteund");
        }
        if (exact) {
            operator = first ? " > " : " <= ";
        }else{
            wildcard = "%";
            operator = " LIKE ";
        }
        String valuePart = quote + wildcard + String.valueOf(value) + wildcard + quote;
        String query = column + operator + valuePart;
        return query;
    }
}
