/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * BGT GML Light tranformer.
 *
 * @author mprins
 */
public interface GMLLightFeatureTransformer {

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
     * @return de getransformeerde feature van het nieuwe type
     */
    SimpleFeature transform(SimpleFeature inFeature, SimpleFeatureType targetType, boolean shouldUppercase);
}
