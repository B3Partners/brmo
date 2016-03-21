/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 *
 * @author mprins
 */
public class Wijk extends GMLLightFeatureTransformerImpl {

    public Wijk() {
        attrMapping.put("naam", "naam");
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
        attrMapping.put("wijkcode", "wijkcode");
        // stadsdeel (gml:ReferenceType) blijkt niet voor te komen in de GML
        attrMapping.put("stadsdeel", "stadsdeel");
    }

}
