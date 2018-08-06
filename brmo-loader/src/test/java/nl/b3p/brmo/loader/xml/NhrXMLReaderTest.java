package nl.b3p.brmo.loader.xml;

import java.util.ArrayList;
import java.util.List;
import nl.b3p.brmo.loader.entity.NhrBericht;
import static org.junit.Assert.*;

import org.junit.Ignore;
import org.junit.Test;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 *
 * @author Matthijs Laan
 */
public class NhrXMLReaderTest {

    private static final Log LOG = LogFactory.getLog(NhrXMLReaderTest.class);

    @Test
    @Ignore("dit is HR dataservice v2.5 data; dat wordt niet langer ondersteund")
    public void testMaatschappelijkeActiviteit() throws Exception {
        NhrXMLReader r;

        r = new NhrXMLReader(NhrXMLReader.class.getResourceAsStream("nhr-2.5.xml"));
        assertTrue(r.hasNext());

        int total = 0;
        List<String> objectRefs = new ArrayList();
        while (r.hasNext()) {
            NhrBericht b = r.next();
            objectRefs.add(b.getObjectRef());
            LOG.debug(String.format("Bericht #%d van %tF %2$tT, object ref %s",
                    b.getVolgordeNummer(),
                    b.getDatum(),
                    b.getObjectRef())
            );
            total++;
        }
        assertEquals(3, total);

        assertArrayEquals(new String[] {
            "nhr.rechtspersoon.rsin.808350894",
            "nhr.maatschAct.kvk.12345678",
            "nhr.comVestg.000016444663",
        }, objectRefs.toArray(new String[] {}));
    }
}
