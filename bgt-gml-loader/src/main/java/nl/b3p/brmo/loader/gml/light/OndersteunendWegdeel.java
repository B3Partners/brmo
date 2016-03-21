/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 *
 * @author mprins
 */
public class OndersteunendWegdeel extends GMLLightFeatureTransformerImpl {

    public OndersteunendWegdeel() {
        attrMapping.put("opTalud", "optalud");
        attrMapping.put("bgt-functie", "bgtfunctie");

        attrMapping.put("bgt-fysiekVoorkomen", "bgt_fysiekvoorkomen");
        attrMapping.put("plus-functie", "plus_functie");
        attrMapping.put("plus-fysiekVoorkomen", "plus_fysiekvoorkomen");

        attrMapping.put("kruinlijn", "kruinlijn");
        attrMapping.put("lod0Surface", "lod0surface");
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
