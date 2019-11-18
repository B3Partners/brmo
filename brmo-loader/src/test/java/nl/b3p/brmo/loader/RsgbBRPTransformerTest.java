 package nl.b3p.brmo.loader;

import java.io.StringWriter;
import org.apache.commons.io.IOUtils;
import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author meine
 */


public class RsgbBRPTransformerTest {
    
    @Test
    @Ignore("test bestanden van meine ontbreken")
    public void mergeTest() throws Exception{
        StringWriter writerOld = new StringWriter();
        IOUtils.copy(RsgbBRPTransformerTest.class.getResourceAsStream("old.xml"), writerOld, "UTF-8");
        String old = writerOld.toString();
        
        StringWriter writerNew = new StringWriter();
        IOUtils.copy(RsgbBRPTransformerTest.class.getResourceAsStream("new.xml"), writerNew, "UTF-8");
        String newFile = writerNew.toString();
        
        Document doc = RsgbBRPTransformer.merge( old, newFile);
        NodeList data = doc.getElementsByTagName("data");
        assertEquals("Too many data elems", 1,data.getLength());
        Node dataNode = data.item(0);
        //assertEquals("Too many children in data", 6, dataChildren.getLength());
        NodeList huw = doc.getElementsByTagName("huw_ger_partn");
        assertEquals("Huwelijk not present", 1, huw.getLength());
        NodeList subjectList = doc.getElementsByTagName("subject");
        assertEquals("too many subjects", 1, subjectList.getLength());
        Element subject = (Element)subjectList.item(0);
        assertEquals("new value in adres_buitenland not merged", "daaro", subject.getElementsByTagName("adres_buitenland").item(0).getTextContent() );
        assertEquals("old value in pa_postbus__of_antwoordnummer not retained", "666", subject.getElementsByTagName("pa_postbus__of_antwoordnummer").item(0).getTextContent() );
    }
}
