package nl.b3p.brmo.loader.entity;


import static nl.b3p.brmo.loader.BrmoFramework.BR_WOZ;

public class WozBericht extends Bericht{

    public WozBericht(String brXml) {
        super(brXml);
        super.setSoort(BR_WOZ);
    }
}
