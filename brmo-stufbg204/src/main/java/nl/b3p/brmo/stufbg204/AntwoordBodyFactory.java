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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import nl.egem.stuf.sector.bg._0204.ACDTabel;
import nl.egem.stuf.sector.bg._0204.ADLTabel;
import nl.egem.stuf.sector.bg._0204.ADRAntwoord;
import nl.egem.stuf.sector.bg._0204.ADRFund;
import nl.egem.stuf.sector.bg._0204.ADRRelFund;
import nl.egem.stuf.sector.bg._0204.ANummerE;
import nl.egem.stuf.sector.bg._0204.AcademischeTitelPositieTovNaam;
import nl.egem.stuf.sector.bg._0204.AdellijkeTitelPredikaatE;
import nl.egem.stuf.sector.bg._0204.AdellijkeTitelSoort;
import nl.egem.stuf.sector.bg._0204.BsnNummerE;
import nl.egem.stuf.sector.bg._0204.CodeGeboortelandE;
import nl.egem.stuf.sector.bg._0204.GEMTabel;
import nl.egem.stuf.sector.bg._0204.GeboortedatumE;
import nl.egem.stuf.sector.bg._0204.GeboorteplaatsE;
import nl.egem.stuf.sector.bg._0204.Geslachtsaanduiding;
import nl.egem.stuf.sector.bg._0204.GeslachtsaanduidingE;
import nl.egem.stuf.sector.bg._0204.GeslachtsnaamE;
import nl.egem.stuf.sector.bg._0204.LNDTabel;
import nl.egem.stuf.sector.bg._0204.NATTabel;
import nl.egem.stuf.sector.bg._0204.ObjectFactory;
import nl.egem.stuf.sector.bg._0204.PRSAntwoord;
import nl.egem.stuf.sector.bg._0204.PRSFund.AanduidingNaamgebruik;
import nl.egem.stuf.sector.bg._0204.PRSFund.AcademischeTitel;
import nl.egem.stuf.sector.bg._0204.PRSFund.BurgerlijkeStaat;
import nl.egem.stuf.sector.bg._0204.PRSFund.CodeGemeenteVanInschrijving;
import nl.egem.stuf.sector.bg._0204.PRSFund.CodeLandOverlijden;
import nl.egem.stuf.sector.bg._0204.PRSFund.DatumVerkrijgingVerblijfstitel;
import nl.egem.stuf.sector.bg._0204.PRSFund.DatumVerliesVerblijfstitel;
import nl.egem.stuf.sector.bg._0204.PRSFund.OmschrijvingRedenOpschortingBijhouding;
import nl.egem.stuf.sector.bg._0204.PRSFund.PlaatsOverlijden;
import nl.egem.stuf.sector.bg._0204.PRSVraag;
import nl.egem.stuf.sector.bg._0204.SIBTabel;
import nl.egem.stuf.sector.bg._0204.VBTTabel;
import nl.egem.stuf.sector.bg._0204.VoorlettersE;
import nl.egem.stuf.sector.bg._0204.VoornamenE;
import nl.egem.stuf.sector.bg._0204.VoorvoegselGeslachtsnaamE;
import nl.egem.stuf.stuf0204.DatumMetIndicator;
import nl.egem.stuf.stuf0204.ExtraElement;
import nl.egem.stuf.stuf0204.ExtraElementen;
import nl.egem.stuf.stuf0204.NoValue;
import nl.egem.stuf.stuf0204.Verwerkingssoort;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Meine Toonen
 */
public class AntwoordBodyFactory {

    private static final Log LOG = LogFactory.getLog(AntwoordBodyFactory.class);
    
    private static final ObjectFactory objFac = new ObjectFactory();

    public AntwoordBodyFactory() {

    }

    // <editor-fold defaultstate="collapsed" desc="Answermessage creators">
    public static VBTTabel createVerblijfstitel(String omschrijving, BigInteger code) {
        VBTTabel l = new VBTTabel();

        VBTTabel.Omschrijving o = new VBTTabel.Omschrijving();
        o.setValue(omschrijving);

        VBTTabel.Code c = new VBTTabel.Code();
        c.setValue(code);

        l.setOmschrijving(objFac.createVBTTabelOmschrijving(o));
        l.setCode(objFac.createVBTTabelCode(c));

        return l;
    }

    public static SIBTabel createSoortIdentiteitsbewijs(String omschrijving, BigInteger soort) {
        SIBTabel l = new SIBTabel();

        SIBTabel.Soort s = new SIBTabel.Soort();
        s.setValue(soort);

        SIBTabel.Omschrijving o = new SIBTabel.Omschrijving();
        o.setValue(omschrijving);

        l.setOmschrijving(objFac.createSIBTabelOmschrijving(o));
        l.setSoort(objFac.createSIBTabelSoort(s));
        l.setSoortEntiteit("SIB");
        return l;
    }

    public static PRSAntwoord createPersoon(Map<String, Object> values, PRSVraag prs) {
        BigDecimal bsnBD = (BigDecimal) values.get("bsn");
        BigInteger bsnInt = bsnBD.toBigInteger();

        PRSAntwoord p = new PRSAntwoord();

        BsnNummerE bsn = new BsnNummerE();
        bsn.setValue(bsnInt);
        calculateNoValue(bsn);
        
        p.setBsnNummer(objFac.createPRSFundBsnNummer(bsn));

        if (prs.getVoornamen() != null) {
            VoornamenE v = new VoornamenE();
            v.setGegevengroep("PRS1");
            v.setValue(nullIfEmpty(values.get("nm_voornamen")));
            p.setVoornamen(objFac.createPRSFundVoornamen(v));
            calculateNoValue(v);
        }
        if (prs.getGeslachtsnaam() != null) {
            GeslachtsnaamE g = new GeslachtsnaamE();
            g.setGegevengroep("PRS1");
            g.setValue(nullIfEmpty(values.get("nm_geslachtsnaam")));
            p.setGeslachtsnaam(objFac.createPRSFundGeslachtsnaam(g));
            calculateNoValue(g);
        }
        if (prs.getGeboortedatum() != null) {
            GeboortedatumE gd = new GeboortedatumE();
            gd.setValue(nullIfEmptyBD(values.get("gb_geboortedatum")));
            p.setGeboortedatum(objFac.createPRSFundGeboortedatum(gd));
            calculateNoValue(gd);
        }
        if (prs.getVoorletters() != null) {
            VoorlettersE vl = new VoorlettersE();
            vl.setGegevengroep("PRS1");
            vl.setValue(nullIfEmpty(values.get("na_voorletters_aanschrijving")));
            p.setVoorletters(objFac.createPRSFundVoorletters(vl));
            calculateNoValue(vl);
        }
        if (prs.getVoorvoegselGeslachtsnaam() != null) {
            VoorvoegselGeslachtsnaamE vvgn = new VoorvoegselGeslachtsnaamE();
            vvgn.setGegevengroep("PRS1");
            vvgn.setValue(nullIfEmpty(values.get("nm_voorvoegsel_geslachtsnaam")));
            p.setVoorvoegselGeslachtsnaam(objFac.createPRSFundVoorvoegselGeslachtsnaam(vvgn));
            calculateNoValue(vvgn);
        }
        if (prs.getAdellijkeTitelPredikaat() != null) {
            AdellijkeTitelPredikaatE a = new AdellijkeTitelPredikaatE();
            a.setValue(nullIfEmpty(values.get("nm_adellijke_titel_predikaat")));
            p.setAdellijkeTitelPredikaat(objFac.createPRSFundAdellijkeTitelPredikaat(a));
            calculateNoValue(a);
        }

        if (prs.getAcademischeTitel() != null && prs.getAcademischeTitel().size() > 0) {
            AcademischeTitel at = new AcademischeTitel();
            at.setValue(nullIfEmpty(values.get("fk_2acd_code")));
            p.getAcademischeTitel().add(at);
            calculateNoValue(at);
        }
        if (prs.getANummer() != null) {
            ANummerE aN = new ANummerE();
            aN.setValue(nullIfEmptyBI(values.get("a_nummer")));
            p.setANummer(objFac.createPRSFundANummer(aN));
            calculateNoValue(aN);
        }
        if (prs.getBurgerlijkeStaat() != null) {
            BurgerlijkeStaat bu = new BurgerlijkeStaat();
            bu.setValue(nullIfEmptyBI(values.get("burgerlijke_staat")));
            calculateNoValue(bu);
            p.setBurgerlijkeStaat(objFac.createPRSFundBurgerlijkeStaat(bu));
        }

        if (prs.getDatumInschrijvingGemeente() != null) {
            DatumMetIndicator da = new DatumMetIndicator();
            da.setValue(nullIfEmptyBD(values.get("datum_inschrijving_in_gemeente")));
            calculateNoValue(da);
            p.setDatumInschrijvingGemeente(objFac.createPRSFundDatumInschrijvingGemeente(da));
        }
        if (prs.getDatumVerkrijgingVerblijfstitel() != null) {
            DatumVerkrijgingVerblijfstitel dav = new DatumVerkrijgingVerblijfstitel();
            dav.setValue(nullIfEmptyBD(values.get("datum_verkr_nation")));
            calculateNoValue(dav);
            p.setDatumVerkrijgingVerblijfstitel(objFac.createPRSFundDatumVerkrijgingVerblijfstitel(dav));
        }
        if (prs.getDatumVerliesVerblijfstitel() != null) {
            DatumVerliesVerblijfstitel dvt = new DatumVerliesVerblijfstitel();
            dvt.setValue(nullIfEmptyBD(values.get("datum_verlies_nation")));
            calculateNoValue(dvt);
            p.setDatumVerliesVerblijfstitel(objFac.createPRSFundDatumVerliesVerblijfstitel(dvt));
        }
        if (prs.getDatumVertrekUitNederland() != null) {
            DatumMetIndicator da = new DatumMetIndicator();
            da.setValue(nullIfEmptyBD(values.get("datum_vertrek_uit_nederland")));
            calculateNoValue(da);
            p.setDatumVertrekUitNederland(objFac.createPRSFundDatumVertrekUitNederland(da));
        }
        if (prs.getDatumVestigingInNederland() != null) {
            DatumMetIndicator da = new DatumMetIndicator();
            da.setValue(nullIfEmptyBD(values.get("datum_vestg_in_nederland")));
            calculateNoValue(da);
            p.setDatumVestigingInNederland(objFac.createPRSFundDatumVestigingInNederland(da));
        }
        if (prs.getCodeGemeenteVanInschrijving() != null) {
            CodeGemeenteVanInschrijving co = new CodeGemeenteVanInschrijving();
            co.setValue(nullIfEmptyBI(values.get("gemeente_van_inschrijving")));
            calculateNoValue(co);
            p.setCodeGemeenteVanInschrijving(objFac.createPRSFundCodeGemeenteVanInschrijving(co));
        }
        if (prs.getGeboorteplaats() != null) {
            GeboorteplaatsE gp = new GeboorteplaatsE();
            gp.setValue(nullIfEmpty(values.get("gb_geboorteplaats")));
            calculateNoValue(gp);
            p.setGeboorteplaats(objFac.createPRSFundGeboorteplaats(gp));
        }
        if (prs.getCodeGeboorteland() != null) {
            CodeGeboortelandE cg = new CodeGeboortelandE();
            cg.setValue(nullIfEmptyBI(values.get("fk_gb_lnd_code_iso")));
            calculateNoValue(cg);
            p.setCodeGeboorteland(objFac.createPRSFundCodeGeboorteland(cg));
        }
        if (prs.getDatumOverlijden() != null) {
            DatumMetIndicator da = new DatumMetIndicator();
            da.setValue(nullIfEmptyBD(values.get("ol_overlijdensdatum")));
            calculateNoValue(da);
            p.setDatumOverlijden(objFac.createPRSFundDatumOverlijden(da));
        }
        if (prs.getPlaatsOverlijden() != null) {
            PlaatsOverlijden pl = new PlaatsOverlijden();
            pl.setValue(nullIfEmpty(values.get("fk_gb_lnd_code_iso")));
            calculateNoValue(pl);
            p.setPlaatsOverlijden(objFac.createPRSFundPlaatsOverlijden(pl));
        }
        if (prs.getCodeLandOverlijden() != null) {
            CodeLandOverlijden clo = new CodeLandOverlijden();
            clo.setValue(nullIfEmptyBI(values.get("fk_ol_lnd_code_iso")));
            calculateNoValue(clo);
            p.setCodeLandOverlijden(objFac.createPRSFundCodeLandOverlijden(clo));
        }
        if (prs.getDatumOpschortingBijhouding() != null) {
            DatumMetIndicator da = new DatumMetIndicator();
            da.setValue(nullIfEmptyBD(values.get("datum_opschorting_bijhouding")));
            calculateNoValue(da);
            p.setDatumOpschortingBijhouding(objFac.createPRSFundDatumOpschortingBijhouding(da));
        }
        if (prs.getOmschrijvingRedenOpschortingBijhouding() != null) {
            OmschrijvingRedenOpschortingBijhouding om = new OmschrijvingRedenOpschortingBijhouding();
            om.setValue(nullIfEmpty(values.get("reden_opschorting_bijhouding")));
            calculateNoValue(om);
            p.setOmschrijvingRedenOpschortingBijhouding(objFac.createPRSFundOmschrijvingRedenOpschortingBijhouding(om));
        }

        if (prs.getAanduidingNaamgebruik() != null && values.get("aand_naamgebruik") != null) {
            AanduidingNaamgebruik an = new AanduidingNaamgebruik();
            an.setValue(nl.egem.stuf.sector.bg._0204.AanduidingNaamgebruik.fromValue(nullIfEmpty(values.get("aand_naamgebruik"))));
            calculateNoValue(an);
            p.setAanduidingNaamgebruik(objFac.createPRSFundAanduidingNaamgebruik(an));
        }

        if (prs.getGeslachtsaanduiding() != null && values.get("geslachtsaand") != null) {
            GeslachtsaanduidingE ga = new GeslachtsaanduidingE();
            Geslachtsaanduiding ge = Geslachtsaanduiding.fromValue(nullIfEmpty(values.get("geslachtsaand")));
            ga.setValue(ge);
            calculateNoValue(ga);
            p.setGeslachtsaanduiding(objFac.createPRSFundGeslachtsaanduiding(ga));
        }

        if (prs.getPRSADRCOR() != null) {
            List<ADRRelFund> adrcor = p.getPRSADRCOR();
            ADRRelFund a = createAdres("teststraat", new BigInteger("16"), "d", "1212", new BigInteger("0556"), prs.getPRSADRCOR().getValue().getADR());
            a.setSoortEntiteit("R");
            a.setVerwerkingssoort(Verwerkingssoort.I);
            adrcor.add(a);
        }

        if (prs.getPRSADRVBL() != null) {
            List<ADRRelFund> adrcor = p.getPRSADRVBL();
            ADRRelFund a = createAdres("teststraat", new BigInteger("16"), "d", "1212", new BigInteger("0556"), prs.getPRSADRVBL().getValue().getADR());
            adrcor.add(a);
        }

        if (prs.getPRSNAT() != null) {
            NATTabel n = createNationaliteit("NL", new BigInteger("42"));
            PRSAntwoord.PRSNAT nat = new PRSAntwoord.PRSNAT();
            nat.setNAT(n);
            nat.setVerwerkingssoort(Verwerkingssoort.I);
            nat.setSoortEntiteit("R");
            p.getPRSNAT().add(nat);
        }

        return p;
    }
    
    private static void calculateNoValue(Object element){
        Class c = element.getClass();
        Object val = null;
        try {
            Method method = element.getClass().getDeclaredMethod("getValue");
            val = method.invoke(element);
        } catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            LOG.error("Cannot get value for element " + c.toString(), e);
        }
        if (val == null) {
            try {
                Method method = element.getClass().getDeclaredMethod("setNoValue", NoValue.class);
                val = method.invoke(element, NoValue.GEEN_WAARDE);
            } catch (SecurityException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                LOG.error("Cannot set NoValue for element " + c.toString(), e);
            }
        }
    }

    public static NATTabel createNationaliteit(String nationaliteit, BigInteger code) {
        NATTabel l = new NATTabel();
        l.setVerwerkingssoort(Verwerkingssoort.I);
        l.setSoortEntiteit("T");
        l.setVerwerkingssoort(Verwerkingssoort.I);
        NATTabel.Omschrijving o = new NATTabel.Omschrijving();
        o.setValue(nationaliteit);

        NATTabel.Code c = new NATTabel.Code();
        c.setValue(code);

        l.setOmschrijving(objFac.createNATTabelOmschrijving(o));
        l.setCode(objFac.createNATTabelCode(c));
        l.setSoortEntiteit("NAT");
        return l;
    }

    public static LNDTabel createLand(String naam, BigInteger code) {
        LNDTabel l = new LNDTabel();

        LNDTabel.Landnaam n = new LNDTabel.Landnaam();
        n.setValue(naam);

        LNDTabel.Landcode c = new LNDTabel.Landcode();
        c.setValue(code);

        l.setLandnaam(objFac.createLNDTabelLandnaam(n));
        l.setLandcode(objFac.createLNDTabelLandcode(c));
        l.setSoortEntiteit("LND");
        return l;
    }

    public static GEMTabel createGemeente(String naam, BigInteger code) {
        GEMTabel g = new GEMTabel();
        GEMTabel.Gemeentenaam n = new GEMTabel.Gemeentenaam();
        n.setValue(naam);

        GEMTabel.Gemeentecode c = new GEMTabel.Gemeentecode();
        c.setValue(code);

        g.setGemeentenaam(objFac.createGEMTabelGemeentenaam(n));
        g.setGemeentecode(objFac.createGEMTabelGemeentecode(c));

        g.setSoortEntiteit("GEM");
        return g;
    }

    public static ACDTabel createAcademischeTitel(String titel, boolean posVoorNaam, String code) {
        ACDTabel t = new ACDTabel();
        ACDTabel.Code c = new ACDTabel.Code();
        c.setExact(true);
        c.setKerngegeven(true);
        c.setNoValue(NoValue.GEEN_WAARDE);
        c.setValue(code);

        ACDTabel.Omschrijving o = new ACDTabel.Omschrijving();
        o.setValue(titel);
        o.setExact(Boolean.FALSE);

        ACDTabel.PositieTovNaam p = new ACDTabel.PositieTovNaam();
        p.setValue(posVoorNaam ? AcademischeTitelPositieTovNaam.V : AcademischeTitelPositieTovNaam.N);

        ExtraElementen e = new ExtraElementen();
        ExtraElement ex = new ExtraElement();
        ex.setNaam("ex1");
        ex.setValue("val1");
        e.getExtraElement().add(ex);

        t.setNoValue(NoValue.NIET_ONDERSTEUND);
        t.setSoortEntiteit("ACD");
        t.setCode(objFac.createACDTabelCode(c));
        t.setOmschrijving(objFac.createACDTabelOmschrijving(o));
        t.setVerwerkingssoort(Verwerkingssoort.V);
        t.setPositieTovNaam(objFac.createACDTabelPositieTovNaam(p));
        t.setExtraElementen(e);

        return t;
    }

    public static ADLTabel createAdelijkeTitel(String titel) {
        ADLTabel t = new ADLTabel();
        ADLTabel.Soort s = new ADLTabel.Soort();
        s.setValue(AdellijkeTitelSoort.A);

        ADLTabel.Omschrijving o = new ADLTabel.Omschrijving();
        o.setValue(titel);
        o.setExact(Boolean.FALSE);

        t.setNoValue(NoValue.GEEN_WAARDE);
        t.setOmschrijving(objFac.createADLTabelOmschrijving(o));
        t.setSoort(objFac.createADLTabelSoort(s));
        t.setVerwerkingssoort(Verwerkingssoort.R);
        t.setSoortEntiteit("ADL");
        return t;
    }

    public static ADRRelFund createAdres(String straat, BigInteger huisnummer, String huisletter, String postcode, BigInteger gemeentecode, ADRFund vraag) {
        ADRRelFund aa = new ADRRelFund();

        ADRAntwoord a = new ADRAntwoord();
        a.setSoortEntiteit("F");
        a.setVerwerkingssoort(Verwerkingssoort.I);

        ADRFund.Straatnaam s = new ADRFund.Straatnaam();
        s.setValue(straat);
        s.setGegevengroep("ADR1");
        calculateNoValue(s);

        ADRFund.Huisnummer h = new ADRFund.Huisnummer();
        h.setValue(huisnummer);
        h.setGegevengroep("ADR1");
        calculateNoValue(h);
        
        ADRFund.Gemeentecode g = new ADRFund.Gemeentecode();
        g.setValue(gemeentecode);
        calculateNoValue(g);
        
        ADRFund.Huisletter hl = new ADRFund.Huisletter();
        hl.setValue(huisletter);
        hl.setGegevengroep("ADR1");
        calculateNoValue(hl);
        
        ADRFund.Postcode p = new ADRFund.Postcode();
        p.setValue(postcode);
        p.setGegevengroep("ADR1");
        calculateNoValue(p);
        
        ADRFund.Woonplaatsnaam w = new ADRFund.Woonplaatsnaam();
        w.setValue("Maassluis");
        calculateNoValue(w);

        if (vraag.getStraatnaam() != null) {
            a.setStraatnaam(objFac.createADRFundStraatnaam(s));
        }
        if (vraag.getHuisnummer() != null) {
            a.setHuisnummer(objFac.createADRFundHuisnummer(h));
        }
        if (vraag.getHuisletter() != null) {
            a.setHuisletter(objFac.createADRFundHuisletter(hl));
        }
        if (vraag.getGemeentecode() != null) {
            a.setGemeentecode(objFac.createADRFundGemeentecode(g));
        }
        if (vraag.getPostcode() != null) {
            a.setPostcode(objFac.createADRFundPostcode(p));
        }
        if (vraag.getWoonplaatsnaam() != null) {
            a.setWoonplaatsnaam(objFac.createADRFundWoonplaatsnaam(w));
        }
        if (vraag.getAdresBuitenland1() != null) {
            ADRFund.AdresBuitenland1 ab = new ADRFund.AdresBuitenland1();
            ab.setGegevengroep("ADR4");
            a.setAdresBuitenland1(objFac.createADRFundAdresBuitenland1(ab));
            calculateNoValue(ab);
        }
        if (vraag.getAdresBuitenland2() != null) {
            ADRFund.AdresBuitenland2 ab = new ADRFund.AdresBuitenland2();
            ab.setGegevengroep("ADR4");
            a.setAdresBuitenland2(objFac.createADRFundAdresBuitenland2(ab));
            calculateNoValue(ab);
        }
        if (vraag.getAdresBuitenland3() != null) {
            ADRFund.AdresBuitenland3 ab = new ADRFund.AdresBuitenland3();
            ab.setGegevengroep("ADR4");
            a.setAdresBuitenland3(objFac.createADRFundAdresBuitenland3(ab));
            calculateNoValue(ab);
        }
        if (vraag.getLandcode() != null) {
            ADRFund.Landcode lc = new ADRFund.Landcode();
            calculateNoValue(lc);
            a.setLandcode(objFac.createADRFundLandcode(lc));
        }
        if (vraag.getPostbusnummer() != null) {
            ADRFund.Postbusnummer pn = new ADRFund.Postbusnummer();
            calculateNoValue(pn);
            a.setPostbusnummer(objFac.createADRFundPostbusnummer(pn));
        }
        if (vraag.getAntwoordnummer() != null) {
            ADRFund.Antwoordnummer an = new ADRFund.Antwoordnummer();
            calculateNoValue(an);
            a.setAntwoordnummer(objFac.createADRFundAntwoordnummer(an));
        }
        if (vraag.getHuisnummertoevoeging() != null) {
            ADRFund.Huisnummertoevoeging ht = new ADRFund.Huisnummertoevoeging();
            calculateNoValue(ht);
            a.setHuisnummertoevoeging(objFac.createADRFundHuisnummertoevoeging(ht));
        }
        if (vraag.getAanduidingBijHuisnummer() != null) {
            ADRFund.AanduidingBijHuisnummer ah = new ADRFund.AanduidingBijHuisnummer();
            calculateNoValue(ah);
            a.setAanduidingBijHuisnummer(objFac.createADRFundAanduidingBijHuisnummer(ah));
        }

        aa.setADR(a);
        return aa;
    }

    public static String nullIfEmpty(Object e) {
        return e == null ? null : e.toString();
    }

    public static BigDecimal nullIfEmptyBD(Object e) {
        return e == null ? null : new BigDecimal(e.toString());
    }

    public static BigInteger nullIfEmptyBI(Object e) {
        return e == null ? null : new BigInteger(e.toString());
    }

    // </editor-fold>
}
