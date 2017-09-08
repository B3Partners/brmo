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

import java.math.BigInteger;
import nl.egem.stuf.sector.bg._0204.ACDTabel;
import nl.egem.stuf.sector.bg._0204.ADLTabel;
import nl.egem.stuf.sector.bg._0204.ADRAntwoord;
import nl.egem.stuf.sector.bg._0204.ADRFund;
import nl.egem.stuf.sector.bg._0204.AcademischeTitelPositieTovNaam;
import nl.egem.stuf.sector.bg._0204.AdellijkeTitelSoort;
import nl.egem.stuf.sector.bg._0204.BsnNummerE;
import nl.egem.stuf.sector.bg._0204.GEMTabel;
import nl.egem.stuf.sector.bg._0204.GeslachtsnaamE;
import nl.egem.stuf.sector.bg._0204.LNDTabel;
import nl.egem.stuf.sector.bg._0204.NATTabel;
import nl.egem.stuf.sector.bg._0204.ObjectFactory;
import nl.egem.stuf.sector.bg._0204.PRSAntwoord;
import nl.egem.stuf.sector.bg._0204.SIBTabel;
import nl.egem.stuf.sector.bg._0204.SynchroonAntwoordBericht.Body;
import nl.egem.stuf.sector.bg._0204.VBTTabel;
import nl.egem.stuf.sector.bg._0204.VoornamenE;
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

    private static final ObjectFactory objFac = new ObjectFactory();

    public AntwoordBodyFactory() {

    }

    public static Body getBody(VraagBericht bericht) {
        Body b = new Body();
        String entiteitsType = bericht.getStuurgegevens().getEntiteittype();
        
        switch (entiteitsType) {
            case "ACD": {
                ACDTabel t = createAcademischeTitel("Professor", true, "pietje");
                b.getACD().add(t);
                break;
            }
            case "ADL": {
                ADLTabel t = createAdelijkeTitel("Koning");
                b.getADL().add(t);
                break;
            }
            case "ADR": {
                ADRAntwoord a = createAdres("Zonnebaan", new BigInteger("12"), "c", "3542EC", new BigInteger("42"));
                b.getADR().add(a);
                break;
            }
            case "GEM": {
                GEMTabel g = createGemeente("Utrecht", new BigInteger("42"));
                b.getGEM().add(g);
                break;
            }
            case "LND": {
                LNDTabel l = createLand("Nederland", new BigInteger("42"));
                b.getLND().add(l);
                break;
            }
            case "NAT": {
                NATTabel l = createNationaliteit("Nederlandse", new BigInteger("42"));
                b.getNAT().add(l);
                break;
            }
            case "PRS": {
                PRSAntwoord p = createPersoon(new BigInteger("123456789"),"pietje", "puk");
                b.getPRS().add(p);
                break;
            }
            case "SIB": {
                SIBTabel s = createSoortIdentiteitsbewijs("Paspoort", BigInteger.ONE);
                b.getSIB().add(s);
                break;
            }
            case "VBT": {
                VBTTabel v = createVerblijfstitel("Inwonende", BigInteger.ONE);
                b.getVBT().add(v);
                break;
            }
            default:
                throw new IllegalArgumentException("Entiteitstype niet ondersteund: " + entiteitsType);
        }
        return b;
    }

    
    // <editor-fold defaultstate="collapsed" desc="Answermessage creators">
        
    public static VBTTabel createVerblijfstitel(String omschrijving, BigInteger code){
        VBTTabel l = new VBTTabel();
      
        VBTTabel.Omschrijving o = new VBTTabel.Omschrijving();
        o.setValue(omschrijving);
        
        VBTTabel.Code c = new VBTTabel.Code();
        c.setValue(code);
        
        l.setOmschrijving(objFac.createVBTTabelOmschrijving(o));
        l.setCode(objFac.createVBTTabelCode(c));
        
        return l;
    }
    
    public static SIBTabel createSoortIdentiteitsbewijs(String omschrijving, BigInteger soort){
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
    
    public static PRSAntwoord createPersoon(BigInteger bs, String voornamen, String geslachtsnaam){
        PRSAntwoord p = new PRSAntwoord();
        
        BsnNummerE bsn = new BsnNummerE();
        bsn.setValue(bs);
        
        VoornamenE v = new VoornamenE();
        v.setValue(voornamen);
        
        GeslachtsnaamE g = new GeslachtsnaamE();
        g.setValue(geslachtsnaam);
        
        p.setBsnNummer(objFac.createPRSFundBsnNummer(bsn));
        p.setVoornamen(objFac.createPRSFundVoornamen(v));
        p.setGeslachtsnaam(objFac.createPRSFundGeslachtsnaam(g));
        return p;
    }
    
    public static NATTabel createNationaliteit(String nationaliteit, BigInteger code){
        NATTabel l = new NATTabel();
      
        NATTabel.Omschrijving o = new NATTabel.Omschrijving();
        o.setValue(nationaliteit);
        
        NATTabel.Code c = new NATTabel.Code();
        c.setValue(code);
        
        l.setOmschrijving(objFac.createNATTabelOmschrijving(o));
        l.setCode(objFac.createNATTabelCode(c));        
        l.setSoortEntiteit("NAT");
        return l;
    }
    
    public static LNDTabel createLand(String naam, BigInteger code){
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

    public static ADRAntwoord createAdres(String straat, BigInteger huisnummer, String huisletter, String postcode, BigInteger gemeentecode) {
        ADRAntwoord a = new ADRAntwoord();

        ADRFund.Straatnaam s = new ADRFund.Straatnaam();
        s.setValue(straat);

        ADRFund.Huisnummer h = new ADRFund.Huisnummer();
        h.setValue(huisnummer);

        ADRFund.Gemeentecode g = new ADRFund.Gemeentecode();
        g.setValue(gemeentecode);

        ADRFund.Huisletter hl = new ADRFund.Huisletter();
        hl.setValue(huisletter);

        ADRFund.Postcode p = new ADRFund.Postcode();
        p.setValue(postcode);

        a.setStraatnaam(objFac.createADRFundStraatnaam(s));
        a.setHuisnummer(objFac.createADRFundHuisnummer(h));
        a.setHuisletter(objFac.createADRFundHuisletter(hl));
        a.setGemeentecode(objFac.createADRFundGemeentecode(g));
        a.setPostcode(objFac.createADRFundPostcode(p));
        return a;
    }

    // </editor-fold>
}
