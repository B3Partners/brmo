package nl.b3p.brmo.sql;

import nl.b3p.brmo.sql.dialect.SQLDialect;

import java.util.concurrent.atomic.AtomicBoolean;

public class AttributeColumnMapping {
    private final String name;
    private final String type;
    private final boolean notNull;
    private final boolean primaryKey;

    public AttributeColumnMapping(String name, String type, boolean notNull, boolean primaryKey) {
        this.name = name;
        this.type = type;
        this.notNull = notNull;
        this.primaryKey = primaryKey;
    }

    public AttributeColumnMapping(String name, String type, boolean notNull) {
        this(name, type, notNull, false);
    }

    public AttributeColumnMapping(String name) {
        this(name, "varchar(255)", true, false);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public Object toQueryParameter(Object value) throws Exception {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
