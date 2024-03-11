/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 */

package nl.b3p.brmo.schema.mapping;

public class BooleanAttributeColumnMapping extends AttributeColumnMapping {
  public BooleanAttributeColumnMapping(String name, boolean notNull) {
    super(name, "boolean", notNull, false);
  }

  public BooleanAttributeColumnMapping(String name) {
    this(name, true);
  }

  // Do not override toQueryParameter() to set boolean parameter as a Boolean object but as string, so for Oracle 'true' or 'false' for a varchar2 column is set. For PostgreSQL this will still use a boolean column, accepting a string parameter.
}
