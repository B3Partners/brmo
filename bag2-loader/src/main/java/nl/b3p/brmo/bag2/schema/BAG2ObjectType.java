/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.schema;

import nl.b3p.brmo.schema.ObjectType;
import nl.b3p.brmo.schema.Schema;
import nl.b3p.brmo.schema.mapping.AttributeColumnMapping;

import java.util.List;

public class BAG2ObjectType extends ObjectType {
    public BAG2ObjectType(Schema schema, String name, List<AttributeColumnMapping> attributes) {
        super(schema, name, attributes);
    }
}
