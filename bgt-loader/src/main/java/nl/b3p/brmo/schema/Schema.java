/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.schema;

import nl.b3p.brmo.bgt.schema.BGTObjectType;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Schema {
    public static final String INDEX = "idx";

    private final Map<String, ObjectType> objectTypes = new HashMap<>();

    protected Schema() {
    }

    protected void addObjectType(ObjectType objectType) {
        objectTypes.put(objectType.getName(), objectType);
    }

    protected Map<String, ObjectType> getObjectTypes() {
        return objectTypes;
    }

    public Stream<? extends ObjectType> getAllObjectTypes() {
        return objectTypes.values().stream();
    }

    public ObjectType getObjectTypeByName(String name) {
        return objectTypes.get(name);
    }
}
