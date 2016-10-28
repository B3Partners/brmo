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
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Boy de Wit
 * @autor mprins
 */
public class BrkToStagingToRsgbIntegrationTest /* extends AbstractDatabaseIntegrationTest */ {

    private final String testFolder = "/home/boy/dev/projects/rsgb/testbestanden/";
    private final String fileNaamStand = "BRK_KLEIN_SNAPSHOT.xml";
    private final String fileNaamMutatie = "BRK_MUTBX01.xml";

    private Connection connStaging;
    private Connection connRsgb;

    private BrmoFramework brmo;

//    @Before
    public void setUp() throws BrmoException, SQLException {
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
    public void testBrkStandToStaging() throws BrmoException {
        brmo.loadFromFile("brk", testFolder + fileNaamStand);
        List<LaadProces> processen = brmo.listLaadProcessen();
        assert (processen != null && processen.size() == 1) : "Er moet 1 proces zijn.";
    }

//    @Test
    public void testBrkMutatieToStaging() throws BrmoException {
        brmo.loadFromFile("brk", testFolder + fileNaamMutatie);
        List<LaadProces> processen = brmo.listLaadProcessen();
        assert (processen != null && processen.size() == 2) : "Er moeten 2 proces zijn.";
    }

//    @Test
    /*
    public void testStandToDbXml() throws BrmoException {
        Long laadProcesId = brmo.getLaadProcesIdByFileName(fileNaamStand);

        brmo.toDbXml(laadProcesId);

        List<Bericht> berichten = brmo.getBerichtenByLaadProcesId(laadProcesId);

        for (Bericht ber : berichten) {
            assert(ber != null && ber.getDbXml() != null) : "Db xml moet gevuld zijn!";
        }
    }*/
//    @Test
    /*
    public void testMutatieToDbXml() throws BrmoException {
        Long laadProcesId = brmo.getLaadProcesIdByFileName(fileNaamMutatie);

        brmo.toDbXml(laadProcesId);

        List<Bericht> berichten = brmo.getBerichtenByLaadProcesId(laadProcesId);

        for (Bericht ber : berichten) {
            assert(ber != null && ber.getDbXml() != null) : "Db xml moet gevuld zijn!";
        }
    }*/
//    @Test
    /*
    public void testStandToRsgb() throws BrmoException {
        Long laadProcesId = brmo.getLaadProcesIdByFileName(fileNaamStand);

        brmo.toRsgb(laadProcesId);

        List<Bericht> berichten = brmo.getBerichtenByLaadProcesId(laadProcesId);

        for (Bericht ber : berichten) {
            assert(ber != null && ber.getStatus() == Bericht.STATUS.RSGB_OK) : "Berichtstatus moet RSGB_OK zijn!";
        }
    }*/
//    @Test
    /*
    public void testMutatieToRsgb() throws BrmoException {
        Long laadProcesId = brmo.getLaadProcesIdByFileName(fileNaamMutatie);

        brmo.toRsgb(laadProcesId);

        List<Bericht> berichten = brmo.getBerichtenByLaadProcesId(laadProcesId);

        for (Bericht ber : berichten) {
            assert(ber != null && ber.getStatus() == Bericht.STATUS.RSGB_OK) : "Berichtstatus moet RSGB_OK zijn!";
        }
    }
     */
}
