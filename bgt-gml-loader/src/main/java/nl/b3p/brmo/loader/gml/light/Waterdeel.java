/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 *
 * @author mprins
 */
public class Waterdeel extends GMLLightFeatureTransformerImpl {

    public Waterdeel() {
        attrMapping.put("bgt-type", "bgt_type");
        attrMapping.put("plus-type", "plus_type");

        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
