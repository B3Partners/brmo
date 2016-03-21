/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 *
 * @author mprins
 */
public class OnbegroeidTerreindeel extends GMLLightFeatureTransformerImpl {

    public OnbegroeidTerreindeel() {
        attrMapping.put("bgt-fysiekVoorkomen", "bgt_fysiekvoorkomen");
        attrMapping.put("opTalud", "optalud");
        attrMapping.put("plus-fysiekVoorkomen", "plus_fysiekvoorkomen");

        attrMapping.put("kruinlijn", "kruinlijn");
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
