/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 *
 * @author mprins
 */
public class Overbruggingsdeel extends GMLLightFeatureTransformerImpl {

    public Overbruggingsdeel() {
        attrMapping.put("typeOverbruggingsdeel", "typeoverbruggingsdeel");
        attrMapping.put("hoortBijTypeOverbrugging", "hoortbijtypeoverbrugging");
        attrMapping.put("overbruggingIsBeweegbaar", "overbruggingisbeweegbaar");

        attrMapping.put("lod0Geometrie", "lod0geometrie");
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);

    }
}
