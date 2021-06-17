package nl.b3p.brmo.imgeo;

import nl.b3p.brmo.sql.AttributeColumnMapping;
import nl.b3p.brmo.sql.BooleanAttributeColumnMapping;
import nl.b3p.brmo.sql.DoubleAttributeColumnMapping;
import nl.b3p.brmo.sql.GeometryAttributeColumnMapping;
import nl.b3p.brmo.sql.IntegerAttributeColumnMapping;
import nl.b3p.brmo.sql.OneToManyColumnMapping;
import nl.b3p.brmo.sql.SimpleDateFormatAttributeColumnMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static nl.b3p.brmo.sql.SimpleDateFormatAttributeColumnMapping.PATTERN_XML_DATE;
import static nl.b3p.brmo.sql.SimpleDateFormatAttributeColumnMapping.PATTERN_XML_TIMESTAMP;

public class IMGeoSchema {

    public static final String INDEX = "idx";

    public static final String EIND_REGISTRATIE = "eindRegistratie";

    public static Set<String> bgtObjectTypes = new HashSet<>(Arrays.asList(
            "TrafficArea",
            "AuxiliaryTrafficArea",
            "Railway",
            "OnbegroeidTerreindeel",
            "PlantCover",
            "Waterdeel",
            "OndersteunendWaterdeel",
            "BuildingPart",
            "nummeraanduidingreeks",
            // "OverigeConstructie", // Wel in gegevenscatalogus, niet in LV
            "OverigBouwwerk",
            "BridgeConstructionElement",
            "TunnelPart",
            "Kunstwerkdeel",
            "Scheiding",
            "FunctioneelGebied",
            "OpenbareRuimteLabel"//,
            // "Plaatsbepalingspunt" // TODO
    ));

    public static Map<String, List<AttributeColumnMapping>> objectTypeAttributes = new HashMap<>();

    public static List<AttributeColumnMapping> baseAttributes = Arrays.asList(
            new AttributeColumnMapping("gmlId", "varchar(255)", true, true),
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

    public static Set<String> noBaseAttributesObjectTypes = new HashSet<>(Arrays.asList(
            "nummeraanduidingreeks"
    ));

    static {
        objectTypeAttributes.put("Bak", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dBak", "geometry(POINT, 28992)")
        ));
        objectTypeAttributes.put("PlantCover", Arrays.asList(
                new AttributeColumnMapping("plus-fysiekVoorkomen"),
                new AttributeColumnMapping("class"),
                new BooleanAttributeColumnMapping("begroeidTerreindeelOpTalud", false),
                // XXX ook Curve
                new GeometryAttributeColumnMapping("kruinlijnBegroeidTerreindeel"),
                new GeometryAttributeColumnMapping("geometrie2dBegroeidTerreindeel")
        ));
        objectTypeAttributes.put("Bord", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dBord", "geometry(POINT, 28992)")
        ));
        objectTypeAttributes.put("Buurt", Arrays.asList(
                new AttributeColumnMapping("naam", "varchar(255)", false),
                new AttributeColumnMapping("buurtcode"),
                new AttributeColumnMapping("wijk", "varchar(255)", false),
                new GeometryAttributeColumnMapping("geometrie2d")
        ));
        objectTypeAttributes.put("FunctioneelGebied", Arrays.asList(
                new AttributeColumnMapping("naam", "varchar(255)", false),
                new AttributeColumnMapping("bgt-type"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dFunctioneelGebied")
        ));
        objectTypeAttributes.put("BuildingInstallation", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-typeGebouwInstallatie"),
                new GeometryAttributeColumnMapping("geometrie2dGebouwInstallatie")
        ));
        objectTypeAttributes.put("Installatie", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dInstallatie", "geometry(POINT, 28992)")
        ));
        objectTypeAttributes.put("Kast", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dKast", "geometry(POINT, 28992)")
        ));
        objectTypeAttributes.put("Kunstwerkdeel", Arrays.asList(
                new AttributeColumnMapping("bgt-type"),
                new AttributeColumnMapping("plus-type"),
                // XXX notNull vanwege Curve
                new GeometryAttributeColumnMapping("geometrie2dOverigeConstructie")
        ));
        objectTypeAttributes.put("Mast", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dMast", "geometry(POINT, 28992)")
        ));
        objectTypeAttributes.put("OnbegroeidTerreindeel", Arrays.asList(
                new AttributeColumnMapping("bgt-fysiekVoorkomen"),
                new AttributeColumnMapping("plus-fysiekVoorkomen"),
                new BooleanAttributeColumnMapping("onbegroeidTerreindeelOpTalud", false),
                // XXX Curve
                new GeometryAttributeColumnMapping("kruinlijnOnbegroeidTerreindeel"),
                new GeometryAttributeColumnMapping("geometrie2dOnbegroeidTerreindeel")
        ));
        objectTypeAttributes.put("OndersteunendWaterdeel", Arrays.asList(
                new AttributeColumnMapping("class"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dOndersteunendWaterdeel")
        ));
        objectTypeAttributes.put("AuxiliaryTrafficArea", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("surfaceMaterial"),
                new AttributeColumnMapping("plus-functieOndersteunendWegdeel"),
                new AttributeColumnMapping("plus-fysiekVoorkomenOndersteunendWegdeel"),
                new BooleanAttributeColumnMapping("ondersteunendWegdeelOpTalud", false),
                // XXX Curve
                new GeometryAttributeColumnMapping("kruinlijnOndersteunendWegdeel"),
                new GeometryAttributeColumnMapping("geometrie2dOndersteunendWegdeel")
        ));
        objectTypeAttributes.put("OngeclassificeerdObject", Arrays.asList(
                new GeometryAttributeColumnMapping("geometrie2d")
        ));
        objectTypeAttributes.put("OpenbareRuimte", Arrays.asList(
                new GeometryAttributeColumnMapping("geometrie2d")
        ));
        objectTypeAttributes.put("OpenbareRuimteLabel", Arrays.asList(
                new IntegerAttributeColumnMapping("idx", true, true),
                new AttributeColumnMapping("tekst"),
                new DoubleAttributeColumnMapping("hoek"),
                new GeometryAttributeColumnMapping("plaatsingspunt", "geometry(POINT, 28992)"),
                new AttributeColumnMapping("openbareRuimteType"),
                new AttributeColumnMapping("identificatieBAGOPR", "varchar(16)", false)
        ));
        objectTypeAttributes.put("BridgeConstructionElement", Arrays.asList(
                new BooleanAttributeColumnMapping("overbruggingIsBeweegbaar", false),
                new AttributeColumnMapping("hoortBijTypeOverbrugging"),
                new AttributeColumnMapping("class"),
                new AttributeColumnMapping("identificatieBAGOPR", "varchar(16)", false),
                new GeometryAttributeColumnMapping("geometrie2dOverbruggingsdeel")
        ));
        objectTypeAttributes.put("OverigBouwwerk", Arrays.asList(
                new AttributeColumnMapping("bgt-type"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dOverigeConstructie")
        ));
        objectTypeAttributes.put("OverigeScheiding", Arrays.asList(
                new AttributeColumnMapping("plus-type", "varchar(255)", false),
                new GeometryAttributeColumnMapping("geometrie2dOverigeConstructie")
        ));
        objectTypeAttributes.put("Paal", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type", "varchar(255)", false),
                new AttributeColumnMapping("hectometeraanduiding", "varchar(255)", false),
                new GeometryAttributeColumnMapping("geometrie2dPaal", "geometry(POINT, 28992)")
        ));
        objectTypeAttributes.put("BuildingPart", Arrays.asList(
                new AttributeColumnMapping("identificatieBAGPND"),
                new OneToManyColumnMapping("nummeraanduidingreeks"),
                new GeometryAttributeColumnMapping("geometrie2dGrondvlak")
        ));
        objectTypeAttributes.put("nummeraanduidingreeks", Arrays.asList(
                new AttributeColumnMapping("pandgmlid", "varchar(255)", true, true),
                new IntegerAttributeColumnMapping("idx", true, true),
                new BooleanAttributeColumnMapping("pandeindregistratie", true),
                new AttributeColumnMapping("tekst"),
                new DoubleAttributeColumnMapping("hoek"),
                new GeometryAttributeColumnMapping("plaatsingspunt", "geometry(POINT, 28992)"),
                new AttributeColumnMapping("identificatieBAGVBOLaagsteHuisnummer", "varchar(16)", false),
                new AttributeColumnMapping("identificatieBAGVBOHoogsteHuisnummer", "varchar(16)", false)
        ));
        objectTypeAttributes.put("Put", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dPut", "geometry(POINT, 28992)")
        ));
        objectTypeAttributes.put("Scheiding", Arrays.asList(
                new AttributeColumnMapping("bgt-type"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dOverigeConstructie")
        ));
        objectTypeAttributes.put("Sensor", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dSensor")
        ));
        objectTypeAttributes.put("Railway", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-functieSpoor", "varchar(255)", false),
                new GeometryAttributeColumnMapping("geometrie2dSpoor")
        ));
        objectTypeAttributes.put("Stadsdeel", Arrays.asList(
                new AttributeColumnMapping("naam", "varchar(255)", false),
                new AttributeColumnMapping("function", "varchar(255)", false),
                new GeometryAttributeColumnMapping("geometrie2d")
        ));
        objectTypeAttributes.put("Straatmeubilair", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dStraatmeubilair", "geometry(POINT, 28992)")
        ));
        objectTypeAttributes.put("TunnelPart", Arrays.asList(
                new GeometryAttributeColumnMapping("geometrie2dTunneldeel")
        ));
        objectTypeAttributes.put("SolitaryVegetationObject", Arrays.asList(
                new AttributeColumnMapping("class"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dVegetatieObject")
        ));
        objectTypeAttributes.put("Waterdeel", Arrays.asList(
                new AttributeColumnMapping("class"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dWaterdeel")
        ));
        objectTypeAttributes.put("Waterinrichtingselement", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dWaterinrichtingselement")
        ));
        objectTypeAttributes.put("Waterschap", Arrays.asList(
                new GeometryAttributeColumnMapping("geometrie2d")
        ));
        objectTypeAttributes.put("TrafficArea", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-functieWegdeel"),
                new AttributeColumnMapping("plus-fysiekVoorkomenWegdeel"),
                new AttributeColumnMapping("surfaceMaterial"),
                new BooleanAttributeColumnMapping("wegdeelOpTalud", false),
                // XXX Curve
                new GeometryAttributeColumnMapping("kruinlijnWegdeel"),
                new GeometryAttributeColumnMapping("geometrie2dWegdeel")
        ));
        objectTypeAttributes.put("Weginrichtingselement", Arrays.asList(
                new AttributeColumnMapping("function"),
                new AttributeColumnMapping("plus-type"),
                new GeometryAttributeColumnMapping("geometrie2dWeginrichtingselement")
        ));
        objectTypeAttributes.put("Wijk", Arrays.asList(
                new AttributeColumnMapping("naam", "varchar(255)", false),
                new AttributeColumnMapping("wijkcode"),
                new GeometryAttributeColumnMapping("geometrie2d")
        ));

        // Prepend base columns to all tables
        objectTypeAttributes.keySet().stream().filter(name -> !noBaseAttributesObjectTypes.contains(name)).forEach(name -> {
            List<AttributeColumnMapping> allAttributes = new ArrayList<>(baseAttributes);
            allAttributes.addAll(objectTypeAttributes.get(name));
            objectTypeAttributes.put(name, allAttributes);
        });

    }

    public static Set<String> getAllObjectTypes() {
        return objectTypeAttributes.keySet();
    }

    public static Set<String> getOnlyBGTObjectTypes() {
        return bgtObjectTypes;
    }

    public static Set<String> getIMGeoPlusObjectTypes() {
        Set<String> s = new HashSet<>(objectTypeAttributes.keySet());
        s.removeAll(bgtObjectTypes);
        return s;
    }
}
