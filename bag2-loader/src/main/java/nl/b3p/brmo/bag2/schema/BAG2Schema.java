/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.schema;

import nl.b3p.brmo.schema.Schema;
import nl.b3p.brmo.schema.mapping.ArrayAttributeMapping;
import nl.b3p.brmo.schema.mapping.AttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.BooleanAttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.GeometryAttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.IntegerAttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.SimpleDateFormatAttributeColumnMapping;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.b3p.brmo.schema.mapping.SimpleDateFormatAttributeColumnMapping.PATTERN_XML_DATE;
import static nl.b3p.brmo.schema.mapping.SimpleDateFormatAttributeColumnMapping.PATTERN_XML_TIMESTAMP;

public class BAG2Schema extends Schema {
    private static BAG2Schema instance;

    static final Set<String> bag2ObjectTypes = new HashSet<>(Arrays.asList(
            "Pand",
            "Verblijfsobject",
            "Nummeraanduiding",
            "OpenbareRuimte",
            "Ligplaats",
            "Standplaats",
            "Woonplaats"
            // TODO in onderzoek
            // TODO inactief
            // TODO gemeente woonplaats relatie
    ));

    public static final String ARCHIVE_SUFFIX = "_archief";
    private static final String VOORKOMEN_IDENTIFICATIE = "voorkomenidentificatie";
    public static final String EIND_REGISTRATIE = "eindRegistratie";

    private static final List<AttributeColumnMapping> bag2BaseAttributes = Arrays.asList(
            new AttributeColumnMapping("identificatie",  "char(16)", true, true),
            new IntegerAttributeColumnMapping(VOORKOMEN_IDENTIFICATIE, true, false),
            new SimpleDateFormatAttributeColumnMapping("beginGeldigheid", "date", PATTERN_XML_DATE),
            new SimpleDateFormatAttributeColumnMapping("eindGeldigheid", "date", false, PATTERN_XML_DATE),
            new SimpleDateFormatAttributeColumnMapping("tijdstipRegistratie", "timestamp", PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping(EIND_REGISTRATIE, "timestamp", false, PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("tijdstipInactief", "timestamp", false, PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("tijdstipRegistratieLV", "timestamp", PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("tijdstipEindRegistratieLV", "timestamp", false, PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("tijdstipInactiefLV", "timestamp", false, PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("tijdstipNietBagLV", "timestamp", false, PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("documentdatum", "date", PATTERN_XML_DATE),
            new AttributeColumnMapping("documentnummer"),
            new BooleanAttributeColumnMapping("geconstateerd"),
            new AttributeColumnMapping("status")
    );

    private static final List<String> archiveOnlyAttributes = Arrays.asList(
            "eindGeldigheid", EIND_REGISTRATIE, "tijdstipInactief", "tijdstipEindRegistratieLV", "tijdstipInactiefLV", "tijdstipNietBagLV"
    );

    private List<AttributeColumnMapping> withBaseAttributes(AttributeColumnMapping... attributes) {
        return Stream.concat(bag2BaseAttributes.stream(), Stream.of(attributes)).collect(Collectors.toList());
    }

    public BAG2Schema() {
        super();

        addObjectType(new BAG2ObjectType(this, "Woonplaats", withBaseAttributes(
                new AttributeColumnMapping("naam"),
                new GeometryAttributeColumnMapping("geometrie", "geometry(GEOMETRY, 28992)")
        )));

        addObjectType(new BAG2ObjectType(this, "OpenbareRuimte", withBaseAttributes(
                new AttributeColumnMapping("naam"),
                new AttributeColumnMapping("verkorteNaam", "varchar(255)", false),
                new AttributeColumnMapping("type"),
                new AttributeColumnMapping("ligtIn", "char(4)", false)
        )));

        addObjectType(new BAG2ObjectType(this, "Nummeraanduiding", withBaseAttributes(
                new IntegerAttributeColumnMapping("huisnummer"),
                new AttributeColumnMapping("huisletter", "varchar(255)", false),
                new AttributeColumnMapping("huisnummertoevoeging", "varchar(255)", false),
                new AttributeColumnMapping("postcode", "char(6)", false),
                new AttributeColumnMapping("typeAdresseerbaarObject", "varchar(255)", false),
                new AttributeColumnMapping("ligtIn", "char(4)", false),
                new AttributeColumnMapping("ligtAan", "char(16)", false)
        )));

        addObjectType(new BAG2ObjectType(this, "Verblijfsobject", withBaseAttributes(
                new ArrayAttributeMapping("gebruiksdoel", "gebruiksdoel", "varchar(255)"),
                new IntegerAttributeColumnMapping("oppervlakte"),
                new AttributeColumnMapping("heeftAlsHoofdadres", "varchar", false),
                new ArrayAttributeMapping("heeftAlsNevenadres", "nevenadres", "char(16)"),
                new ArrayAttributeMapping("maaktDeelUitVan", "pand", "char(16)"),
                new GeometryAttributeColumnMapping("geometrie", "geometry(GEOMETRY, 28992)")
        )));

        addObjectType(new BAG2ObjectType(this, "Ligplaats", withBaseAttributes(
                new GeometryAttributeColumnMapping("geometrie", "geometry(POLYGON, 28992)"),
                new AttributeColumnMapping("heeftAlsHoofdadres"),
                new ArrayAttributeMapping("heeftAlsNevenadres", "nevenadres", "char(16)")
        )));

        addObjectType(new BAG2ObjectType(this, "Standplaats", withBaseAttributes(
                new GeometryAttributeColumnMapping("geometrie", "geometry(POLYGON, 28992)"),
                new AttributeColumnMapping("heeftAlsHoofdadres"),
                new ArrayAttributeMapping("heeftAlsNevenadres", "nevenadres", "char(16)")
        )));

        addObjectType(new BAG2ObjectType(this, "Pand", withBaseAttributes(
                new IntegerAttributeColumnMapping("oorspronkelijkBouwjaar"),
                new GeometryAttributeColumnMapping("geometrie", "geometry(POLYGON, 28992)")
        )));

    }

    protected void addObjectType(BAG2ObjectType objectType) {

        // Add object type for currently valid objects without archive attributes
        BAG2ObjectType currentType = new BAG2ObjectType(this, objectType.getName(),
                objectType.getAllAttributes().stream()
                        .filter(attribute -> !archiveOnlyAttributes.contains(attribute.getName()))
                        .collect(Collectors.toList()));
        super.addObjectType(currentType);

        // Add object type for ended objects with archive attributes, and composite primary key
        BAG2ObjectType archiveType = new BAG2ObjectType(this, objectType.getName() + ARCHIVE_SUFFIX,
                objectType.getAllAttributes().stream()
                        .map(attribute -> {
                            if (attribute.getName().equals(VOORKOMEN_IDENTIFICATIE)) {
                                return new IntegerAttributeColumnMapping(VOORKOMEN_IDENTIFICATIE, true, true);
                            } else {
                                return attribute;
                            }
                        })
                        .collect(Collectors.toList()));
        super.addObjectType(archiveType);
    }

    public static BAG2Schema getInstance() {
        if (instance == null) {
            instance = new BAG2Schema();
        }
        return instance;
    }

    public Stream<BAG2ObjectType> getAllObjectTypes() {
        return (Stream<BAG2ObjectType>) super.getAllObjectTypes();
    }

    public BAG2ObjectType getObjectTypeByName(String name) {
        return (BAG2ObjectType) super.getObjectTypeByName(name);
    }
}
