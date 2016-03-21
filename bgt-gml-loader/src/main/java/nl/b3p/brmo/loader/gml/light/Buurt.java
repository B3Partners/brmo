/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 *
 * @author mprins
 */
public class Buurt extends GMLLightFeatureTransformerImpl {

    public Buurt() {
        attrMapping.put("naam", "naam");
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
        attrMapping.put("buurtcode", "buurtcode");
        // wijk (gml:ReferenceType) blijkt niet voor te komen in de GML
        attrMapping.put("wijk", "wijk");
    }
}
