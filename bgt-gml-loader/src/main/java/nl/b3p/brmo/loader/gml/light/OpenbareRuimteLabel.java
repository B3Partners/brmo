/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 *
 * @author mprins
 */
public class OpenbareRuimteLabel extends GMLLightFeatureTransformerImpl {

    public OpenbareRuimteLabel() {
        attrMapping.put("identificatieBAGOPR", "identif_bagopr");
        attrMapping.put("openbareRuimteNaam.tekst", "openbareruimtenaam_tekst");
        attrMapping.put("openbareRuimteNaam.positie_1.plaatsingspunt", "openbareruimtenaam_positie_1_plaatsingspunt");
        attrMapping.put("openbareRuimteNaam.positie_1.hoek", "openbareruimtenaam_positie_1_hoek");
        attrMapping.put("openbareRuimteNaam.positie_2.plaatsingspunt", "openbareruimtenaam_positie_2_plaatsingspunt");
        attrMapping.put("openbareRuimteNaam.positie_2.hoek", "openbareruimtenaam_positie_2_hoek");
        attrMapping.put("openbareRuimteNaam.positie_3.plaatsingspunt", "openbareruimtenaam_positie_3_plaatsingspunt");
        attrMapping.put("openbareRuimteNaam.positie_3.hoek", "openbareruimtenaam_positie_3_hoek");
        attrMapping.put("openbareRuimteType", "openbareruimtetype");

    }
}
