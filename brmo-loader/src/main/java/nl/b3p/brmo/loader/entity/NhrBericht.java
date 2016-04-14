package nl.b3p.brmo.loader.entity;

import static nl.b3p.brmo.loader.BrmoFramework.BR_NHR;

/**
 *
 * @author Matthijs Laan
 */
public class NhrBericht extends Bericht {

    public NhrBericht(String brXml) {
        super(brXml);
        setSoort(BR_NHR);
    }
}
