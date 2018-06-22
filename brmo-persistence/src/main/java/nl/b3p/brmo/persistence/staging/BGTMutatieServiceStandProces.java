package nl.b3p.brmo.persistence.staging;

import javax.persistence.Entity;
import java.util.Collections;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Proces configuratie voor de BGT mutatie service pilot (stand).
 *
 * @author mprins
 */
@Entity
public class BGTMutatieServiceStandProces extends AutomatischProces {

    private static final String DELTA = "deltaId";
    // plaatsbepalingspunt
    private static final String EXCLUDED_TYPES = "excludetypes";
    // bgtv3
    private static final String DATASET =     "dataset";
    // tiles={"layers":[{"aggregateLevel":0,"codes":[52764]}]}
    // of {"layers":[{"aggregateLevel":0,"codes":[52764]}]}
    private static final String FILTER =     "geographischFilter";
    // citygml
    private static final String FORMAT =     "format";
    private static final String URL =     "url";

// https://downloads.pdok.nl/service/extract.zip?extractname=bgt&extractset=citygml&excludedtypes=plaatsbepalingspunt&history=true&tiles=%7B%22layers%22%3A%5B%7B%22aggregateLevel%22%3A0%2C%22codes%22%3A%5B52764%5D%7D%5D%7D

    public String getDelta() {
        return ClobElement.nullSafeGet(this.getConfig().get(DELTA));
    }

    public void setDelta(String delta) {
        if (delta == null) {
            this.getConfig().put(DELTA, null);
        } else {
            this.getConfig().put(DELTA, new ClobElement(delta));
        }
    }

    /**
     *
     * @return "https://test.downloads.pdok.nl/api/"
     */
    public String getOphaalUrl(){
                return ClobElement.nullSafeGet(this.getConfig().get(URL));
        }

    /**
     *
     * @return "citygml"
     */
    public String getFormat(){        return ClobElement.nullSafeGet(this.getConfig().get(FORMAT));    }

    /**
     *
     * @return "bgtv3"
     */
    public String getDataset(){        return ClobElement.nullSafeGet(this.getConfig().get(DATASET));    }

    /**
     *
     * @return "plaatsbepalingspunt"
     */
    public String getExludedTypes() {
        return ClobElement.nullSafeGet(this.getConfig().get(EXCLUDED_TYPES));
    }

    public void setExludedTypes(String typesCSV) {
        if (typesCSV == null) {
            this.getConfig().put(EXCLUDED_TYPES, null);
        } else {
            this.getConfig().put(EXCLUDED_TYPES, new ClobElement(typesCSV));
        }
    }

    /**
     *
     * @return "{\"layers\":[{\"aggregateLevel\":0,\"codes\":[52764]}]}"
     */
    public String getGeoFilter() {        return ClobElement.nullSafeGet(this.getConfig().get(FILTER));    }

}
