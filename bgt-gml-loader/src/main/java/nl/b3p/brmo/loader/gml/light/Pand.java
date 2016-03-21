/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 *
 * @author mprins
 */
public class Pand extends GMLLightFeatureTransformerImpl {

    public Pand() {
        attrMapping.put("identificatieBAGPND", "identif_bagpnd");

        attrMapping.put("nummeraanduidingreeks_1.tekst", "nummeraanduidingreeks_1_tekst");
        attrMapping.put("nummeraanduidingreeks_1.positie_1.plaatsingspunt", "nummeraanduidingreeks_1_positie_1_plaatsingspunt");
        attrMapping.put("nummeraanduidingreeks_1.positie_1.hoek", "nummeraanduidingreeks_1_positie_1_hoek");
        attrMapping.put("nummeraanduidingreeks_1.positie_2.plaatsingspunt", "nummeraanduidingreeks_1_positie_2_plaatsingspunt");
        attrMapping.put("nummeraanduidingreeks_1.positie_2.hoek", "nummeraanduidingreeks_1_positie_2_hoek");
        attrMapping.put("nummeraanduidingreeks_1.positie_3.plaatsingspunt", "nummeraanduidingreeks_1_positie_3_plaatsingspunt");
        attrMapping.put("nummeraanduidingreeks_1.positie_3.hoek", "nummeraanduidingreeks_1_positie_3_hoek");
        attrMapping.put("nummeraanduidingreeks_1.identificatieBAGVBOLaagsteHuisnummer", "nummeraanduidingreeks_1_identificatieBAGVBOLaagsteHuisnummer");
        attrMapping.put("nummeraanduidingreeks_1.identificatieBAGVBOHoogsteHuisnummer", "nummeraanduidingreeks_1_identificatieBAGVBOHoogsteHuisnummer");

        attrMapping.put("nummeraanduidingreeks_2.tekst", "nummeraanduidingreeks_2_tekst");
        attrMapping.put("nummeraanduidingreeks_2.positie_1.plaatsingspunt", "nummeraanduidingreeks_2_positie_1_plaatsingspunt");
        attrMapping.put("nummeraanduidingreeks_2.positie_1.hoek", "nummeraanduidingreeks_2_positie_1_hoek");
        attrMapping.put("nummeraanduidingreeks_2.positie_2.plaatsingspunt", "nummeraanduidingreeks_2_positie_2_plaatsingspunt");
        attrMapping.put("nummeraanduidingreeks_2.positie_2.hoek", "nummeraanduidingreeks_2_positie_2_hoek");
        attrMapping.put("nummeraanduidingreeks_2.positie_3.plaatsingspunt", "nummeraanduidingreeks_2_positie_3_plaatsingspunt");
        attrMapping.put("nummeraanduidingreeks_2.positie_3.hoek", "nummeraanduidingreeks_2_positie_3_hoek");
        attrMapping.put("nummeraanduidingreeks_2.identificatieBAGVBOLaagsteHuisnummer", "nummeraanduidingreeks_2_identificatieBAGVBOLaagsteHuisnummer");
        attrMapping.put("nummeraanduidingreeks_2.identificatieBAGVBOHoogsteHuisnummer", "nummeraanduidingreeks_2_identificatieBAGVBOHoogsteHuisnummer");

        attrMapping.put("nummeraanduidingreeks_3.tekst", "nummeraanduidingreeks_3_tekst");
        attrMapping.put("nummeraanduidingreeks_3.positie_1.plaatsingspunt", "nummeraanduidingreeks_3_positie_1_plaatsingspunt");
        attrMapping.put("nummeraanduidingreeks_3.positie_1.hoek", "nummeraanduidingreeks_3_positie_1_hoek");
        attrMapping.put("nummeraanduidingreeks_3.positie_2.plaatsingspunt", "nummeraanduidingreeks_3_positie_2_plaatsingspunt");
        attrMapping.put("nummeraanduidingreeks_3.positie_2.hoek", "nummeraanduidingreeks_3_positie_2_hoek");
        attrMapping.put("nummeraanduidingreeks_3.positie_3.plaatsingspunt", "nummeraanduidingreeks_3_positie_3_plaatsingspunt");
        attrMapping.put("nummeraanduidingreeks_3.positie_3.hoek", "nummeraanduidingreeks_3_positie_3_hoek");
        attrMapping.put("nummeraanduidingreeks_3.identificatieBAGVBOLaagsteHuisnummer", "nummeraanduidingreeks_3_identificatieBAGVBOLaagsteHuisnummer");
        attrMapping.put("nummeraanduidingreeks_3.identificatieBAGVBOHoogsteHuisnummer", "nummeraanduidingreeks_3_identificatieBAGVBOHoogsteHuisnummer");

        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
