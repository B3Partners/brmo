package nl.b3p.brmo.loader.xml;

import java.io.InputStream;
import nl.b3p.brmo.loader.entity.Bericht;

/**
 *
 * @author Matthijs Laan
 */
public class NhrXMLReader extends BrmoXMLReader {

    public NhrXMLReader(InputStream in) throws Exception {
    }

    @Override
    public void init() throws Exception {
    }

    @Override
    public boolean hasNext() throws Exception {
        return false;
    }

    @Override
    public Bericht next() throws Exception {
        return null;
    }
}
