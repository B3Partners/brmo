/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.schema.mapping;

import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;

public class BooleanAttributeColumnMapping extends AttributeColumnMapping {
  public BooleanAttributeColumnMapping(String name, boolean notNull) {
    super(name, "boolean", notNull, false);
  }

  public BooleanAttributeColumnMapping(String name) {
    this(name, true);
  }

  @Override
  public Object toQueryParameter(Object value, SQLDialect sqlDialect) throws Exception {
    if (sqlDialect instanceof OracleDialect) {
      // Use default string to have 'true' and 'false' in nvarchar2(5) column
      return super.toQueryParameter(value, sqlDialect);
    } else {
      if (value == null) {
        return null;
      } else {
        return Boolean.parseBoolean(value.toString());
      }
    }
  }
}
