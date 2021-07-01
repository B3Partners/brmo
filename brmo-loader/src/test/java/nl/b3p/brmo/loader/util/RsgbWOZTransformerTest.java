package nl.b3p.brmo.loader.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RsgbWOZTransformerTest {
    private static final Log LOG = LogFactory.getLog(RsgbWOZTransformerTest.class);

    @Test
    public void mergeTest1() throws Exception {
        StringWriter writerOld = new StringWriter();
        IOUtils.copy(RsgbWOZTransformerTest.class.getResourceAsStream("old-woz-1.xml"), writerOld, "UTF-8");
        String old = writerOld.toString();

        StringWriter writerNew = new StringWriter();
        IOUtils.copy(RsgbWOZTransformerTest.class.getResourceAsStream("new-woz-1.xml"), writerNew, "UTF-8");
        String newFile = writerNew.toString();

        Document doc = RsgbWOZTransformer.merge(old, newFile);
        String mergedDoc = RsgbWOZTransformer.print(doc);
        LOG.debug("merged document: \n" + mergedDoc);

        NodeList data = doc.getElementsByTagName("data");
        assertEquals(1, data.getLength(), "Too many data elems");

        Node dataNode = data.item(0);

        NodeList subjectList = doc.getElementsByTagName("subject");
        assertEquals(1, subjectList.getLength(), "aantal subject nodes klopt niet");

        NodeList ingeschr_nat_prs_list = doc.getElementsByTagName("ingeschr_nat_prs");
        assertEquals(1, ingeschr_nat_prs_list.getLength(), "aantal ingeschr_nat_prs nodes klopt niet");

        Element ingeschr_nat_prs = (Element) ingeschr_nat_prs_list.item(0);
        // 1e bericht zet "DienstberichtKerkpad 29, 8510AA DienstberichtDorp", update heeft "geenWaarde"
        assertEquals("",
                ingeschr_nat_prs.getElementsByTagName("va_loc_beschrijving").item(0).getTextContent(),
                "va_loc_beschrijving klopt niet");

    }
}
