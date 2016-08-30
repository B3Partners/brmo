/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import java.util.Collections;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javax.persistence.Entity;

/**
 * Proces configuratie voor de BGT light downloader.
 *
 * @author mprins
 */
@Entity
public class BGTLightOphaalProces extends AutomatischProces {

    private static final String ARCHIEF_DIRECTORY = "archiefdirectory";

    /**
     * Zoekt de geconfigureerde archief directory op.
     *
     * @return de naam van de directory voor deze configuratie of null
     */
    public String getArchiefDirectory() {
        return ClobElement.nullSafeGet(this.getConfig().get(ARCHIEF_DIRECTORY));
    }

    /**
     * Stelt de te gebruiken archief directory in.
     *
     * @param archiefDirectory te gebruiken archief directory
     */
    public void setArchiefDirectory(String archiefDirectory) {
        if (archiefDirectory == null) {
            this.getConfig().put(ARCHIEF_DIRECTORY, null);
        } else {
            this.getConfig().put(ARCHIEF_DIRECTORY, new ClobElement(archiefDirectory));
        }
    }

    public String getTileInfoUrl() {
        return ClobElement.nullSafeGet(this.getConfig().get("geojsonurl"));
    }

    public String getOphaalgebied() {
        return ClobElement.nullSafeGet(this.getConfig().get("ophaalgebied"));
    }

    public String getOphaalUrl() {
        return ClobElement.nullSafeGet(this.getConfig().get("ophaalurl"));
    }

    public Set<Integer> getGridIds() {
        TreeSet<Integer> ids = new TreeSet();
        String cvsId = ClobElement.nullSafeGet(this.getConfig().get("gridids"));
        if (cvsId != null) {
            StringTokenizer st = new StringTokenizer(cvsId, ",;", false);
            while (st.hasMoreTokens()) {
                try {
                    ids.add(Integer.parseInt(st.nextToken().trim()));
                } catch (NumberFormatException ignore) {
                    // LOG.debug("Niet parsable integer.", ignore);
                }
            }
        }
        return Collections.unmodifiableSet(ids);
    }
}
