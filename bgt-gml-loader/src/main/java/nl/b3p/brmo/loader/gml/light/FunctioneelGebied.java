/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 *
 * @author mprins
 */
public class FunctioneelGebied extends GMLLightFeatureTransformerImpl {

    public FunctioneelGebied() {
        attrMapping.put("bgt-type", "bgt_type");
        attrMapping.put("plus-type", "plus_type");
        attrMapping.put("naam", "naam");
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
