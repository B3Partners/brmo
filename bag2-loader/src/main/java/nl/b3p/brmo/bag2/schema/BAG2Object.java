/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.bag2.schema;

import nl.b3p.brmo.schema.ObjectType;
import nl.b3p.brmo.schema.SchemaObjectInstance;

import java.util.Map;

import static nl.b3p.brmo.bag2.schema.BAG2Schema.ARCHIVE_SUFFIX;
import static nl.b3p.brmo.bag2.schema.BAG2Schema.EIND_REGISTRATIE;

public class BAG2Object extends SchemaObjectInstance {
    public BAG2Object(BAG2ObjectType objectType, Map<String, Object> attributes) {
        // XXX Hier of elders
        super(attributes.get(EIND_REGISTRATIE) == null ? objectType : BAG2Schema.getInstance().getObjectTypeByName(objectType.getName() + ARCHIVE_SUFFIX), attributes);
    }
}
