/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 *
 * @author mprins
 */
public class Stadsdeel extends GMLLightFeatureTransformerImpl {

    public Stadsdeel() {
        attrMapping.put("naam", "naam");

        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);

    }
}
