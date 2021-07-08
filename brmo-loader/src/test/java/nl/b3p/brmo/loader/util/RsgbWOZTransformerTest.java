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

    @Test
    public void mergeTestGEom() throws Exception {
        StringWriter writerOld = new StringWriter();
        IOUtils.copy(RsgbWOZTransformerTest.class.getResourceAsStream("old-woz-geom.xml"), writerOld, "UTF-8");
        String old = writerOld.toString();

        StringWriter writerNew = new StringWriter();
        IOUtils.copy(RsgbWOZTransformerTest.class.getResourceAsStream("new-woz-geom.xml"), writerNew, "UTF-8");
        String newFile = writerNew.toString();

        Document doc = RsgbWOZTransformer.merge(old, newFile);
        String mergedDoc = RsgbWOZTransformer.print(doc);
        LOG.warn("merged document: \n" + mergedDoc);

        NodeList data = doc.getElementsByTagName("data");
        assertEquals(1, data.getLength(), "Too many data elems");

        Node dataNode = data.item(0);

        NodeList belangen = doc.getElementsByTagName("woz_belang");
        assertEquals(1, belangen.getLength(), "aantal subject nodes klopt niet");

        NodeList geomList = doc.getElementsByTagName("geom");
        assertEquals(1, geomList.getLength(), "aantal geom nodes klopt niet");

        NodeList rings = doc.getElementsByTagName("gml:LinearRing");
        assertEquals(1, rings.getLength(), "aantal gml:LinearRing nodes klopt niet");

        NodeList posList = doc.getElementsByTagName("gml:posList");
        assertEquals(1, posList.getLength(), "aantal gml:posList nodes klopt niet");
        assertEquals(
                "101046.545 439989.381 101037.316 440000.0 101033.976 440003.843 101028.315 440010.42 101015.891 440000.0 101000.0 439986.673 100999.962 439986.641 101000.0 439986.6 101006.19 439979.85 101018.809 439965.278 101032.193 439976.905 101046.545 439989.381",
                posList.item(0).getTextContent(), "posList inhoud incorrect"
        );
    }
}
