/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204.util;

import nl.egem.stuf.stuf0204.FoutBericht;
import nl.egem.stuf.stuf0204.Stuurgegevens;

/**
 * Utility methodes voor StUF BG 204.
 *
 * @author mprins
 */
public final class StUFbg204Util {

    public static FoutBericht maakFout() {
        final FoutBericht fout = new FoutBericht();
        Stuurgegevens s = new Stuurgegevens();
        s.setBerichtsoort("Fo01");
        fout.setStuurgegevens(s);
        fout.setBody(new FoutBericht.Body());

        return fout;
    }

    private StUFbg204Util() {
    }
}
