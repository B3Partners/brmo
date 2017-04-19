/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Houdt de lijst met beschkibare GML transformers bij. Er zijn een aantal
 * bestanden die niet geconverteerd worden, oa. bestuurlijke eenheden.
 *
 * @author mprins
 */
public enum BGTGMLLightTransformerFactory {
    // NB (mogelijk) geen geometrie,
    // TODO nog geen voorbeeld of bestand gezien...
    // zie: https://github.com/B3Partners/brmo/issues/324
    inrichtingselement("", "Inrichtingselement"),
    // doen we niet/nooit
    // plaatsbepalingspunt("", ""),
    
    bak("bgt_bak.gml", "Bak"),
    bord("bgt_bord.gml", "Bord"),
    put("bgt_put.gml", "Put"),
    paal("bgt_paal.gml", "Paal"),
    mast("bgt_mast.gml", "Mast"),
    sensor("bgt_sensor.gml", "Sensor"),
    installatie("bgt_installatie.gml", "Installatie"),
    kast("bgt_kast.gml", "Kast"),
    //
    gebouw_installatie("bgt_buildinginstallation.gml", "GebouwInstallatie"),
    pand("bgt_buildingpart.gml", "Pand"),
    overig_bouwwerk("bgt_overigbouwwerk.gml", "OverigBouwwerk"),
    //
    openbareruimte("bgt_openbareruimte.gml", "OpenbareRuimte"),
    openbareruimtelabel("bgt_openbareruimtelabel.gml", "OpenbareRuimteLabel"),
    functioneelgebied("bgt_functioneelgebied.gml", "FunctioneelGebied"),
    // TODO bestuurlijke eenheden, nog geen gml bestand van gezien
    // zie: https://github.com/B3Partners/brmo/issues/324
    // stadsdeel("", "Stadsdeel"),
    // waterschap("", "Waterschap"),
    buurt("bgt_buurt.gml", "Buurt"),
    wijk("bgt_wijk.gml", "Wijk"),
    //
    ongeclassificeerdobject("bgt_ongeclassificeerdobject.gml", "OngeclassificeerdObject"),
    //
    begroeid_terreindeel("bgt_begroeidterreindeel.gml", "BegroeidTerreindeel"),
    onbegroeid_terreindeel("bgt_onbegroeidterreindeel.gml", "OnbegroeidTerreindeel"),
    vegetatieobject("bgt_vegetatieobject.gml", "VegetatieObject"),
    //
    spoor("bgt_spoor.gml", "Spoor"),
    //
    scheiding("bgt_scheiding.gml", "Scheiding"),
    overige_scheiding("bgt_overigescheiding.gml", "OverigeScheiding"),
    //
    waterdeel("bgt_waterdeel.gml", "Waterdeel"),
    ondersteunend_waterdeel("bgt_ondersteunendwaterdeel.gml", "OndersteunendWaterdeel"),
    waterinrichtingselement("bgt_waterinrichtingselement.gml", "Waterinrichtingselement"),
    //
    overbruggingsdeel("bgt_overbruggingsdeel.gml", "Overbruggingsdeel"),
    kunstwerkdeel("bgt_kunstwerkdeel.gml", "Kunstwerkdeel"),
    //
    ondersteunend_wegdeel("bgt_ondersteunendwegdeel.gml", "OndersteunendWegdeel"),
    wegdeel("bgt_wegdeel.gml", "Wegdeel"),
    tunneldeel("bgt_tunneldeel.gml", "Tunneldeel"),
    straatmeubilair("bgt_straatmeubilair.gml", "Straatmeubilair"),
    weginrichtingselement("bgt_weginrichtingselement.gml", "Weginrichtingselement");

    /**
     * naam van het GML bestand.
     */
    private final String gmlFileName;
    /**
     * naam van de transformer klasse.
     */
    private final String transformerClassName;

    private static final Map<String, BGTGMLLightTransformerFactory> lookup = new HashMap<>();

    static {
        for (BGTGMLLightTransformerFactory d : BGTGMLLightTransformerFactory.values()) {
            //lookup.put(d.getGmlFileName(), d);
            lookup.put(d.getTransformerClassName(), d);
        }
    }

    private BGTGMLLightTransformerFactory(String gmlFileName, String transformerClassName) {
        this.gmlFileName = gmlFileName;
        this.transformerClassName = transformerClassName;
    }

    public String getGmlFileName() {
        return gmlFileName;
    }

    public String getTransformerClassName() {
        return transformerClassName;
    }

    /**
     * zoek de tabelnaam voor dit GML bestand.
     *
     * @param gmlFileName GML bestandsnaam
     * @return tabelnaam
     */
    public static String getTableName(String gmlFileName) {
        return lookup.get(gmlFileName).name();
    }

    /**
     * Zoek de juiste feature transformer op voor het (gml light) bestand.
     *
     * @param gmlFileName bestandsnaam
     * @return transformer of {@code null} als er geen transformer beschikbaar
     * danwel gevonden is
     */
    public static GMLLightFeatureTransformer getTransformer(String gmlFileName) {
        try {
            Class c = Class.forName(GMLLightFeatureTransformer.class.getPackage().getName() + ".light." + lookup.get(gmlFileName).getTransformerClassName());
            return (GMLLightFeatureTransformer) c.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NullPointerException ex) {
            return null;
        }
    }
}
