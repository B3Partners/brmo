/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.schema;

import nl.b3p.brmo.schema.mapping.AttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.GeometryAttributeColumnMapping;
import nl.b3p.brmo.schema.mapping.OneToManyColumnMapping;

import java.util.List;
import java.util.stream.Collectors;

public class ObjectType {
    private final String name;
    private final Schema schema;

    private List<AttributeColumnMapping> attributes;

    private final List<AttributeColumnMapping> primaryKeys;
    private final List<AttributeColumnMapping> directAttributes;
    private final List<GeometryAttributeColumnMapping> geometryAttributes;
    private List<ObjectType> oneToManyAttributeObjectTypes;

    protected ObjectType(Schema schema, String name, List<AttributeColumnMapping> attributes) {
        this.schema = schema;
        this.name = name;
        this.attributes = attributes;

        this.primaryKeys = attributes.stream()
                .filter(AttributeColumnMapping::isPrimaryKey)
                .collect(Collectors.toList());
        this.directAttributes = attributes.stream()
                .filter(attributeColumnMapping -> !(attributeColumnMapping instanceof OneToManyColumnMapping))
                .collect(Collectors.toList());
        this.geometryAttributes = attributes.stream()
                .filter(attributeColumnMapping -> (attributeColumnMapping instanceof GeometryAttributeColumnMapping))
                .map(attributeColumnMapping -> (GeometryAttributeColumnMapping) attributeColumnMapping)
                .collect(Collectors.toList());
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

    public List<AttributeColumnMapping> getDirectAttributes() {
        return directAttributes;
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

    public List<GeometryAttributeColumnMapping> getGeometryAttributes() {
        return geometryAttributes;
    }
}
