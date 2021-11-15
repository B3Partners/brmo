/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.schema;

import nl.b3p.brmo.schema.mapping.ArrayAttributeMapping;
import nl.b3p.brmo.schema.mapping.AttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.GeometryAttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.OneToManyColumnMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ObjectType {
    private final String name;
    private final Schema schema;

    private List<AttributeColumnMapping> attributes;

    private final List<AttributeColumnMapping> primaryKeys;
    private final List<AttributeColumnMapping> directNonDefaultInsertAttributes;
    private final List<GeometryAttributeColumnMapping> geometryAttributes;
    private List<ObjectType> oneToManyAttributeObjectTypes;
    private List<ArrayAttributeMapping> arrayAttributes;
    private List<String> extraDataDefinitionSQL = new ArrayList<>();

    protected ObjectType(Schema schema, String name, List<AttributeColumnMapping> attributes) {
        this.schema = schema;
        this.name = name;
        this.attributes = attributes;

        this.primaryKeys = attributes.stream()
                .filter(AttributeColumnMapping::isPrimaryKey)
                .collect(Collectors.toList());
        this.directNonDefaultInsertAttributes = attributes.stream()
                .filter(AttributeColumnMapping::isDirectNonDefaultAttribute)
                .collect(Collectors.toList());
        this.geometryAttributes = attributes.stream()
                .filter(attributeColumnMapping -> (attributeColumnMapping instanceof GeometryAttributeColumnMapping))
                .map(attributeColumnMapping -> (GeometryAttributeColumnMapping) attributeColumnMapping)
                .collect(Collectors.toList());
    }

    public ObjectType addExtraDataDefinitionSQL(List<String> extraDataDefinitionSQL) {
        this.extraDataDefinitionSQL.addAll(extraDataDefinitionSQL);
        return this;
    }

    public String getName() {
        return name;
    }

    public List<AttributeColumnMapping> getAllAttributes() {
        return attributes;
    }

    public List<AttributeColumnMapping> getPrimaryKeys() {
        return primaryKeys;
    }

    public AttributeColumnMapping getAttributeByName(String name) {
        return attributes.stream().filter(a -> a.getName().equals(name)).findFirst().orElse(null);
    }

    public List<AttributeColumnMapping> getDirectAttributes() {
        return attributes.stream()
                .filter(AttributeColumnMapping::isDirectAttribute)
                .collect(Collectors.toList());
    }

    public List<AttributeColumnMapping> getDirectNonDefaultInsertAttributes() {
        return directNonDefaultInsertAttributes;
    }

    public boolean hasOnlyDirectAttributes() {
        return directNonDefaultInsertAttributes.size() == attributes.size();
    }

    public List<ObjectType> getOneToManyAttributeObjectTypes() {
        // Create on-demand because objectTypes map must be completely filled
        if (oneToManyAttributeObjectTypes == null) {
            oneToManyAttributeObjectTypes = attributes.stream()
                    .filter(attributeColumnMapping -> (attributeColumnMapping instanceof OneToManyColumnMapping))
                    .map(attributeColumnMapping -> schema.getObjectTypes().get(attributeColumnMapping.getName()))
                    .collect(Collectors.toList());
        }
        return oneToManyAttributeObjectTypes;
    }

    public List<ArrayAttributeMapping> getArrayAttributes() {
        if (arrayAttributes == null) {
            arrayAttributes = attributes.stream()
                    .filter(attribute -> attribute instanceof ArrayAttributeMapping)
                    .map(attribute -> (ArrayAttributeMapping) attribute)
                    .collect(Collectors.toList());
        }
        return arrayAttributes;
    }

    public List<GeometryAttributeColumnMapping> getGeometryAttributes() {
        return geometryAttributes;
    }

    public List<String> getExtraDataDefinitionSQL() {
        return extraDataDefinitionSQL;
    }
}
