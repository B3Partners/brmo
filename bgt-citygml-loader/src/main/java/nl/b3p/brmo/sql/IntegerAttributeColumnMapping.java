package nl.b3p.brmo.sql;

public class IntegerAttributeColumnMapping extends AttributeColumnMapping {
    public IntegerAttributeColumnMapping(String name, boolean notNull, boolean primaryKey) {
        super(name, "integer", notNull, primaryKey);
    }

    public IntegerAttributeColumnMapping(String name, boolean notNull) {
        super(name, "integer", notNull, false);
    }

    public IntegerAttributeColumnMapping(String name) {
        this(name, true);
    }

    @Override
    public Object toQueryParameter(Object value) throws Exception {
        if(value == null) {
            return null;
        } else {
            return Integer.parseInt(value.toString());
        }
    }
}
