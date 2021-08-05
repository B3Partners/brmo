/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bgt.schema;

import nl.b3p.brmo.schema.ObjectType;
import nl.b3p.brmo.schema.Schema;
import nl.b3p.brmo.schema.mapping.AttributeColumnMapping;

import java.util.List;

public class BGTObjectType extends ObjectType {
    private boolean isIMGeoPlusType;

    BGTObjectType(Schema schema, String name, List<AttributeColumnMapping> attributes) {
        super(schema, name, attributes);
        this.isIMGeoPlusType = !BGTSchema.bgtObjectTypes.contains(name);
    }

    public boolean isIMGeoPlusType() {
        return isIMGeoPlusType;
    }
}
