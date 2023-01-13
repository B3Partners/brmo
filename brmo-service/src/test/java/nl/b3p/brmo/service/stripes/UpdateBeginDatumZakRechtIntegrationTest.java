package nl.b3p.brmo.service.stripes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import net.sourceforge.stripes.action.ActionBeanContext;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.service.testutil.TestUtil;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.jdbc.util.converter.OracleConnectionUnwrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Testcases voor updaten van zak_recht.begind_recht tabel
 *
 * <p>Draaien met: {@code mvn -Dit.test=UpdateBeginDatumZakRechtIntegrationTest -Dtest.onlyITs=true
 * verify -Poracle > target/oracle.log} voor bijvoorbeeld Oracle of {@code mvn
 * -Dit.test=UpdateBeginDatumZakRechtIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql >
 * target/postgresql.log}
 *
 * @author meine
 */
public class UpdateBeginDatumZakRechtIntegrationTest extends TestUtil {

    private static final Log LOG = LogFactory.getLog(UpdateBeginDatumZakRechtIntegrationTest.class);
    private final String sBestandsNaam = "/GH557-brk-comfort-adres/staging-flat.xml";
    private final String rBestandsNaam = "/GH557-brk-comfort-adres/rsgb-flat.xml";
    private final Lock sequential = new ReentrantLock();
    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private BrmoFramework brmo;
    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;
    private UpdatesActionBean bean;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        // mock de context van de bean
        ServletContext sctx = mock(ServletContext.class);
        ActionBeanContext actx = mock(ActionBeanContext.class);
        bean = spy(UpdatesActionBean.class);
        when(bean.getContext()).thenReturn(actx);
        when(actx.getServletContext()).thenReturn(sctx);

        assumeTrue(
                UpdateBeginDatumZakRechtIntegrationTest.class.getResource(sBestandsNaam) != null,
                "Het bestand met staging testdata zou moeten bestaan.");
        assumeTrue(
                UpdateBeginDatumZakRechtIntegrationTest.class.getResource(rBestandsNaam) != null,
                "Het bestand met rsgb testdata zou moeten bestaan.");

        if (isOracle) {
            rsgb =
                    new DatabaseConnection(
                            OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()),
                            DBPROPS.getProperty("rsgb.schema").toUpperCase());
            rsgb.getConfig()
                    .setProperty(
                            DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                            new Oracle10DataTypeFactory());
            rsgb.getConfig()
                    .setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
            staging =
                    new DatabaseConnection(
                            OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()),
                            DBPROPS.getProperty("staging.schema").toUpperCase());
            staging.getConfig()
                    .setProperty(
                            DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                            new Oracle10DataTypeFactory());
            staging.getConfig()
                    .setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (isPostgis) {
            rsgb =
                    new DatabaseConnection(
                            dsRsgb.getConnection(), DBPROPS.getProperty("rsgb.schema"));
            rsgb.getConfig()
                    .setProperty(
                            DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                            new PostgresqlDataTypeFactory());
            staging = new DatabaseConnection(dsStaging.getConnection());
            staging.getConfig()
                    .setProperty(
                            DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
                            new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegegeven");
        }

        staging.getConfig().setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);
        rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet =
                fxdb.build(
                        new FileInputStream(
                                new File(
                                        UpdateBeginDatumZakRechtIntegrationTest.class
                                                .getResource(sBestandsNaam)
                                                .toURI())));
        IDataSet rsgbDataSet =
                fxdb.build(
                        new FileInputStream(
                                new File(
                                        UpdateBeginDatumZakRechtIntegrationTest.class
                                                .getResource(rBestandsNaam)
                                                .toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        DatabaseOperation.CLEAN_INSERT.execute(rsgb, rsgbDataSet);

        brmo = new BrmoFramework(dsStaging, dsRsgb, dsRsgbBrk);
        brmo.setOrderBerichten(true);

        assumeTrue(1 == brmo.getCountBerichten("brk", "RSGB_OK"), "Er zijn x RSGB_OK berichten");
        assumeTrue(
                1 == brmo.getCountLaadProcessen("brk", "STAGING_OK"),
                "Er zijn x STAGING_OK laadprocessen");
    }

    @AfterEach
    public void cleanup() throws Exception {
        if (brmo != null) {
            // in geval van niet waar gemaakte assumptions
            brmo.closeBrmoFramework();
            brmo = null;
        }
        bean = null;
        if (rsgb != null) {
            CleanUtil.cleanRSGB_BRK(rsgb, true);
            rsgb.close();
            rsgb = null;
        }
        if (staging != null) {
            CleanUtil.cleanSTAGING(staging, true);
            staging.close();
            staging = null;
        }
        try {
            sequential.unlock();
        } catch (IllegalMonitorStateException e) {
            // in geval van niet waar gemaakte assumptions
            LOG.debug(
                    "unlock van thread is mislukt, mogelijk niet ge-lock-ed of test overgeslagen.");
        }
    }

    @Test
    public void runUpdate() throws Exception {
        ITable zak_recht = rsgb.createDataSet().getTable("zak_recht");
        assertEquals(6, zak_recht.getRowCount(), "Aantal zakelijk rechten klopt niet");
        /*
        eerst checken of de datum leeg is, anders klopt de testdata niet
         */
        for (int i = 0; i < 6; i++) {
            assertNull(zak_recht.getValue(i, "ingangsdatum_recht"), "Ingangsdatum recht is gevuld");
        }

        bean.populateUpdateProcesses();
        bean.setUpdateProcessName("Bijwerken ingangsdatum_recht zakelijk recht");
        bean.update();

        zak_recht = rsgb.createDataSet().getTable("zak_recht");
        assertEquals(6, zak_recht.getRowCount(), "Aantal zakelijk rechten klopt niet");

        for (int i = 0; i < 6; i++) {
            assertNotNull(
                    zak_recht.getValue(i, "ingangsdatum_recht"),
                    "Ingangsdatum recht is niet gevuld");
            assertNotNull(
                    zak_recht.getValue(i, "fk_3avr_aand"), "fk_3avr_aand recht is niet gevuld");
        }

        // check of de db_xml ook is bijgewerkt
        ITable bericht = staging.createDataSet().getTable("bericht");
        DocumentBuilder builder = factory.newDocumentBuilder();
        for (int i = 0; i < bericht.getRowCount(); i++) {
            String db_xml = bericht.getValue(i, "db_xml").toString();
            if (db_xml.contains("zak_recht")) {
                Document doc = builder.parse(new InputSource(new StringReader(db_xml)));
                NodeList zakRechten = doc.getElementsByTagName("zak_recht");
                for (int z = 0; i < zakRechten.getLength(); i++) {
                    Element zr = (Element) zakRechten.item(z);
                    assertNotNull(
                            zr.getElementsByTagName("ingangsdatum_recht"),
                            "Zakelijk recht heeft geen ingangsdatum");
                    assertNotNull(
                            zr.getElementsByTagName("ingangsdatum_recht").item(0).getTextContent(),
                            "Zakelijk recht heeft geen geldige ingangsdatum waarde");
                }
            }
        }
    }
}
