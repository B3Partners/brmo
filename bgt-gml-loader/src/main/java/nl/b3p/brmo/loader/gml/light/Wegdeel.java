/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
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

        attrMapping.put("kruinlijn", "kruinlijn");
        attrMapping.put("lod0Surface", "lod0surface");
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
