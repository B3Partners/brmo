/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.schema.mapping;

import nl.b3p.brmo.sql.dialect.SQLDialect;

public class DoubleAttributeColumnMapping extends AttributeColumnMapping {

  public DoubleAttributeColumnMapping(String name, boolean notNull) {
    super(name, "double precision", notNull, false);
  }

  public DoubleAttributeColumnMapping(String name) {
    this(name, true);
  }

  @Override
  public Object toQueryParameter(Object value, SQLDialect sqlDialect) {
    if (value == null) {
      return null;
    } else {
      return Double.parseDouble(value.toString());
    }
  }
}
