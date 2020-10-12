 package nl.b3p.brmo.loader;

 import org.apache.commons.io.IOUtils;
 import org.junit.jupiter.api.Disabled;
 import org.junit.jupiter.api.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;

 import java.io.StringWriter;

 import static org.junit.jupiter.api.Assertions.assertEquals;

 /**
 *
 * @author meine
 */
public class RsgbBRPTransformerTest {
    
    @Test
    @Disabled("test bestanden van meine ontbreken")
    public void mergeTest() throws Exception{
        StringWriter writerOld = new StringWriter();
        IOUtils.copy(RsgbBRPTransformerTest.class.getResourceAsStream("old.xml"), writerOld, "UTF-8");
        String old = writerOld.toString();
        
        StringWriter writerNew = new StringWriter();
        IOUtils.copy(RsgbBRPTransformerTest.class.getResourceAsStream("new.xml"), writerNew, "UTF-8");
        String newFile = writerNew.toString();
        
        Document doc = RsgbBRPTransformer.merge( old, newFile);
        NodeList data = doc.getElementsByTagName("data");
        assertEquals(1,data.getLength(), "Too many data elems");
        Node dataNode = data.item(0);
        //assertEquals("Too many children in data", 6, dataChildren.getLength());
        NodeList huw = doc.getElementsByTagName("huw_ger_partn");
        assertEquals(1, huw.getLength(), "Huwelijk not present");
        NodeList subjectList = doc.getElementsByTagName("subject");
        assertEquals(1, subjectList.getLength(), "too many subjects");
        Element subject = (Element)subjectList.item(0);
        assertEquals("daaro", subject.getElementsByTagName("adres_buitenland").item(0).getTextContent(),
                "new value in adres_buitenland not merged");
        assertEquals(
                "666", subject.getElementsByTagName("pa_postbus__of_antwoordnummer").item(0).getTextContent(),
                "old value in pa_postbus__of_antwoordnummer not retained");
    }
}
