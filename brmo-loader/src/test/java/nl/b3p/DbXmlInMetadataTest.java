package nl.b3p;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author Boy de Wit
 */
public class DbXmlInMetadataTest {

    private final String testFolder = "/home/boy/dev/projects/rsgb/testbestanden/";
    private final String fileName = "brk_dbxml_metadata.xml";

    private File fileDbXml;

    private BrmoFramework brmo;

//    @Before
    public void setUpClass() throws BrmoException, SQLException {
        /* TODO: HSQLDB, http://hsqldb.org for in-memory database test objects */

        fileDbXml = new File(testFolder + fileName);

        String rsgbUrl = "jdbc:postgresql://kx1/rsgb";
        Properties rsgbProps = new Properties();
        rsgbProps.setProperty("user", "rsgb");
        rsgbProps.setProperty("password", "rsgb");

//        connRsgb = DriverManager.getConnection(rsgbUrl, rsgbProps);
            // TODO ombouw naar datasource niet getest
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl(rsgbUrl);
            ds.setConnectionProperties(rsgbProps.toString());

        brmo = new BrmoFramework(null, ds);
    }

//    @Test
    /*
    public void testAuthAlreadyInMetadataTable() throws BrmoException {
        List<TableData> list = new ArrayList();

        try {
            CountingInputStream cis = new CountingInputStream(new FileInputStream(fileDbXml));
            list = DataComfortXMLReader.readDataXML(new StreamSource(cis));
        } catch (Exception ex) {
            System.out.println("Foutje..." + ex.getLocalizedMessage());
        }

        for (TableData data : list) {
            for (TableRow row : data.getRows()) {

                boolean inMeta = brmo.rowInRsgbMetadata(row);

                assert(inMeta) : "Should be in herkomst_metadata table!";
            }
        }
    }
    */
}
