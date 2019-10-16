package nl.b3p.brmo.service.stripes;


import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.ServletContext;
import net.sourceforge.stripes.action.ActionBeanContext;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.service.testutil.TestUtil;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Testcases voor updaten van zak_recht.begind_recht tabel
 *
 * Draaien met:
 * {@code mvn -Dit.test=UpdateBeginDatumZakRechtIntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor bijvoorbeeld Oracle of
 * {@code mvn -Dit.test=UpdateBeginDatumZakRechtIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * of
 * {@code mvn -Dit.test=UpdateBeginDatumZakRechtIntegrationTest -Dtest.onlyITs=true verify -Pmssql > target/mssql.log}.
 *
 * @author meine
 */
public class UpdateBeginDatumZakRechtIntegrationTest extends TestUtil{

    private static final Log LOG = LogFactory.getLog(UpdateBeginDatumZakRechtIntegrationTest.class);

    private BrmoFramework brmo;

    private IDatabaseConnection rsgb;
    private IDatabaseConnection staging;

    private final String sBestandsNaam = "/GH557-brk-comfort-adres/staging-flat.xml";
    private final String rBestandsNaam = "/GH557-brk-comfort-adres/rsgb-flat.xml";
    private final Lock sequential = new ReentrantLock();

    private UpdatesActionBean bean;
    @Before
    @Override
    public void setUp() throws Exception {
        
        // mock de context van de bean
        ServletContext sctx = mock(ServletContext.class);
        ActionBeanContext actx = mock(ActionBeanContext.class);
        bean = spy(UpdatesActionBean.class);
        when(bean.getContext()).thenReturn(actx);
        when(actx.getServletContext()).thenReturn(sctx);

        assumeTrue("Het bestand met staging testdata zou moeten bestaan.", UpdateBeginDatumZakRechtIntegrationTest.class.getResource(sBestandsNaam) != null);
        assumeTrue("Het bestand met rsgb testdata zou moeten bestaan.", UpdateBeginDatumZakRechtIntegrationTest.class.getResource(rBestandsNaam) != null);

        BasicDataSource dsStaging = new BasicDataSource();
        dsStaging.setUrl(DBPROPS.getProperty("staging.url"));
        dsStaging.setUsername(DBPROPS.getProperty("staging.username"));
        dsStaging.setPassword(DBPROPS.getProperty("staging.password"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        BasicDataSource dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(DBPROPS.getProperty("rsgb.url"));
        dsRsgb.setUsername(DBPROPS.getProperty("rsgb.username"));
        dsRsgb.setPassword(DBPROPS.getProperty("rsgb.password"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);

        rsgb = new DatabaseDataSourceConnection(dsRsgb);
        staging = new DatabaseDataSourceConnection(dsStaging);

        if (this.isMsSQL) {
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), DBPROPS.getProperty("rsgb.username").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), DBPROPS.getProperty("staging.username").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegegeven");
        }

        staging.getConfig().setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);
        rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, true);
        setupJNDI(dsRsgb, dsStaging);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(UpdateBeginDatumZakRechtIntegrationTest.class.getResource(sBestandsNaam).toURI())));
        IDataSet rsgbDataSet = fxdb.build(new FileInputStream(new File(UpdateBeginDatumZakRechtIntegrationTest.class.getResource(rBestandsNaam).toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        DatabaseOperation.CLEAN_INSERT.execute(rsgb, rsgbDataSet);

        brmo = new BrmoFramework(dsStaging, dsRsgb);
        brmo.setOrderBerichten(true);

        assumeTrue("Er zijn x RSGB_OK berichten", 1 == brmo.getCountBerichten(null, null, "brk", "RSGB_OK"));
        assumeTrue("Er zijn x STAGING_OK laadprocessen", 1 == brmo.getCountLaadProcessen(null, null, "brk", "STAGING_OK"));
    }

    
    @After
    public void cleanup() throws Exception {
        if (brmo != null) {
            // in geval van niet waar gemaakte assumptions
            brmo.closeBrmoFramework();
        }
        if (rsgb != null) {
            CleanUtil.cleanRSGB_BRK(rsgb, true);
            rsgb.close();
        }
        if (staging != null) {
            CleanUtil.cleanSTAGING(staging);
            staging.close();
        }

        try {
            sequential.unlock();
        } catch (IllegalMonitorStateException e) {
            // in geval van niet waar gemaakte assumptions
            LOG.debug("unlock van thread is mislukt, mogelijk niet ge-lock-ed of test overgeslagen.");
        }
    }


    @Test
    public void runUpdate() throws Exception {

        ITable zak_recht = rsgb.createDataSet().getTable("zak_recht");
        assertEquals("Aantal zakelijk rechten klopt niet", 6, zak_recht.getRowCount());
        /*
        eerst checken of de datum leeg is, anders klopt de testdata niet
         */
        for (int i = 0; i < 6; i++) {
            assertNull("Ingangsdatum recht is gevuld", zak_recht.getValue(i, "ingangsdatum_recht"));
        }
       
        bean.populateUpdateProcesses();
        bean.setUpdateProcessName("Bijwerken ingangsdatum_recht zakelijk recht");
        bean.update();

        zak_recht = rsgb.createDataSet().getTable("zak_recht");
        assertEquals("Aantal zakelijk rechten klopt niet", 6, zak_recht.getRowCount());

        for (int i = 0; i < 6; i++) {
            assertNotNull("Ingangsdatum recht is niet gevuld", zak_recht.getValue(i, "ingangsdatum_recht"));
            assertNotNull("fk_3avr_aand recht is niet gevuld", zak_recht.getValue(i, "fk_3avr_aand"));
        }
    }
}
