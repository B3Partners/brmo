/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * GML Light transformer voor RSGB 3.0 objecttype WEGDEEL.
 *
 * @author mprins
 */
public class Wegdeel extends GMLLightFeatureTransformerImpl {

    public Wegdeel() {
        attrMapping.put("bgt-functie", "bgt_functie");
        attrMapping.put("plus-functie", "plus_functie");
        attrMapping.put("opTalud", "optalud");
        attrMapping.put("bgt-fysiekVoorkomen", "bgt_fysiekvoorkomen");
        attrMapping.put("plus-fysiekVoorkomen", "plus_fysiekvoorkomen");

        attrMapping.put("kruinlijn", KRUINLIJN_GEOM_NAME);
        attrMapping.put("lod0Surface", LOD0_GEOM_NAME);
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
