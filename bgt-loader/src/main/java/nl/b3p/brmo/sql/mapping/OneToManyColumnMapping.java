/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.sql.mapping;

public class OneToManyColumnMapping extends AttributeColumnMapping {

    public OneToManyColumnMapping(String name) {
        super(name, null, false, false);
    }
}
