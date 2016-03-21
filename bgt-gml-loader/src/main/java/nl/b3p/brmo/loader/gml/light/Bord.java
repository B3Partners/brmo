/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 *
 * @author mprins
 */
public class Bord extends GMLLightFeatureTransformerImpl {

    public Bord() {
        attrMapping.put("bgt-type", "bgt_type");
        attrMapping.put("plus-type", "plus_type");

        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
        attrMapping.put("lod0Geometrie", "lod0geometrie");
    }
}
