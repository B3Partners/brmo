package nl.b3p;

import java.sql.Connection;
import java.sql.DriverManager;
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
public class BerichtenFilterSqlTest {

    private Connection connStaging;

    private String filterSoort;
    private String filterStatus;

    private int page;
    private int start;
    private int limit;
    private String sort;
    private String dir;

    private BrmoFramework brmo;

    private final String testFolder = "/home/boy/dev/projects/rsgb/testbestanden/";
    private final String fileNaamStand = "BRK_KLEIN_SNAPSHOT.xml";

//    @Before
    public void setUpClass() throws BrmoException, SQLException {
        /* TODO: HSQLDB, http://hsqldb.org for in-memory database test objects */

        String url = "jdbc:postgresql://kx1/staging";
        Properties props = new Properties();
        props.setProperty("user", "staging");
        props.setProperty("password", "staging");

        connStaging = DriverManager.getConnection(url, props);
//        connStaging = DriverManager.getConnection(stagingUrl, stagingProps);
            // TODO ombouw naar datasource niet getest
            BasicDataSource dsStaging = new BasicDataSource();
            dsStaging.setUrl(url);
            dsStaging.setConnectionProperties(props.toString());

        brmo = new BrmoFramework(dsStaging, null);
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
    public void testBrkStandToStaging() throws BrmoException {
        brmo.loadFromFile("brk", testFolder + fileNaamStand);

        List<LaadProces> processen = brmo.listLaadProcessen();

        assert(processen != null && processen.size() == 1) : "Er moet 1 proces zijn.";
    }

//    @Test
    public void testStatus() throws BrmoException {
        filterStatus = "STAGING_OK";
        sort = "status";

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir,
                filterSoort, filterStatus);

        assert(berichten != null && berichten.size() > 0) : "Fout testStatus.";
    }

//    @Test
    public void testSoort() throws BrmoException {
        filterSoort = "brk";
        sort = "soort";

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir,
                filterSoort, filterStatus);

        assert(berichten != null) : "Fout testSoort.";
    }

//    @Test
    public void testOrderByDesc() throws BrmoException {
        sort = "id";
        dir = "DESC";

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir,
                filterSoort, filterStatus);

        long id1 = berichten.get(0).getId();
        long id2 = berichten.get(1).getId();

        assert(berichten != null && id1 > id2) : "Fout testOrderByDesc.";
    }

//    @Test
    public void testOrderByAsc() throws BrmoException {
        sort = "id";
        dir = "ASC";

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir,
                filterSoort, filterStatus);

        long id1 = berichten.get(0).getId();
        long id2 = berichten.get(1).getId();

        assert(berichten != null && id1 < id2) : "Fout testOrderByAsc.";
    }

//    @Test
    public void testPaging() throws BrmoException {
        page = 0;
        start = 0;
        limit = 3;

        List<Bericht> berichten = brmo.getBerichten(page, start, limit, sort, dir,
                filterSoort, filterStatus);

        assert(berichten != null && berichten.size() > 0) : "Fout testPaging.";
    }
}
