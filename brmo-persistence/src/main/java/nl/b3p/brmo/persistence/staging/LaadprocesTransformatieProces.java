/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.persistence.Entity;

/**
 *
 * @author mprins
 */
@Entity
public class LaadprocesTransformatieProces extends AutomatischProces {

    public String getSoort() {
        return ClobElement.nullSafeGet(this.getConfig().get("laadprocessoort"));
    }

    public boolean alsStandTransformeren() {
        String alsStand = ClobElement.nullSafeGet(this.getConfig().get("versneldtransformeren"));
        return "true".equals(alsStand);
    }

    /**
     * soorten laadproces waar we iets mee kunnen.
     */
    public enum LaadprocesSoorten {
        // zie ook: nl.b3p.brmo.loader.BrmoFramework#BR_BGTLIGHT
        BR_BGTLIGHT("bgtlight"),
        BR_TOPNL("topnl");

        private static final ArrayList<String> soorten = new ArrayList();

        static {
            for (LaadprocesSoorten s : values()) {
                soorten.add(s.getSoort());
            }
        }
        private final String soort;

        LaadprocesSoorten(String soort) {
            this.soort = soort;
        }

        public String getSoort() {
            return soort;
        }

        public static List<String> soorten() {
            return Collections.unmodifiableList(soorten);
        }
    }
}
