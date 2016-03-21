/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 *
 * @author mprins
 */
public class BegroeidTerreindeel extends GMLLightFeatureTransformerImpl {

    public BegroeidTerreindeel() {
        attrMapping.put("opTalud", "optalud");
        attrMapping.put("bgt-fysiekVoorkomen", "bgt_fysiekvoorkomen");
        attrMapping.put("plus-fysiekVoorkomen", "plus_fysiekvoorkomen");

        attrMapping.put("lod0MultiSurface", "lod0multisurface");
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
        attrMapping.put("kruinlijn", "kruinlijn");
    }

}
