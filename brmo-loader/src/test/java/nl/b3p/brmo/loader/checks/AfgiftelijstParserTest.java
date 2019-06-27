package nl.b3p.brmo.loader.checks;

import java.net.URL;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author meine
 */


public class AfgiftelijstParserTest {
    
    public AfgiftelijstParserTest() {
    }

    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        String input = "overzicht_klein.xlsx";
        URL u = AfgiftelijstParserTest.class.getResource(input);
        String file = u.toURI().getPath();
        AfgiftelijstParser instance = new AfgiftelijstParser();
        
        List<Afgifte> result = instance.parse(file);
        assertEquals(18, result.size());
        for (Afgifte afgifte : result) {
            assertEquals("16", afgifte.getKlantnummer());
            assertEquals("9700005117", afgifte.getContractnummer());
            assertTrue(afgifte.isRapport());
            assertTrue(afgifte.isGeleverd());
            
        }
    }
    
}
