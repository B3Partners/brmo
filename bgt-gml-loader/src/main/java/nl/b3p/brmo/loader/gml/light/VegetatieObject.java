/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * GML Light transformer voor RSGB 3.0 objecttype VEGETATIEOBJECT.
 *
 * @author mprins
 */
public class VegetatieObject extends GMLLightFeatureTransformerImpl {

    public VegetatieObject() {
        attrMapping.put("bgt-type", "bgt_type");
        attrMapping.put("plus-type", "plus_type");

        attrMapping.put("lod0Geometry", LOD0_GEOM_NAME);
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
