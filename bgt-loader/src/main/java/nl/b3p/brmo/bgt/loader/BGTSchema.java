package nl.b3p.brmo.bgt.loader;

import nl.b3p.brmo.sql.mapping.AttributeColumnMapping;
import nl.b3p.brmo.sql.mapping.BooleanAttributeColumnMapping;
import nl.b3p.brmo.sql.mapping.DoubleAttributeColumnMapping;
import nl.b3p.brmo.sql.mapping.GeometryAttributeColumnMapping;
import nl.b3p.brmo.sql.mapping.IntegerAttributeColumnMapping;
import nl.b3p.brmo.sql.mapping.OneToManyColumnMapping;
import nl.b3p.brmo.sql.mapping.SimpleDateFormatAttributeColumnMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.b3p.brmo.sql.mapping.SimpleDateFormatAttributeColumnMapping.PATTERN_XML_DATE;
import static nl.b3p.brmo.sql.mapping.SimpleDateFormatAttributeColumnMapping.PATTERN_XML_TIMESTAMP;

public class BGTSchema {
    public static class BGTObjectType {
        String name;
        boolean isIMGeoPlusType;
        List<AttributeColumnMapping> attributes;

        private final List<AttributeColumnMapping> primaryKeys;
        private final List<AttributeColumnMapping> directAttributes;
        private final List<GeometryAttributeColumnMapping> geometryAttributes;
        private List<BGTObjectType> oneToManyAttributeObjectTypes;

        private BGTObjectType(String name, List<AttributeColumnMapping> attributes) {
            this(name, true, attributes);
        }

        private BGTObjectType(String name, boolean includeBaseAttributes, List<AttributeColumnMapping> additionalAttributes) {
            this.name = name;
            if (includeBaseAttributes) {
                this.attributes = Stream.concat(baseAttributes.stream(), additionalAttributes.stream())
                        .collect(Collectors.toList());
            } else {
                this.attributes = additionalAttributes;
            }
            this.isIMGeoPlusType = !bgtObjectTypes.contains(name);

            this.primaryKeys = attributes.stream()
                    .filter(AttributeColumnMapping::isPrimaryKey)
                    .collect(Collectors.toList());
            this.directAttributes = attributes.stream()
                    .filter(attributeColumnMapping -> !(attributeColumnMapping instanceof OneToManyColumnMapping))
                    .collect(Collectors.toList());
            this.geometryAttributes = attributes.stream()
                    .filter(attributeColumnMapping -> (attributeColumnMapping instanceof GeometryAttributeColumnMapping))
                    .map(attributeColumnMapping -> (GeometryAttributeColumnMapping)attributeColumnMapping)
                    .collect(Collectors.toList());
        }

        public String getName() {
            return name;
        }

        public boolean isIMGeoPlusType() {
            return isIMGeoPlusType;
        }

        public List<AttributeColumnMapping> getAllAttributes() {
            return attributes;
        }

        public List<AttributeColumnMapping> getPrimaryKeys() {
            return primaryKeys;
        }

        public List<AttributeColumnMapping> getDirectAttributes() {
            return directAttributes;
        }

        public List<BGTObjectType> getOneToManyAttributeObjectTypes() {
            // Create on-demand because objectTypes map must be completely filled
            if (oneToManyAttributeObjectTypes == null) {
                oneToManyAttributeObjectTypes = attributes.stream()
                        .filter(attributeColumnMapping -> (attributeColumnMapping instanceof OneToManyColumnMapping))
                        .map(attributeColumnMapping -> objectTypes.get(attributeColumnMapping.getName()))
                        .collect(Collectors.toList());
            }
            return oneToManyAttributeObjectTypes;
        }

        public List<GeometryAttributeColumnMapping> getGeometryAttributes() {
            return geometryAttributes;
        }
    }

    public static final String INDEX = "idx";
    public static final String EIND_REGISTRATIE = "eindRegistratie";

    private static final Set<String> bgtObjectTypes = new HashSet<>(Arrays.asList(
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
            "Plaatsbepalingspunt"
    ));

    private static final Map<String, BGTObjectType> objectTypes = new HashMap<>();

    private static final List<AttributeColumnMapping> baseAttributes = Arrays.asList(
            new AttributeColumnMapping("gmlId", "char(32)", true, true),
            new AttributeColumnMapping("identificatie"),
            new SimpleDateFormatAttributeColumnMapping("LV-publicatiedatum", "timestamp", PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("creationDate", "date", PATTERN_XML_DATE),
            new SimpleDateFormatAttributeColumnMapping("tijdstipRegistratie", "timestamp", PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping(EIND_REGISTRATIE, "timestamp", false, PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("terminationDate", "date", false, PATTERN_XML_DATE),
            new AttributeColumnMapping("bronhouder"),
            new BooleanAttributeColumnMapping("inOnderzoek"),
            new IntegerAttributeColumnMapping("relatieveHoogteligging"),
            new AttributeColumnMapping("bgt-status"),
            new AttributeColumnMapping("plus-status")
    );

    private static void addObjectType(BGTObjectType objectType) {
        objectTypes.put(objectType.getName(), objectType);
    }

    static {
        addObjectType(new BGTObjectType("Bak", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dBak", "geometry(POINT, 28992)")
        )));

        addObjectType(new BGTObjectType("PlantCover", Arrays.asList(
                new AttributeColumnMapping("plus-fysiekVoorkomen"),
                new AttributeColumnMapping("class"),
                new BooleanAttributeColumnMapping("begroeidTerreindeelOpTalud", false),
                new GeometryAttributeColumnMapping("kruinlijnBegroeidTerreindeel"),
                new GeometryAttributeColumnMapping("geometrie2dBegroeidTerreindeel")
        )));
        addObjectType(new BGTObjectType("Bord", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dBord", "geometry(POINT, 28992)")
        )));
        addObjectType(new BGTObjectType("Buurt", Arrays.asList(
                new AttributeColumnMapping("naam", "varchar(255)", false),
                new AttributeColumnMapping("buurtcode"),
                new AttributeColumnMapping("wijk", "varchar(255)", false),
                new GeometryAttributeColumnMapping("geometrie2d")
        )));
        addObjectType(new BGTObjectType("FunctioneelGebied", Arrays.asList(
                new AttributeColumnMapping("naam", "varchar(255)", false),
                new AttributeColumnMapping("bgt-type"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dFunctioneelGebied")
        )));
        addObjectType(new BGTObjectType("BuildingInstallation", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-typeGebouwInstallatie"),
                new GeometryAttributeColumnMapping("geometrie2dGebouwInstallatie")
        )));
        addObjectType(new BGTObjectType("Installatie", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dInstallatie", "geometry(POINT, 28992)")
        )));
        addObjectType(new BGTObjectType("Kast", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dKast", "geometry(POINT, 28992)")
        )));
        addObjectType(new BGTObjectType("Kunstwerkdeel", Arrays.asList(
                new AttributeColumnMapping("bgt-type"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dOverigeConstructie")
        )));
        addObjectType(new BGTObjectType("Mast", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dMast", "geometry(POINT, 28992)")
        )));
        addObjectType(new BGTObjectType("OnbegroeidTerreindeel", Arrays.asList(
                new AttributeColumnMapping("bgt-fysiekVoorkomen"),
                new AttributeColumnMapping("plus-fysiekVoorkomen"),
                new BooleanAttributeColumnMapping("onbegroeidTerreindeelOpTalud", false),
                new GeometryAttributeColumnMapping("kruinlijnOnbegroeidTerreindeel"),
                new GeometryAttributeColumnMapping("geometrie2dOnbegroeidTerreindeel")
        )));
        addObjectType(new BGTObjectType("OndersteunendWaterdeel", Arrays.asList(
                new AttributeColumnMapping("class"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dOndersteunendWaterdeel")
        )));
        addObjectType(new BGTObjectType("AuxiliaryTrafficArea", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("surfaceMaterial"),
                new AttributeColumnMapping("plus-functieOndersteunendWegdeel"),
                new AttributeColumnMapping("plus-fysiekVoorkomenOndersteunendWegdeel"),
                new BooleanAttributeColumnMapping("ondersteunendWegdeelOpTalud", false),
                new GeometryAttributeColumnMapping("kruinlijnOndersteunendWegdeel"),
                new GeometryAttributeColumnMapping("geometrie2dOndersteunendWegdeel")
        )));
        addObjectType(new BGTObjectType("OngeclassificeerdObject", Arrays.asList(
                new GeometryAttributeColumnMapping("geometrie2d")
        )));
        addObjectType(new BGTObjectType("OpenbareRuimte", Arrays.asList(
                new GeometryAttributeColumnMapping("geometrie2d")
        )));
        addObjectType(new BGTObjectType("OpenbareRuimteLabel", Arrays.asList(
                new IntegerAttributeColumnMapping("idx", true, true),
                new AttributeColumnMapping("tekst"),
                new DoubleAttributeColumnMapping("hoek"),
                new GeometryAttributeColumnMapping("plaatsingspunt", "geometry(POINT, 28992)"),
                new AttributeColumnMapping("openbareRuimteType"),
                new AttributeColumnMapping("identificatieBAGOPR", "varchar(16)", false)
        )));
        addObjectType(new BGTObjectType("BridgeConstructionElement", Arrays.asList(
                new BooleanAttributeColumnMapping("overbruggingIsBeweegbaar", false),
                new AttributeColumnMapping("hoortBijTypeOverbrugging"),
                new AttributeColumnMapping("class"),
                new AttributeColumnMapping("identificatieBAGOPR", "varchar(16)", false),
                new GeometryAttributeColumnMapping("geometrie2dOverbruggingsdeel")
        )));
        addObjectType(new BGTObjectType("OverigBouwwerk", Arrays.asList(
                new AttributeColumnMapping("bgt-type"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dOverigeConstructie")
        )));
        addObjectType(new BGTObjectType("OverigeScheiding", Arrays.asList(
                new AttributeColumnMapping("plus-type", "varchar(255)", false),
                new GeometryAttributeColumnMapping("geometrie2dOverigeConstructie")
        )));
        addObjectType(new BGTObjectType("Paal", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type", "varchar(255)", false),
                new AttributeColumnMapping("hectometeraanduiding", "varchar(255)", false),
                new GeometryAttributeColumnMapping("geometrie2dPaal", "geometry(POINT, 28992)")
        )));
        addObjectType(new BGTObjectType("BuildingPart", Arrays.asList(
                new AttributeColumnMapping("identificatieBAGPND"),
                new OneToManyColumnMapping("nummeraanduidingreeks"),
                new GeometryAttributeColumnMapping("geometrie2dGrondvlak")
        )));
        addObjectType(new BGTObjectType("nummeraanduidingreeks", false, Arrays.asList(
                new AttributeColumnMapping("pandgmlid", "varchar(255)", true, true),
                new IntegerAttributeColumnMapping("idx", true, true),
                new BooleanAttributeColumnMapping("pandeindregistratie", true),
                new AttributeColumnMapping("tekst"),
                new DoubleAttributeColumnMapping("hoek"),
                new GeometryAttributeColumnMapping("plaatsingspunt", "geometry(POINT, 28992)"),
                new AttributeColumnMapping("identificatieBAGVBOLaagsteHuisnummer", "varchar(16)", false),
                new AttributeColumnMapping("identificatieBAGVBOHoogsteHuisnummer", "varchar(16)", false)
        )));
        addObjectType(new BGTObjectType("Plaatsbepalingspunt", false, Arrays.asList(
                new AttributeColumnMapping("gmlId", "char(32)", true, true),
                new AttributeColumnMapping("identificatie"),
                new IntegerAttributeColumnMapping("nauwkeurigheid", false),
                new SimpleDateFormatAttributeColumnMapping("datumInwinning", "date", true, PATTERN_XML_DATE),
                new AttributeColumnMapping("inwinnendeInstantie", "varchar(255)", false),
                new AttributeColumnMapping("inwinningsmethode", "varchar(255)", true),
                new GeometryAttributeColumnMapping("geometrie", "geometry(POINT, 28992)")
        )));
        addObjectType(new BGTObjectType("Put", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dPut", "geometry(POINT, 28992)")
        )));
        addObjectType(new BGTObjectType("Scheiding", Arrays.asList(
                new AttributeColumnMapping("bgt-type"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dOverigeConstructie")
        )));
        addObjectType(new BGTObjectType("Sensor", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dSensor")
        )));
        addObjectType(new BGTObjectType("Railway", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-functieSpoor", "varchar(255)", false),
                new GeometryAttributeColumnMapping("geometrie2dSpoor")
        )));
        addObjectType(new BGTObjectType("Stadsdeel", Arrays.asList(
                new AttributeColumnMapping("naam", "varchar(255)", false),
                new AttributeColumnMapping("function", "varchar(255)", false),
                new GeometryAttributeColumnMapping("geometrie2d")
        )));
        addObjectType(new BGTObjectType("Straatmeubilair", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dStraatmeubilair", "geometry(POINT, 28992)")
        )));
        addObjectType(new BGTObjectType("TunnelPart", Arrays.asList(
                new GeometryAttributeColumnMapping("geometrie2dTunneldeel")
        )));
        addObjectType(new BGTObjectType("SolitaryVegetationObject", Arrays.asList(
                new AttributeColumnMapping("class"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dVegetatieObject")
        )));
        addObjectType(new BGTObjectType("Waterdeel", Arrays.asList(
                new AttributeColumnMapping("class"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dWaterdeel")
        )));
        addObjectType(new BGTObjectType("Waterinrichtingselement", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dWaterinrichtingselement")
        )));
        addObjectType(new BGTObjectType("Waterschap", Arrays.asList(
                new GeometryAttributeColumnMapping("geometrie2d")
        )));
        addObjectType(new BGTObjectType("TrafficArea", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-functieWegdeel"),
                new AttributeColumnMapping("plus-fysiekVoorkomenWegdeel"),
                new AttributeColumnMapping("surfaceMaterial"),
                new BooleanAttributeColumnMapping("wegdeelOpTalud", false),
                new GeometryAttributeColumnMapping("kruinlijnWegdeel"),
                new GeometryAttributeColumnMapping("geometrie2dWegdeel")
        )));
        addObjectType(new BGTObjectType("Weginrichtingselement", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dWeginrichtingselement")
        )));
        addObjectType(new BGTObjectType("Wijk", Arrays.asList(
                new AttributeColumnMapping("naam", "varchar(255)", false),
                new AttributeColumnMapping("wijkcode"),
                new GeometryAttributeColumnMapping("geometrie2d")
        )));
    }

    public static Stream<BGTObjectType> getAllObjectTypes() {
        return objectTypes.values().stream();
    }

    public static BGTObjectType getObjectTypeByName(String name) {
        return objectTypes.get(name);
    }

    public static Stream<BGTObjectType> getOnlyBGTObjectTypes() {
        return objectTypes.values().stream().filter(objectType -> !objectType.isIMGeoPlusType());
    }

    public static Stream<BGTObjectType> getIMGeoPlusObjectTypes() {
        return objectTypes.values().stream().filter(BGTObjectType::isIMGeoPlusType);
    }

    /**
     * CityGML gml:id's hebben in IMGeo bestanden soms een extra 'b' voor het UUID, blijkelijk zodat in mutatiebestanden
     * geen dubbele id attributen voorkomen. Deze methode stript de extra 'b' en eventuele streepjes en maakt alles
     * lowercase.
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
