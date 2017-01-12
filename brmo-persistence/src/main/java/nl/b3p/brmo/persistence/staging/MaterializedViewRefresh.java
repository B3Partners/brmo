/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import javax.persistence.Entity;

/**
 * Configuratie voor m-view refresh.
 *
 * @author mprins
 */
@Entity
public class MaterializedViewRefresh extends AutomatischProces {

    /**
     * de sleutel {@value MVIEW}.
     */
    public static final String MVIEW = "mview";

    public String getMView() {
        return ClobElement.nullSafeGet(this.getConfig().get(MVIEW));
    }
}
