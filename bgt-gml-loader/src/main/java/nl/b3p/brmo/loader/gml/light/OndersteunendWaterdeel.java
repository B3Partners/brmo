/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * GML Light transformer voor RSGB 3.0 objecttype ONDERSTEUNEND WATERDEEL.
 *
 * @author mprins
 */
public class OndersteunendWaterdeel extends GMLLightFeatureTransformerImpl {

    public OndersteunendWaterdeel() {
        attrMapping.put("bgt-type", "bgt_type");
        attrMapping.put("plus-type", "plus_type");

        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
