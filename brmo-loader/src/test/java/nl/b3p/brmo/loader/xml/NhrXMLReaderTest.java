package nl.b3p.brmo.loader.xml;

import java.util.ArrayList;
import java.util.List;
import nl.b3p.brmo.loader.entity.NhrBericht;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Matthijs Laan
 */
public class NhrXMLReaderTest {
    @Test
    public void testMaatschappelijkeActiviteit() throws Exception {
        NhrXMLReader r;

        r = new NhrXMLReader(NhrXMLReader.class.getResourceAsStream("nhr-maatschappelijkeActiviteit.xml"));
        assertTrue(r.hasNext());

        int total = 0;
        List<String> objectRefs = new ArrayList();
        while (r.hasNext()) {
            NhrBericht b = r.next();
            objectRefs.add(b.getObjectRef());
            System.out.println(String.format("Bericht #%d van %tF %2$tT, object ref %s",
                    b.getVolgordeNummer(),
                    b.getDatum(),
                    b.getObjectRef())
            );
            total++;
        }
        assertEquals(3, total);
        assertArrayEquals(new String[] {
            "nhr.maatschAct.kvk.16029104",
            "nhr.comVestg.000002706229",
            "nhr.rechtspersoon.rsin.001681965"
        }, objectRefs.toArray(new String[] {}));
    }
}
