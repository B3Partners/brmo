/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import java.util.Date;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * BGT GML Light tranformer.
 * 
 * @author mprins
 */
public interface GMLLightFeatureTransformer {

    /**
     * default geometrie kolom, {@value }.
     */
    public static final String DEFAULT_GEOM_NAME = "geom2d";
    /**
     * kruinlijn geometrie kolom, {@value }.
     */
    public static final String KRUINLIJN_GEOM_NAME = "kruinlijn";
    /**
     * {@value }.
     */
    public static final String LOD0_GEOM_NAME = "lod0geom";
    /**
     * {@value }.
     */
    public static final String LOD1_GEOM_NAME = "lod1geom";
    /**
     * {@value }.
     */
    public static final String LOD2_GEOM_NAME = "lod2geom";
    /**
     * {@value }.
     */
    public static final String LOD3_GEOM_NAME = "lod3geom";
    /**
     * gebruik {@value } voor nen3610id.
     */
    public static final String ID_NAME = "identif";

    /**
     * Naam voor laatst bijgewerkt veld {@value }.
     */
    public static final String BIJWERKDATUM_NAME = "bijwerkdatum";
    /**
     * Naam voor object begin tijd veld {@value }.
     */
    public static final String BEGINTIJD_NAME = "dat_beg_geldh";

    /**
     * Naam voor object eind tijd veld {@value }.
     */
    public static final String EINDTIJD_NAME = "datum_einde_geldh";
    /**
     * Maak nieuw (getransformeerd) featuretype.
     *
     * @param gmlSchema Simple GML / GML light feature type
     * @param targetTableName naam van het nieuwe type
     * @param shouldUppercase als de naam van het schema in uppercase moet
     * (Oracle)
     * @return het nieuwe type
     */
    SimpleFeatureType getTargetSchema(SimpleFeatureType gmlSchema, String targetTableName, boolean shouldUppercase);

    /**
     * Transformeer een feature.
     *
     * @param inFeature input (te transformeren) feature
     * @param targetType doel type voor transformatie
     * @param shouldUppercase als de naam van de attributen in uppercase moet
     * (Oracle)
     * @param userDefinedPrimaryKey {@code true} als de identifier uit de bron
     * wordt geconstrueerd en in bestaand schema wordt geladen, {@code false} in
     * geval geotools gegenereerd schema met {@code fid} kolom
     * @param bijwerkDatum metadata, de datum waarop een object is bijgewerkt
     *
     * @return de getransformeerde feature van het nieuwe type of null
     */
    SimpleFeature transform(SimpleFeature inFeature, SimpleFeatureType targetType, boolean shouldUppercase, boolean userDefinedPrimaryKey, Date bijwerkDatum);
}
