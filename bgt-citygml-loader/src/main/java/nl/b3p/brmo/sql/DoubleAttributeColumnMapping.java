package nl.b3p.brmo.sql;

public class DoubleAttributeColumnMapping  extends AttributeColumnMapping {

    public DoubleAttributeColumnMapping(String name, boolean notNull) {
        super(name, "double precision", notNull, false);
    }

    public DoubleAttributeColumnMapping(String name) {
        this(name, true);
    }

    @Override
    public Object toQueryParameter(Object value) throws Exception {
        if(value == null) {
            return null;
        } else {
            return Double.parseDouble(value.toString());
        }
    }
}