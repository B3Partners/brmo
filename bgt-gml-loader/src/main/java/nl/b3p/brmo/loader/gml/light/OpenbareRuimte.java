/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

/**
 * BGT OPR, niet BAG OPR, wordt geladen in
 * {@link nl.b3p.brmo.loader.gml.BGTGMLLightTransformerFactory#bgt_openbareruimte}
 *
 * @author mprins
 */
public class OpenbareRuimte extends GMLLightFeatureTransformerImpl {

    public OpenbareRuimte() {
        attrMapping.put("naam", "naam");

        // naamEnIdOpenbareRuimte (gml:ReferenceType) blijkt niet voor te komen in de GML
        attrMapping.put("naamEnIdOpenbareRuimte", "naam_id_opr");

        attrMapping.put("geometrie2d", DEFAULT_GEOM_NAME);
    }
}
