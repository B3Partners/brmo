/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * Een ruimtelijk object al dan niet ter detaillering dan wel ter inrichting van
 * de overige benoemde ruimtelijke objecten of een ander inrichtingselement.
 *
 *
 * @author mprins
 */
public class Inrichtingselement extends GMLLightFeatureTransformerImpl {

    public Inrichtingselement() {
        attrMapping.put("bgt-type", "bgt_type");
        // vreemd: geen geomtrie
    }
}
