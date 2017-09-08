/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import nl.egem.stuf.stuf0204.FoutBericht;
import nl.egem.stuf.stuf0204.Stuurgegevens;
import nl.egem.stuf.stuf0204.Systeem;

/**
 * Utility methodes voor StUF BG 204.
 *
 * @author mprins
 */
public final class StUFbg204Util {
    
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkkmmssSSS");

    public static FoutBericht maakFout() {
        final FoutBericht fout = new FoutBericht();
        Stuurgegevens s = new Stuurgegevens();
        s.setBerichtsoort("Fo01");
        fout.setStuurgegevens(s);
        fout.setBody(new FoutBericht.Body());

        return fout;
    }
    
    public static Stuurgegevens maakStuurgegevens(Stuurgegevens vraagStuurgegevens){
        final Stuurgegevens sg = vraagStuurgegevens;
        Systeem s = new Systeem();
        s.setApplicatie("BRMO");
        s.setOrganisatie("B3Partners B.V.");
        sg.setZender(s);
        sg.setVersieStUF("0204");
        sg.setTijdstipBericht(sdf.format(new Date()));
        
        return sg;
    }

    private StUFbg204Util() {
    }
}
