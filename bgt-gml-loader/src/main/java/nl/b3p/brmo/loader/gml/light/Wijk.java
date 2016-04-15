/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * GML Light transformer voor RSGB 3.0 objecttype WIJK.
 *
 * @author mprins
 */
public class Wijk extends GMLLightFeatureTransformerImpl {

    public Wijk() {
        attrMapping.put("wijkcode", "wijkcode");
        attrMapping.put("naam", "naam");
        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);

        // TODO evt. relatie/constraint in datamodel aanbrengen
        // WIJK [1 .. *] ligt in GEMEENTE [1]
        // stadsdeel (gml:ReferenceType) blijkt niet voor te komen in de GML
        // stadsdeel komt niet voor in de beschrijving
        attrMapping.put("stadsdeel", "stadsdeel");

        // komen niet voor in de RSGB 3.0 beschrijving, op null zetten/niet transformeren
        attrMapping.put("bgt-status", null);
        attrMapping.put("plus-status", null);
        attrMapping.put("relatieveHoogteligging", null);
    }
}
