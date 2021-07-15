package nl.b3p.brmo.sql;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleDateFormatAttributeColumnMapping extends AttributeColumnMapping {
    public static final String PATTERN_XML_DATE = "yyyy-MM-dd";
    public static final String PATTERN_XML_TIMESTAMP = "yyyy-MM-dd'T'HH:mm:ss";

    private final SimpleDateFormat dateFormat;

    public SimpleDateFormatAttributeColumnMapping(String name, String type, boolean notNull, boolean primaryKey, String dateFormatPattern) {
        super(name, type, notNull, primaryKey);
        dateFormat = new SimpleDateFormat(dateFormatPattern);
    }

    public SimpleDateFormatAttributeColumnMapping(String name, String type, boolean notNull, String dateFormatPattern) {
        this(name, type, notNull, false, dateFormatPattern);
    }

    public SimpleDateFormatAttributeColumnMapping(String name, String type, String dateFormatPattern) {
        this(name, type, true, false, dateFormatPattern);
    }

    @Override
    public Object toQueryParameter(Object value) throws ParseException {
        if(value == null) {
            return null;
        } else {
            Date date = dateFormat.parse(value.toString());
            if ("timestamp".equals(getType())) {
                return new java.sql.Timestamp(date.getTime());
            } else {
                return new java.sql.Date(date.getTime());
            }
        }
    }
}
