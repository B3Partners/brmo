/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.Entity;

/**
 *
 * @author mprins
 */
@Entity
public class BerichtTransformatieProces extends AutomatischProces {

    private static final String BLOCK_ON_MISSINGNUMBERSFOUND = "blokkeer_transformatie";

    public void setBlockOnMissingNumbers(boolean block) {
        this.getConfig().put(BLOCK_ON_MISSINGNUMBERSFOUND, new ClobElement(Boolean.toString(block)));
    }

    public boolean getBlockOnMissingNumbers() {
        String s = ClobElement.nullSafeGet(this.getConfig().get(BLOCK_ON_MISSINGNUMBERSFOUND));
        return (s == null ? false : Boolean.parseBoolean(s));
    }
}
