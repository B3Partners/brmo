/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.schema;

import static nl.b3p.brmo.schema.mapping.SimpleDateFormatAttributeColumnMapping.PATTERN_XML_DATE;
import static nl.b3p.brmo.schema.mapping.SimpleDateFormatAttributeColumnMapping.PATTERN_XML_TIMESTAMP;

import nl.b3p.brmo.schema.Schema;
import nl.b3p.brmo.schema.mapping.AttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.BooleanAttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.DoubleAttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.GeometryAttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.IntegerAttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.OneToManyColumnMapping;
import nl.b3p.brmo.schema.mapping.SimpleDateFormatAttributeColumnMapping;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BGTSchema extends Schema {
    private static BGTSchema instance;

    public static final String EIND_REGISTRATIE = "eindRegistratie";

    static final Set<String> bgtObjectTypes =
            new HashSet<>(
                    Arrays.asList(
                            "TrafficArea",
                            "AuxiliaryTrafficArea",
                            "Railway",
                            "OnbegroeidTerreindeel",
                            "PlantCover",
                            "Waterdeel",
                            "OndersteunendWaterdeel",
                            "BuildingPart",
                            "nummeraanduidingreeks", // BuildingPart one-to-many
                            // "OverigeConstructie", // Wel in gegevenscatalogus, niet in LV
                            "OverigBouwwerk",
                            "BridgeConstructionElement",
                            "TunnelPart",
                            "Kunstwerkdeel",
                            "Scheiding",
                            "FunctioneelGebied",
                            "OpenbareRuimteLabel",
                            "Plaatsbepalingspunt"));

    private static final List<AttributeColumnMapping> bgtBaseAttributes =
            Arrays.asList(
                    new AttributeColumnMapping("gmlId", "char(32)", true, true),
                    new AttributeColumnMapping("identificatie"),
                    new SimpleDateFormatAttributeColumnMapping(
                            "LV-publicatiedatum", "timestamp", PATTERN_XML_TIMESTAMP),
                    new SimpleDateFormatAttributeColumnMapping(
                            "creationDate", "date", PATTERN_XML_DATE),
                    new SimpleDateFormatAttributeColumnMapping(
                            "tijdstipRegistratie", "timestamp", PATTERN_XML_TIMESTAMP),
                    new SimpleDateFormatAttributeColumnMapping(
                            EIND_REGISTRATIE, "timestamp", false, PATTERN_XML_TIMESTAMP),
                    new SimpleDateFormatAttributeColumnMapping(
                            "terminationDate", "date", false, PATTERN_XML_DATE),
                    new AttributeColumnMapping("bronhouder"),
                    new BooleanAttributeColumnMapping("inOnderzoek"),
                    new IntegerAttributeColumnMapping("relatieveHoogteligging"),
                    new AttributeColumnMapping("bgt-status"),
                    new AttributeColumnMapping("plus-status"));

    private List<AttributeColumnMapping> withBaseAttributes(AttributeColumnMapping... attributes) {
        return Stream.concat(bgtBaseAttributes.stream(), Stream.of(attributes))
                .collect(Collectors.toList());
    }

    public BGTSchema() {
        super();

        addObjectType(
                new BGTObjectType(
                        this,
                        "Bak",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dBak", "geometry(POINT, 28992)"))));

        addObjectType(
                new BGTObjectType(
                        this,
                        "PlantCover",
                        withBaseAttributes(
                                new AttributeColumnMapping("plus-fysiekVoorkomen"),
                                new AttributeColumnMapping("class"),
                                new BooleanAttributeColumnMapping(
                                        "begroeidTerreindeelOpTalud", false),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dBegroeidTerreindeel"),
                                new GeometryAttributeColumnMapping(
                                        "kruinlijnBegroeidTerreindeel"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Bord",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dBord", "geometry(POINT, 28992)"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Buurt",
                        withBaseAttributes(
                                new AttributeColumnMapping("naam", "varchar(255)", false),
                                new AttributeColumnMapping("buurtcode"),
                                new AttributeColumnMapping("wijk", "varchar(255)", false),
                                new GeometryAttributeColumnMapping("geometrie2d"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "FunctioneelGebied",
                        withBaseAttributes(
                                new AttributeColumnMapping("naam", "varchar(255)", false),
                                new AttributeColumnMapping("bgt-type"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dFunctioneelGebied"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "BuildingInstallation",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("plus-typeGebouwInstallatie"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dGebouwInstallatie"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Installatie",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dInstallatie", "geometry(POINT, 28992)"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Kast",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dKast", "geometry(POINT, 28992)"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Kunstwerkdeel",
                        withBaseAttributes(
                                new AttributeColumnMapping("bgt-type"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dOverigeConstructie"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Mast",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dMast", "geometry(POINT, 28992)"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "OnbegroeidTerreindeel",
                        withBaseAttributes(
                                new AttributeColumnMapping("bgt-fysiekVoorkomen"),
                                new AttributeColumnMapping("plus-fysiekVoorkomen"),
                                new BooleanAttributeColumnMapping(
                                        "onbegroeidTerreindeelOpTalud", false),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dOnbegroeidTerreindeel"),
                                new GeometryAttributeColumnMapping(
                                        "kruinlijnOnbegroeidTerreindeel"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "OndersteunendWaterdeel",
                        withBaseAttributes(
                                new AttributeColumnMapping("class"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dOndersteunendWaterdeel"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "AuxiliaryTrafficArea",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("surfaceMaterial"),
                                new AttributeColumnMapping("plus-functieOndersteunendWegdeel"),
                                new AttributeColumnMapping(
                                        "plus-fysiekVoorkomenOndersteunendWegdeel"),
                                new BooleanAttributeColumnMapping(
                                        "ondersteunendWegdeelOpTalud", false),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dOndersteunendWegdeel"),
                                new GeometryAttributeColumnMapping(
                                        "kruinlijnOndersteunendWegdeel"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "OngeclassificeerdObject",
                        withBaseAttributes(new GeometryAttributeColumnMapping("geometrie2d"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "OpenbareRuimte",
                        withBaseAttributes(new GeometryAttributeColumnMapping("geometrie2d"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "OpenbareRuimteLabel",
                        withBaseAttributes(
                                new IntegerAttributeColumnMapping("idx", true, true),
                                new AttributeColumnMapping("tekst"),
                                new DoubleAttributeColumnMapping("hoek"),
                                new GeometryAttributeColumnMapping(
                                        "plaatsingspunt", "geometry(POINT, 28992)"),
                                new AttributeColumnMapping("openbareRuimteType"),
                                new AttributeColumnMapping(
                                        "identificatieBAGOPR", "varchar(16)", false))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "BridgeConstructionElement",
                        withBaseAttributes(
                                new BooleanAttributeColumnMapping(
                                        "overbruggingIsBeweegbaar", false),
                                new AttributeColumnMapping("hoortBijTypeOverbrugging"),
                                new AttributeColumnMapping("class"),
                                new AttributeColumnMapping(
                                        "identificatieBAGOPR", "varchar(16)", false),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dOverbruggingsdeel"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "OverigBouwwerk",
                        withBaseAttributes(
                                new AttributeColumnMapping("bgt-type"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dOverigeConstructie"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "OverigeScheiding",
                        withBaseAttributes(
                                new AttributeColumnMapping("plus-type", "varchar(255)", false),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dOverigeConstructie"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Paal",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("plus-type", "varchar(255)", false),
                                new AttributeColumnMapping(
                                        "hectometeraanduiding", "varchar(255)", false),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dPaal", "geometry(POINT, 28992)"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "BuildingPart",
                        withBaseAttributes(
                                new AttributeColumnMapping("identificatieBAGPND"),
                                new OneToManyColumnMapping("nummeraanduidingreeks"),
                                new GeometryAttributeColumnMapping("geometrie2dGrondvlak"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "nummeraanduidingreeks",
                        Arrays.asList(
                                new AttributeColumnMapping("pandgmlid", "varchar(255)", true, true),
                                new IntegerAttributeColumnMapping("idx", true, true),
                                new BooleanAttributeColumnMapping("pandeindregistratie", true),
                                new AttributeColumnMapping("tekst"),
                                new DoubleAttributeColumnMapping("hoek"),
                                new GeometryAttributeColumnMapping(
                                        "plaatsingspunt", "geometry(POINT, 28992)"),
                                new AttributeColumnMapping(
                                        "identificatieBAGVBOLaagsteHuisnummer",
                                        "varchar(16)",
                                        false),
                                new AttributeColumnMapping(
                                        "identificatieBAGVBOHoogsteHuisnummer",
                                        "varchar(16)",
                                        false))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Plaatsbepalingspunt",
                        Arrays.asList(
                                new AttributeColumnMapping("gmlId", "char(32)", true, true),
                                new AttributeColumnMapping("identificatie"),
                                new IntegerAttributeColumnMapping("nauwkeurigheid", false),
                                new SimpleDateFormatAttributeColumnMapping(
                                        "datumInwinning", "date", true, PATTERN_XML_DATE),
                                new AttributeColumnMapping(
                                        "inwinnendeInstantie", "varchar(255)", false),
                                new AttributeColumnMapping(
                                        "inwinningsmethode", "varchar(255)", true),
                                new GeometryAttributeColumnMapping(
                                        "geometrie", "geometry(POINT, 28992)"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Put",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dPut", "geometry(POINT, 28992)"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Scheiding",
                        withBaseAttributes(
                                new AttributeColumnMapping("bgt-type"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dOverigeConstructie"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Sensor",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping("geometrie2dSensor"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Railway",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping(
                                        "plus-functieSpoor", "varchar(255)", false),
                                new GeometryAttributeColumnMapping("geometrie2dSpoor"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Stadsdeel",
                        withBaseAttributes(
                                new AttributeColumnMapping("naam", "varchar(255)", false),
                                new AttributeColumnMapping("function", "varchar(255)", false),
                                new GeometryAttributeColumnMapping("geometrie2d"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Straatmeubilair",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dStraatmeubilair", "geometry(POINT, 28992)"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "TunnelPart",
                        withBaseAttributes(
                                new GeometryAttributeColumnMapping("geometrie2dTunneldeel"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "SolitaryVegetationObject",
                        withBaseAttributes(
                                new AttributeColumnMapping("class"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping("geometrie2dVegetatieObject"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Waterdeel",
                        withBaseAttributes(
                                new AttributeColumnMapping("class"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping("geometrie2dWaterdeel"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Waterinrichtingselement",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dWaterinrichtingselement"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Waterschap",
                        withBaseAttributes(new GeometryAttributeColumnMapping("geometrie2d"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "TrafficArea",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("plus-functieWegdeel"),
                                new AttributeColumnMapping("plus-fysiekVoorkomenWegdeel"),
                                new AttributeColumnMapping("surfaceMaterial"),
                                new BooleanAttributeColumnMapping("wegdeelOpTalud", false),
                                new GeometryAttributeColumnMapping("geometrie2dWegdeel"),
                                new GeometryAttributeColumnMapping("kruinlijnWegdeel"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Weginrichtingselement",
                        withBaseAttributes(
                                new AttributeColumnMapping("function"),
                                new AttributeColumnMapping("plus-type"),
                                new GeometryAttributeColumnMapping(
                                        "geometrie2dWeginrichtingselement"))));
        addObjectType(
                new BGTObjectType(
                        this,
                        "Wijk",
                        withBaseAttributes(
                                new AttributeColumnMapping("naam", "varchar(255)", false),
                                new AttributeColumnMapping("wijkcode"),
                                new GeometryAttributeColumnMapping("geometrie2d"))));
    }

    protected void addObjectType(BGTObjectType objectType) {
        super.addObjectType(objectType);
    }

    public static BGTSchema getInstance() {
        if (instance == null) {
            instance = new BGTSchema();
        }
        return instance;
    }

    public Stream<BGTObjectType> getAllObjectTypes() {
        return (Stream<BGTObjectType>) super.getAllObjectTypes();
    }

    public Stream<BGTObjectType> getOnlyBGTObjectTypes() {
        return getAllObjectTypes().filter(objectType -> !objectType.isIMGeoPlusType());
    }

    public Stream<BGTObjectType> getIMGeoPlusObjectTypes() {
        return getAllObjectTypes().filter(BGTObjectType::isIMGeoPlusType);
    }

    public BGTObjectType getObjectTypeByName(String name) {
        return (BGTObjectType) super.getObjectTypeByName(name);
    }

    /**
     * CityGML gml:id's hebben in IMGeo bestanden soms een extra 'b' voor het UUID, blijkelijk zodat
     * in mutatiebestanden geen dubbele id attributen voorkomen. Deze methode stript de extra 'b' en
     * eventuele streepjes en maakt alles lowercase.
     */
    public static String fixUUID(final String uuidParam) {
        String uuid = uuidParam.replaceAll("-", "").toLowerCase();
        if (uuid.length() == 33 && uuid.charAt(0) == 'b') {
            return uuid.substring(1);
        }
        if (uuid.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID: " + uuidParam);
        }
        return uuid;
    }
}
