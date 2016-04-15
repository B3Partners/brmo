/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * GML Light transformer voor RSGB 3.0 objecttype TUNNELDEEL.
 *
 * @author mprins
 */
public class Tunneldeel extends GMLLightFeatureTransformerImpl {

    public Tunneldeel() {

        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
        attrMapping.put("lod0Geometrie", LOD0_GEOM_NAME);
    }
}
