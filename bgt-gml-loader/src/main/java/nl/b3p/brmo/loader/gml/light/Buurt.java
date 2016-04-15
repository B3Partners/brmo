/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * GML Light transformer voor RSGB 3.0 objecttype BUURT.
 *
 * @author mprins
 */
public class Buurt extends GMLLightFeatureTransformerImpl {

    public Buurt() {
        attrMapping.put("buurtcode", "buurtcode");
        attrMapping.put("naam", "naam");

        // TODO evt. relatie/constraint in datamodel aanbrengen
        // BUURT [1 .. *] ligt in WIJK [1]
        // wijk (gml:ReferenceType) blijkt niet voor te komen in de GML
        attrMapping.put("wijk", "wijk");

        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);

        // komen niet voor in de RSGB 3.0 beschrijving, op null zetten/niet transformeren
        attrMapping.put("bgt-status", null);
        attrMapping.put("plus-status", null);
        attrMapping.put("relatieveHoogteligging", null);
    }
}
