/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * GML Light transformer voor RSGB 3.0 objecttype BEGROEID TERREINDEEL.
 *
 * @author mprins
 */
public class BegroeidTerreindeel extends GMLLightFeatureTransformerImpl {

    public BegroeidTerreindeel() {
        attrMapping.put("opTalud", "optalud");
        attrMapping.put("bgt-fysiekVoorkomen", "bgt_fysiekvoorkomen");
        attrMapping.put("plus-fysiekVoorkomen", "plus_fysiekvoorkomen");

        attrMapping.put("lod0MultiSurface", "lod0multisurface");
        attrMapping.put("kruinlijn", KRUINLIJN_GEOM_NAME);
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
