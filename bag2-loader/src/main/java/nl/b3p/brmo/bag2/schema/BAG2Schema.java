/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.schema;

import nl.b3p.brmo.schema.ObjectType;
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

    public static final String TIJDSTIP_NIETBAGLV = "tijdstipNietBagLV";

    private static final String WHERE_CLAUSE_ACTUEEL = "(begingeldigheid <= current_date and (eindgeldigheid is null or eindgeldigheid > current_date) and tijdstipinactief is null)";

    private static final List<AttributeColumnMapping> bag2BaseAttributes = Arrays.asList(
            new AttributeColumnMapping("objectid",  "serial", true, false, true),
            new AttributeColumnMapping("identificatie",  "char(16)", true, true),
            new IntegerAttributeColumnMapping("voorkomenidentificatie", true, true),
            new SimpleDateFormatAttributeColumnMapping("beginGeldigheid", "date", PATTERN_XML_DATE),
            new SimpleDateFormatAttributeColumnMapping("eindGeldigheid", "date", false, PATTERN_XML_DATE),
            new SimpleDateFormatAttributeColumnMapping("tijdstipRegistratie", "timestamp", PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("eindRegistratie", "timestamp", false, PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("tijdstipInactief", "timestamp", false, PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("tijdstipRegistratieLV", "timestamp", PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("tijdstipEindRegistratieLV", "timestamp", false, PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("tijdstipInactiefLV", "timestamp", false, PATTERN_XML_TIMESTAMP),
            // "Niet BAG" is not supported because it would have to be part of the primary key, which would complicate
            // things, and it is not very useful anyway. "Niet BAG" would be set for versions which were overwritten 
            // during "synchronisation" when a bronhouder detects previous versions were not correctly registered.
            // An update setting tijdstipNietBagLV would lead to deletion of the record and insertion of the newly
            // synchronized versions.
            // new SimpleDateFormatAttributeColumnMapping(TIJDSTIP_NIETBAGLV, "timestamp", true, PATTERN_XML_TIMESTAMP),
            new SimpleDateFormatAttributeColumnMapping("documentdatum", "date", PATTERN_XML_DATE),
            new AttributeColumnMapping("documentnummer"),
            new BooleanAttributeColumnMapping("geconstateerd"),
            new AttributeColumnMapping("status")
    );

    private List<AttributeColumnMapping> withBaseAttributes(AttributeColumnMapping... attributes) {
        return Stream.concat(bag2BaseAttributes.stream(), Stream.of(attributes)).collect(Collectors.toList());
    }

    public BAG2Schema() {
        super();

        addObjectType(new BAG2ObjectType(this, "Woonplaats", withBaseAttributes(
                new AttributeColumnMapping("naam"),
                new GeometryAttributeColumnMapping("geometrie", "geometry(GEOMETRY, 28992)")
        )).addExtraDataDefinitionSQL(List.of(
                "create or replace view v_woonplaats_actueel as select * from woonplaats where " + WHERE_CLAUSE_ACTUEEL
        )));

        addObjectType(new BAG2ObjectType(this, "OpenbareRuimte", withBaseAttributes(
                new AttributeColumnMapping("naam"),
                new AttributeColumnMapping("verkorteNaam", "varchar(255)", false),
                new AttributeColumnMapping("type"),
                new AttributeColumnMapping("ligtIn", "char(4)", false)
        )).addExtraDataDefinitionSQL(List.of(
                "create or replace view v_openbareruimte_actueel as select * from openbareruimte where " + WHERE_CLAUSE_ACTUEEL
        )));

        addObjectType(new BAG2ObjectType(this, "Nummeraanduiding", withBaseAttributes(
                new IntegerAttributeColumnMapping("huisnummer"),
                new AttributeColumnMapping("huisletter", "varchar(255)", false),
                new AttributeColumnMapping("huisnummertoevoeging", "varchar(255)", false),
                new AttributeColumnMapping("postcode", "char(6)", false),
                new AttributeColumnMapping("typeAdresseerbaarObject", "varchar(255)", false),
                new AttributeColumnMapping("ligtIn", "char(4)", false),
                new AttributeColumnMapping("ligtAan", "char(16)", false)
        )).addExtraDataDefinitionSQL(List.of(
                "create or replace view v_nummeraanduiding_actueel as select * from nummeraanduiding where " + WHERE_CLAUSE_ACTUEEL
        )));

        addObjectType(new BAG2ObjectType(this, "Verblijfsobject", withBaseAttributes(
                new ArrayAttributeMapping("gebruiksdoel", "gebruiksdoel", "varchar(255)"),
                new IntegerAttributeColumnMapping("oppervlakte"),
                new AttributeColumnMapping("heeftAlsHoofdadres", "char(16)", false),
                new ArrayAttributeMapping("heeftAlsNevenadres", "nevenadres", "char(16)"),
                new ArrayAttributeMapping("maaktDeelUitVan", "maaktdeeluitvan", "char(16)"),
                new GeometryAttributeColumnMapping("geometrie", "geometry(GEOMETRY, 28992)")
        )).addExtraDataDefinitionSQL(List.of(
                "create or replace view v_verblijfsobject_actueel as select * from verblijfsobject where " + WHERE_CLAUSE_ACTUEEL,
                "create index verblijfsobject_nevenadres_idx on verblijfsobject_nevenadres (identificatie, voorkomenidentificatie)",
                "create index verblijfsobject_maaktdeeluitvan_idx on verblijfsobject_maaktdeeluitvan (identificatie, voorkomenidentificatie)",
                "create index verblijfsobject_gebruiksdoel_idx on verblijfsobject_gebruiksdoel (identificatie, voorkomenidentificatie)"
        )));

        addObjectType(new BAG2ObjectType(this, "Ligplaats", withBaseAttributes(
                new GeometryAttributeColumnMapping("geometrie", "geometry(POLYGON, 28992)"),
                new AttributeColumnMapping("heeftAlsHoofdadres", "char(16)", false),
                new ArrayAttributeMapping("heeftAlsNevenadres", "nevenadres", "char(16)")
        )).addExtraDataDefinitionSQL(List.of(
                "create or replace view v_ligplaats_actueel as select * from ligplaats where " + WHERE_CLAUSE_ACTUEEL,
                "create index ligplaats_nevenadres_idx on ligplaats_nevenadres (identificatie, voorkomenidentificatie)"
        )));

        addObjectType(new BAG2ObjectType(this, "Standplaats", withBaseAttributes(
                new GeometryAttributeColumnMapping("geometrie", "geometry(POLYGON, 28992)"),
                new AttributeColumnMapping("heeftAlsHoofdadres", "char(16)", false),
                new ArrayAttributeMapping("heeftAlsNevenadres", "nevenadres", "char(16)")
        )).addExtraDataDefinitionSQL(List.of(
                "create or replace view v_standplaats_actueel as select * from standplaats where " + WHERE_CLAUSE_ACTUEEL,
                "create index standplaats_nevenadres_idx on standplaats_nevenadres (identificatie, voorkomenidentificatie)"
        )));

        addObjectType(new BAG2ObjectType(this, "Pand", withBaseAttributes(
                new IntegerAttributeColumnMapping("oorspronkelijkBouwjaar"),
                new GeometryAttributeColumnMapping("geometrie", "geometry(POLYGON, 28992)")
        )).addExtraDataDefinitionSQL(List.of(
                "create or replace view v_pand_actueel as select * from pand where " + WHERE_CLAUSE_ACTUEEL
        )));
    }

    @Override
    protected void addObjectType(ObjectType objectType) {
        // Always add a unique index for the OBJECTID column, which is not the primary key but only for ArcGIS

        // XXX table prefixes not supported here and also not in extra DDL above

        String tableName = objectType.getName().toLowerCase();
        String sql = String.format("create index %s_objectid_idx on %s (objectid)", tableName, tableName);
        objectType.addExtraDataDefinitionSQL(List.of(sql));
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
