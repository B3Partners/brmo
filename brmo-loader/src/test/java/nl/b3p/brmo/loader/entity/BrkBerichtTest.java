package nl.b3p.brmo.loader.entity;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import nl.b3p.brmo.loader.xml.BrkSnapshotXMLReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author meine
 */


public class BrkBerichtTest {
        private static final Log LOG = LogFactory.getLog(BrkBerichtTest.class);

    private BrkSnapshotXMLReader bReader;

    private static final String B = "/nl/b3p/brmo/loader/xml/xml/MUTBX01-ASN00T1660-20091119-1-prettyprinted.xml";

    @After
    public void cleanup() throws Exception {
        bReader = null;
    }

    @Test
    public void dateTimeUnequalTest() throws Exception {
        InputStream is = BrkBerichtTest.class.getResourceAsStream(B);
        bReader = new BrkSnapshotXMLReader(is);
        assertTrue(bReader.hasNext());
        BrkBericht b = bReader.next();

        LOG.debug("bericht 1: " + b);
        LaadProces lp = new LaadProces(); 
        SimpleDateFormat output = new SimpleDateFormat("yyyyMMdd");
        String real = b.getRestoredFileName(output.parse("20091119"), 1);
        String expected = "BKE-MUTBX01-ASN00T1660-20091119-1.zip";
        assertEquals(expected,real);
    }

}
