/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.gml.light;

import java.util.Date;
import nl.b3p.brmo.loader.gml.GMLLightFeatureTransformer;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.geotools.referencing.CRS;
import org.geotools.util.SimpleInternationalString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.FeatureTypeFactory;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Default implementatie van GML Light transformer.
 *
 * @author mprins
 */
class GMLLightFeatureTransformerImpl implements GMLLightFeatureTransformer {

    private static final Log LOG = LogFactory.getLog(GMLLightFeatureTransformerImpl.class);

    /**
     * bevat paren van: 'gml attribuut naam', 'rsgb attribuut naam', deze
     * laatste mag {@code null} zijn, dan wordt het veld overgeslagen. 'plus'
     * velden komen uit IMGeo, 'bgt' velden komen uit BGT.
     */
    protected final HashMap<String, String> attrMapping = new HashMap();

    /**
     * bevat paren van: 'gml attribuut naam 1+gml attribuut naam 2', 'rsgb
     * attribuut' waarbij de rgsb attribuut waarde wordt samengesteld uit de
     * waarden van de gml attributen. bijv. de NEN3610ID.
     *
     * Niet alle velden uit de GML worden in de database gezet; overgeslagen oa:
     * <ul>
     * <li>inOnderzoek</li>
     * <li>tijdstipRegistratie</li>
     * <li>eindRegistratie</li>
     * <li>LV-publicatiedatum</li>
     * <li>bronhouder</li>
     * </ul>
     * (IMgeo / BGT attributen) en (GML 3 attributen)
     * <ul>
     * <li>name</li>
     * <li>description</li>
     * <li>boundedBy</li>
     * <li>LV-publicatiedatum</li>
     * </ul>
     */
    protected final HashMap<String, AttributeDescriptor> composedAttr = new HashMap();

    // fail, see: https://osgeo-org.atlassian.net/browse/GEOT-5400
    private final FeatureTypeFactory typeFactory = CommonFactoryFinder.getFeatureTypeFactory(null);
    // gebruik:
    //private final FeatureTypeFactory typeFactory = new FeatureTypeFactoryImpl();
    private final AttributeTypeBuilder builder = new AttributeTypeBuilder();
    private CoordinateReferenceSystem crs = null;

    /**
     * default constructor. Zorgt voor initiele vulling van de
     * {@link #composedAttr} en {@link #attrMapping} transformatie mappings.
     */
    public GMLLightFeatureTransformerImpl() {
        /* onderstaande GML 3 attributen overslaan/niet transformeren */
        attrMapping.put("name", null);
        attrMapping.put("description", null);
        attrMapping.put("boundedBy", null);
        attrMapping.put("metaDataProperty", null);
        /* bovenstaande GML 3 attributen overslaan/niet transformeren */

 /* onderstaande niet in RSBG 3.0 */
        // attrMapping.put("inOnderzoek", "inonderzoek");
        attrMapping.put("inOnderzoek", null);
        // attrMapping.put("tijdstipRegistratie", "tijdstip_registratie");
        attrMapping.put("tijdstipRegistratie", null);
        // attrMapping.put("eindRegistratie", "eind_registratie");
        attrMapping.put("eindRegistratie", null);
        // attrMapping.put("LV-publicatiedatum", "lv_publicatiedatum");
        attrMapping.put("LV-publicatiedatum", null);
        // attrMapping.put("bronhouder", "bronhouder");
        attrMapping.put("bronhouder", null);
        /* bovenstaande niet in RSBG 3.0 */

        // (gedeelde) model attributen
        attrMapping.put("objectBeginTijd", BEGINTIJD_NAME);
        attrMapping.put("objectEindTijd", EINDTIJD_NAME);
        attrMapping.put("bgt-status", "bgt_status");
        attrMapping.put("plus-status", "plus_status");
        attrMapping.put("relatieveHoogteligging", "relve_hoogteligging");

        // NEN 3610 ID velden worden samengevoegd in 'identif' zie hieronder
        //   attrMapping.put("identificatie.namespace", "identificatie_namespace");
        //   attrMapping.put("identificatie.lokaalID", "identificatie_lokaalid");
        builder.setFactory(typeFactory);
        builder.name(ID_NAME)
                .binding(String.class)
                .identifiable(true)
                .abstrct(false)
                .minOccurs(1)
                .maxOccurs(1)
                .nillable(false)
                .description("NEN3610 indentificatie");
        AttributeDescriptor NEN3610ID = builder.buildDescriptor(ID_NAME);
        composedAttr.put("identificatie.namespace+identificatie.lokaalID", NEN3610ID);
        try {
            crs = CRS.decode("EPSG:28992");
        } catch (FactoryException ex) {
            LOG.warn("CRS opzoeken is mislukt", ex);
        }
    }

    @Override
    public SimpleFeatureType getTargetSchema(SimpleFeatureType gmlSchema, String targetTableName, boolean shouldUppercase) {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        if (shouldUppercase) {
            tb.setName(targetTableName.toUpperCase());
        } else {
            tb.setName(targetTableName);
        }

        tb.setCRS(crs);
        tb.setSRS("EPSG:28992");
        tb.setNamespaceURI((String) null);
        tb.setDescription(new SimpleInternationalString("RSGB 3.0 BGT " + gmlSchema.getTypeName()));

        AttributeTypeBuilder bldr = new AttributeTypeBuilder();
        for (AttributeDescriptor attr : composedAttr.values()) {
            if (shouldUppercase) {
                tb.srid(28992).add(
                        bldr.init(attr.getType())
                        .name(attr.getLocalName().toUpperCase())
                        .defaultValue(attr.getDefaultValue())
                        .buildDescriptor(attr.getLocalName().toUpperCase()));
            } else {
                tb.srid(28992).add(attr);
            }
        }

        String gmlAttrLocalName;
        String dbAttrName;
        for (AttributeDescriptor att : gmlSchema.getAttributeDescriptors()) {
            gmlAttrLocalName = att.getLocalName();
            if (gmlAttrLocalName == null) {
                LOG.warn("'Null' local name van attribuut " + att);
                gmlAttrLocalName = att.toString();
            }
            dbAttrName = attrMapping.get(gmlAttrLocalName);

            LOG.debug("aanmaken AttributeDescriptor voor: " + gmlAttrLocalName + " (wordt: " + dbAttrName + ")");
            if (dbAttrName != null) {
                // hernoem attribuut als de db naam niet gelijk is aan gml naam
                if (shouldUppercase) {
                    dbAttrName = dbAttrName.toUpperCase();
                }
                if (dbAttrName.equals(gmlAttrLocalName)) {
                    tb.srid(28992).add(att);
                } else {
                    tb.srid(28992).add(
                            bldr.init(att.getType())
                            .name(dbAttrName)
                            .defaultValue(att.getDefaultValue())
                            .buildDescriptor(dbAttrName));
                }
                if (dbAttrName.equals(DEFAULT_GEOM_NAME)) {
                    tb.setDefaultGeometry(DEFAULT_GEOM_NAME);
                }
                if (dbAttrName.equals(DEFAULT_GEOM_NAME.toUpperCase())) {
                    tb.setDefaultGeometry(DEFAULT_GEOM_NAME.toUpperCase());
                }
            }
        }

        // extra database velden / brmo metadata
        String datum = shouldUppercase ? BIJWERKDATUM_NAME.toUpperCase() : BIJWERKDATUM_NAME;
        tb.add(datum, Date.class);
        return tb.crs(crs).buildFeatureType();
    }

    @Override
    public SimpleFeature transform(SimpleFeature inFeature, SimpleFeatureType targetType, boolean shouldUppercaseFieldnames, boolean userDefinedPrimaryKey, Date bijwerkDatum) {
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(targetType);
        String targetAttrName;
        for (String key : attrMapping.keySet()) {
            targetAttrName = attrMapping.get(key);
            if (targetAttrName != null) {
                if (shouldUppercaseFieldnames) {
                    targetAttrName = targetAttrName.toUpperCase();
                }
                featureBuilder.set(targetAttrName, inFeature.getAttribute(key));
            }
        }

        String composed;
        String id = null;
        for (Entry<String, AttributeDescriptor> e : composedAttr.entrySet()) {
            targetAttrName = e.getValue().getLocalName();
            if (shouldUppercaseFieldnames) {
                targetAttrName = targetAttrName.toUpperCase();
            }
            // splits op '+' en voeg samen met ':'
            String[] keys = e.getKey().split("\\+");
            StringBuilder composition = new StringBuilder();
            for (String key : keys) {
                composition.append(inFeature.getAttribute(key)).append(":");
            }
            composed = composition.substring(0, composition.length() - 1);
            featureBuilder.set(targetAttrName, composed);
            if (targetAttrName.equalsIgnoreCase(ID_NAME)) {
                id = composed;
            }
        }
        // brmo metadata
        String datum = shouldUppercaseFieldnames ? BIJWERKDATUM_NAME.toUpperCase() : BIJWERKDATUM_NAME;
        featureBuilder.set(datum, bijwerkDatum);

        featureBuilder.featureUserData(Hints.USE_PROVIDED_FID, userDefinedPrimaryKey);
        return featureBuilder.buildFeature(id);
    }
}
