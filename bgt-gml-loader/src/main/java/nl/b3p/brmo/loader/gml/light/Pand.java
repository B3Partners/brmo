/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * GML Light transformer voor RSGB 3.0 objecttype PAND. Dit type wordt geladen
 * als {@link nl.b3p.brmo.loader.gml.BGTGMLLightTransformerFactory#bgt_pand}.
 *
  * @author mprins
 */
public class Pand extends GMLLightFeatureTransformerImpl {

    public Pand() {
        attrMapping.put("identificatieBAGPND", "identif_bagpnd");

        attrMapping.put("nummeraanduidingreeks_1.tekst", "nrar_1_tekst");
        attrMapping.put("nummeraanduidingreeks_1.positie_1.plaatsingspunt", "nrar_1_pos_1_pnt");
        attrMapping.put("nummeraanduidingreeks_1.positie_1.hoek", "nrar_1_pos_1_hoek");
        attrMapping.put("nummeraanduidingreeks_1.positie_2.plaatsingspunt", "nrar_1_pos_2_pnt");
        attrMapping.put("nummeraanduidingreeks_1.positie_2.hoek", "nrar_1_pos_2_hoek");
        attrMapping.put("nummeraanduidingreeks_1.positie_3.plaatsingspunt", "nrar_1_pos_3_pnt");
        attrMapping.put("nummeraanduidingreeks_1.positie_3.hoek", "nrar_1_pos_3_hoek");
        attrMapping.put("nummeraanduidingreeks_1.identificatieBAGVBOLaagsteHuisnummer", "nrar_1_id_bagvbo_min_huisnr");
        attrMapping.put("nummeraanduidingreeks_1.identificatieBAGVBOHoogsteHuisnummer", "nrar_1_id_bagvbo_max_huisnr");

        attrMapping.put("nummeraanduidingreeks_2.tekst", "nrar_2_tekst");
        attrMapping.put("nummeraanduidingreeks_2.positie_1.plaatsingspunt", "nrar_2_pos_1_pnt");
        attrMapping.put("nummeraanduidingreeks_2.positie_1.hoek", "nrar_2_pos_1_hoek");
        attrMapping.put("nummeraanduidingreeks_2.positie_2.plaatsingspunt", "nrar_2_pos_2_pnt");
        attrMapping.put("nummeraanduidingreeks_2.positie_2.hoek", "nrar_2_pos_2_hoek");
        attrMapping.put("nummeraanduidingreeks_2.positie_3.plaatsingspunt", "nrar_2_pos_3_pnt");
        attrMapping.put("nummeraanduidingreeks_2.positie_3.hoek", "nrar_2_pos_3_hoek");
        attrMapping.put("nummeraanduidingreeks_2.identificatieBAGVBOLaagsteHuisnummer", "nrar_2_id_bagvbo_min_huisnr");
        attrMapping.put("nummeraanduidingreeks_2.identificatieBAGVBOHoogsteHuisnummer", "nrar_2_id_bagvbo_max_huisnr");

        attrMapping.put("nummeraanduidingreeks_3.tekst", "nrar_3_tekst");
        attrMapping.put("nummeraanduidingreeks_3.positie_1.plaatsingspunt", "nrar_3_pos_1_pnt");
        attrMapping.put("nummeraanduidingreeks_3.positie_1.hoek", "nrar_3_pos_1_hoek");
        attrMapping.put("nummeraanduidingreeks_3.positie_2.plaatsingspunt", "nrar_3_pos_2_pnt");
        attrMapping.put("nummeraanduidingreeks_3.positie_2.hoek", "nrar_3_pos_2_hoek");
        attrMapping.put("nummeraanduidingreeks_3.positie_3.plaatsingspunt", "nrar_3_pos_3_pnt");
        attrMapping.put("nummeraanduidingreeks_3.positie_3.hoek", "nrar_3_pos_3_hoek");
        attrMapping.put("nummeraanduidingreeks_3.identificatieBAGVBOLaagsteHuisnummer", "nrar_3_id_bagvbo_min_huisnr");
        attrMapping.put("nummeraanduidingreeks_3.identificatieBAGVBOHoogsteHuisnummer", "nrar_3_id_bagvbo_max_huisnr");

        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
