package nl.b3p.brmo.loader.util;

import org.geotools.gml.stream.XmlStreamGeometryReader;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.locationtech.jts.geom.Geometry;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Boy de Wit
 */
public class DataComfortXMLReader {

    private static final int LEVEL_ROOT = 0;
    private static final int LEVEL_DATA = 1;
    private static final int LEVEL_COMFORT = 2;
    private static final int LEVEL_TABLE = 3;
    private static final int LEVEL_DELETE = 4;

    private final TransformerFactory tf = TransformerFactory.newInstance();
    private final XMLInputFactory xif = XMLInputFactory.newInstance();

    public DataComfortXMLReader() {
        xif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE); // Coalesce characters
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE); // No XML entity expansions or external entities
    }

    public List<TableData> readDataXML(Source dataXML) throws Exception {
        Split split = SimonManager.getStopwatch("b3p.util.datacomfortxmlreader").start();

        XMLStreamReader xer = xif.createXMLStreamReader(dataXML);
        XmlStreamGeometryReader geometryReader = new XmlStreamGeometryReader(xer);

        int level = LEVEL_ROOT;
        List<TableData> list = new ArrayList();
        TableData data = null;
        TableRow row = null;
        boolean inComfortData = false;
        boolean inDeleteData = false;

        root:
        while (xer.hasNext()) {
            xer.nextTag();
            String tag = xer.getLocalName();
            switch (level) {
                case LEVEL_ROOT:
                    if ("data".equals(tag)) {
                        level = LEVEL_DATA;
                    }
                    break;

                case LEVEL_DATA:
                    if (xer.isEndElement()) {
                        break root;
                    }

                    if ("comfort".equals(tag)) {
                        String comfortSearchTable = xer.getAttributeValue(null, "search-table");
                        String comfortSearchColumn = xer.getAttributeValue(null, "search-column");
                        String comfortSearchValue = xer.getAttributeValue(null, "search-value");
                        String snapshotDate = xer.getAttributeValue(null, "snapshot-date");

                        data = new TableData(comfortSearchTable, comfortSearchColumn, comfortSearchValue, snapshotDate);

                        level = LEVEL_COMFORT;

                        inComfortData = true;
                        inDeleteData = false;

                    } else if ("delete".equals(tag)) {
    
                        data = new TableData();
                        
                        level = LEVEL_DELETE;
                        
                        inComfortData = false;
                        inDeleteData = true;
                        
                    } else {
                        row = new TableRow();
                        row.setTable(tag);
                        row.setIgnoreDuplicates("yes".equals(xer.getAttributeValue(null, "ignore-duplicates")));

                        String beginDatumColumn = xer.getAttributeValue(null, "column-dat-beg-geldh");
                        String eindeDatumColumn = xer.getAttributeValue(null, "column-datum-einde-geldh");

                        row.setColumnDatumBeginGeldigheid(beginDatumColumn);
                        row.setColumnDatumEindeGeldigheid(eindeDatumColumn);

                        data = new TableData(row);

                        level = LEVEL_TABLE;

                        inComfortData = false;
                        inDeleteData = false;
                    }

                    list.add(data);

                    break;

                case LEVEL_COMFORT:

                    if (xer.isEndElement() && "comfort".equals(tag)) {
                        level = LEVEL_DATA;
                    } else {
                        row = new TableRow();
                        row.setTable(tag);

                        data.addRow(row);

                        level = LEVEL_TABLE;
                    }

                    break;

                case LEVEL_DELETE:
                    
                    if (xer.isEndElement() && "delete".equals(tag)) {
                        level = LEVEL_DATA;
                    } else {
                        row = new TableRow();
                        row.setTable(tag);

                        data.addRow(row);

                        level = LEVEL_TABLE;
                    }

                    break;
                   
                case LEVEL_TABLE:
                    if (xer.isEndElement()) {

                        if (inComfortData) {
                            level = LEVEL_COMFORT;
                        } else if (inDeleteData) {
                            level = LEVEL_DELETE;
                        } else {
                            level = LEVEL_DATA;
                        }
                    } else {
                        data = list.get(list.size() - 1);

                        row.getColumns().add(tag);

                        String alleenArchief = xer.getAttributeValue(null, "alleen-archief");
                        if("true".equals(alleenArchief)) {
                            row.setAlleenArchiefColumn(tag);
                        }

                        // Detecteer XML elementen of text
                        xer.next();
                        // Skip whitespace before a possible element
                        while(xer.isWhiteSpace()) {
                            xer.next();
                        }
                        if (xer.isStartElement()) {
                            Split split2 = SimonManager.getStopwatch("b3p.util.datacomfortxmlreader.parsegml").start();

                            Geometry geom = geometryReader.readGeometry();

                            // Note: this linearizes curves, we could use a WKTWriter2 instead!
                            row.getValues().add(geom.toString());

                            //System.out.println("Sub elements " + tag + ": " + sw.toString());
                            split2.stop();
                        } else if (xer.isCharacters()) {
                            StringBuilder t = new StringBuilder();
                            do {
                                t.append(xer.getText());
                                xer.next();
                            } while (xer.isCharacters());
                            row.getValues().add(t.toString());
                        } else {
                            assert (xer.isEndElement());
                            row.getValues().add(null);
                        }
                    }

                    break;
            }
        }

        split.stop();
        return list;
    }
}
