package nl.b3p.brmo.loader.entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Matthijs Laan
 */
public class NhrPeildatumAdapter extends XmlAdapter<String, Date> {
    public static final String NHR_PEILDATUM_FORMAT = "yyyyMMddHHmmssSSS";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(NHR_PEILDATUM_FORMAT);

    @Override
    public String marshal(Date v) throws Exception {
        synchronized (dateFormat) {
            return dateFormat.format(v);
        }
    }

    @Override
    public Date unmarshal(String v) throws Exception {
        synchronized (dateFormat) {
            return dateFormat.parse(v);
        }
    }
}
