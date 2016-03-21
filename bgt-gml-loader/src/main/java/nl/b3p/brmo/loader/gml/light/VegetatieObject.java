/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 *
 * @author mprins
 */
public class VegetatieObject extends GMLLightFeatureTransformerImpl {

    public VegetatieObject() {
        attrMapping.put("bgt-type", "bgt_status");
        attrMapping.put("plus-type", "plus_type");

        attrMapping.put("lod0Geometry", "lod0geometry");
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);

    }
}
