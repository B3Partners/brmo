/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * GML Light transformer voor RSGB 3.0 objecttype SPOOR.
 *
 * @author mprins
 */
public class Spoor extends GMLLightFeatureTransformerImpl {

    public Spoor() {
        attrMapping.put("bgt-functie", "bgt_functie");
        attrMapping.put("plus-functie", "plus_functie");

        attrMapping.put("lod0Curve", LOD0_GEOM_NAME);
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
