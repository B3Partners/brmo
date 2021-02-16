package nl.b3p.brmo.loader.entity;

import nl.b3p.brmo.loader.xml.BrkSnapshotXMLReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.text.SimpleDateFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author meine
 */
public class BrkBerichtTest {
        private static final Log LOG = LogFactory.getLog(BrkBerichtTest.class);

    private BrkSnapshotXMLReader bReader;

    private static final String B = "/nl/b3p/brmo/loader/xml/MUTBX01-ASN00T1660-20091119-1-prettyprinted.xml";
    

    @AfterEach
    public void cleanup() throws Exception {
        bReader = null;
    }

    @Test
    public void getRestoredFileNameTest() throws Exception {
        InputStream is = BrkBerichtTest.class.getResourceAsStream(B);
        bReader = new BrkSnapshotXMLReader(is);
        assertTrue(bReader.hasNext());
        BrkBericht b = bReader.next();

        LOG.debug("bericht 1: " + b);
        SimpleDateFormat output = new SimpleDateFormat("yyyyMMdd");
        String real = b.getRestoredFileName(output.parse("20091119"), 1);
        String expected = "BKE-MUTBX01-ASN00T1660-20091119-1.zip";
        assertEquals(expected,real);
    }
}
