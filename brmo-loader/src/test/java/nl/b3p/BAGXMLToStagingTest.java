package nl.b3p;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author Boy de Wit
 */
public class BAGXMLToStagingTest {

    private final String testFolder = "/home/boy/dev/projects/rsgb/testbestanden/";
    private final String fileNaam = "BAG_Voorbeeld.xml";

    private Connection connStaging;
    private Connection connRsgb;

    private BrmoFramework brmo;

//    @Before
    public void setUpClass() throws BrmoException, SQLException {
        /* TODO: HSQLDB, http://hsqldb.org for in-memory database test objects */

        String stagingUrl = "jdbc:postgresql://kx1/staging";
        Properties stagingProps = new Properties();
        stagingProps.setProperty("user", "staging");
        stagingProps.setProperty("password", "staging");

        String rsgbUrl = "jdbc:postgresql://kx1/rsgb";
        Properties rsgbProps = new Properties();
        rsgbProps.setProperty("user", "rsgb");
        rsgbProps.setProperty("password", "rsgb");

//        connStaging = DriverManager.getConnection(stagingUrl, stagingProps);
            // TODO ombouw naar datasource niet getest
            BasicDataSource dsStaging = new BasicDataSource();
            dsStaging.setUrl(stagingUrl);
            dsStaging.setConnectionProperties(stagingProps.toString());
//        connRsgb = DriverManager.getConnection(rsgbUrl, rsgbProps);
            // TODO ombouw naar datasource niet getest
            BasicDataSource dsRsgb = new BasicDataSource();
            dsRsgb.setUrl(rsgbUrl);
            dsRsgb.setConnectionProperties(rsgbProps.toString());

        brmo = new BrmoFramework(dsStaging, dsRsgb);
    }

//    @Test
    public void emptyStagingDb() throws BrmoException {
        brmo.emptyStagingDb();

        List<LaadProces> processen = brmo.listLaadProcessen();

        assert (processen != null && processen.size() == 0) : "Table " + BrmoFramework.LAADPROCES_TABEL + " should be empty!";

        List<Bericht> berichten = brmo.listBerichten();

        assert (berichten != null && berichten.size() == 0) : "Table " + BrmoFramework.BERICHT_TABLE + " should be empty!";
    }

//    @Test
    public void testBagStandToStaging() throws BrmoException {
        brmo.loadFromFile("bag", testFolder + fileNaam);

        List<LaadProces> processen = brmo.listLaadProcessen();

        assert(processen != null && processen.size() == 1) : "Er moet 1 proces zijn.";
    }
}
