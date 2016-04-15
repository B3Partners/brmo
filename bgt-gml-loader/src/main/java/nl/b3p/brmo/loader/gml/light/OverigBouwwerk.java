/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * GML Light transformer voor RSGB 3.0 objecttype OVERIG BOUWWERK.
 *
 * @author mprins
 */
public class OverigBouwwerk extends GMLLightFeatureTransformerImpl {

    public OverigBouwwerk() {
        attrMapping.put("bgt-type", "bgt_type");
        attrMapping.put("plus-type", "plus_type");

        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
        // TODO verschillende LODx geometrieen (niet in simpel model, wel in doc...)
    }
}
