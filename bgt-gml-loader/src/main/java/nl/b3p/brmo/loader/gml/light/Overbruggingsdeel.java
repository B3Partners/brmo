/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * GML Light transformer voor RSGB 3.0 objecttype OVERBRUGGINGSDEEL.
 *
 * @author mprins
 */
public class Overbruggingsdeel extends GMLLightFeatureTransformerImpl {

    public Overbruggingsdeel() {
        attrMapping.put("typeOverbruggingsdeel", "otype");
        attrMapping.put("hoortBijTypeOverbrugging", "hoortbijtype");
        attrMapping.put("overbruggingIsBeweegbaar", "isbeweegbaar");

        attrMapping.put("lod0Geometrie", LOD0_GEOM_NAME);
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
