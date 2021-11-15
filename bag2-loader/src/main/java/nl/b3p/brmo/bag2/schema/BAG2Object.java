/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.schema;

import nl.b3p.brmo.schema.SchemaObjectInstance;

import java.util.Map;

public class BAG2Object extends SchemaObjectInstance {
    public BAG2Object(BAG2ObjectType objectType, Map<String, Object> attributes) {
        super(objectType, attributes);
    }

    public BAG2ObjectType getObjectType() {
        return (BAG2ObjectType) super.getObjectType();
    }

}
