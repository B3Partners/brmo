/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * TODO Komt niet als zelfstanding objecttype voor in RSGB 3.0 doc.
 *
 * @author mprins
 */
public class OngeclassificeerdObject extends GMLLightFeatureTransformerImpl {

    public OngeclassificeerdObject() {
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
