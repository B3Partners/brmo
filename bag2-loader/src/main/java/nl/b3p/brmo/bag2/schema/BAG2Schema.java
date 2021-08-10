/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.schema;

import nl.b3p.brmo.schema.Schema;
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


    private static final List<AttributeColumnMapping> bag2BaseAttributes = Arrays.asList(
            new AttributeColumnMapping("identificatie",  "varchar", true, false),
            new AttributeColumnMapping("documentnummer"),
            new SimpleDateFormatAttributeColumnMapping("documentdatum", "date", PATTERN_XML_DATE),
            new BooleanAttributeColumnMapping("geconstateerd"),
            new BooleanAttributeColumnMapping("status")
    );

    private List<AttributeColumnMapping> withBaseAttributes(AttributeColumnMapping... attributes) {
        return Stream.concat(bag2BaseAttributes.stream(), Stream.of(attributes)).collect(Collectors.toList());
    }

    public BAG2Schema() {
        super();

        addObjectType(new BAG2ObjectType(this, "Pand", withBaseAttributes(
                new IntegerAttributeColumnMapping("oorspronkelijkBouwjaar"),
                new GeometryAttributeColumnMapping("geometrie", "geometry(POLYGON, 28992)")
        )));

        addObjectType(new BAG2ObjectType(this, "Verblijfsobject", withBaseAttributes(
                new AttributeColumnMapping("gebruiksdoel"),
                new IntegerAttributeColumnMapping("oppervlakte"),
                new AttributeColumnMapping("heeftAlsHoofdAdres", "varchar", false),
                new AttributeColumnMapping("maaktDeelUitVan", "varchar", false),
                new GeometryAttributeColumnMapping("geometrie", "geometry(GEOMETRY, 28992)")
        )));
    }

    protected void addObjectType(BAG2ObjectType objectType) {
        super.addObjectType(objectType);
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
