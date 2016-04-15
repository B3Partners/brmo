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

        attrMapping.put("openbareRuimteNaam.tekst", "oprnm_tekst");
        attrMapping.put("openbareRuimteNaam.positie_1.plaatsingspunt", "oprnm_pos_1_punt");
        attrMapping.put("openbareRuimteNaam.positie_1.hoek", "oprnm_pos_1_hoek");
        attrMapping.put("openbareRuimteNaam.positie_2.plaatsingspunt", "oprnm_pos_2_punt");
        attrMapping.put("openbareRuimteNaam.positie_2.hoek", "oprnm_pos_2_hoek");
        attrMapping.put("openbareRuimteNaam.positie_3.plaatsingspunt", "oprnm_pos_3_punt");
        attrMapping.put("openbareRuimteNaam.positie_3.hoek", "oprnm_pos_3_hoek");

        attrMapping.put("openbareRuimteType", "oprtype");
    }
}
