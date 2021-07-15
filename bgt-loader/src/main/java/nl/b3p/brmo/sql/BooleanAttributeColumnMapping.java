package nl.b3p.brmo.sql;

public class BooleanAttributeColumnMapping extends AttributeColumnMapping {
    public BooleanAttributeColumnMapping(String name, boolean notNull) {
        super(name, "boolean", notNull, false);
    }

    public BooleanAttributeColumnMapping(String name) {
        this(name, true);
    }

    @Override
    public Object toQueryParameter(Object value) throws Exception {
        if(value == null) {
            return null;
        } else {
            return Boolean.parseBoolean(value.toString());
        }
    }
}
