/*
 * Copyright (C) 2018 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
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
import static org.junit.Assume.assumeTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Draaien met:
 * {@code mvn -Dit.test=GBAVXMLToStagingIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 * voor bijvoorbeeld PostgresQL.
 *
 * @author Mark Prins
 */
@RunWith(Parameterized.class)
public class GBAVXMLToStagingIntegrationTest extends AbstractDatabaseIntegrationTest {

    @Parameterized.Parameters(name = "{index}: type: {0}, bestand: {1}")
    public static Collection params() {
        return Arrays.asList(new Object[][]{
            // {"type","filename", aantalBerichten, aantalLaadProcessen},
            {"gbav", "/nl/b3p/brmo/loader/xml/gbav-voorbeeld.xml", 1, 1},});
    }

    private static final Log LOG = LogFactory.getLog(GBAVXMLToStagingIntegrationTest.class);

    /**
     * test parameter.
     */
    private final String bestandNaam;
    /**
     * test parameter.
     */
    private final String bestandType;
    /**
     * test parameter.
     */
    private final long aantalBerichten;
    /**
     * test parameter.
     */
    private final long aantalProcessen;

    private final Lock sequential = new ReentrantLock();
    private BrmoFramework brmo;

    // dbunit
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;

    public GBAVXMLToStagingIntegrationTest(String bestandType, String bestandNaam, long aantalBerichten, long aantalProcessen) {
        this.bestandType = bestandType;
        this.bestandNaam = bestandNaam;
        this.aantalBerichten = aantalBerichten;
        this.aantalProcessen = aantalProcessen;
    }

    @Before
    @Override
    public void setUp() throws Exception {
        BasicDataSource dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.jdbc.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.passwd"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        BasicDataSource dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(params.getProperty("rsgb.jdbc.url"));
        dsRsgb.setUsername(params.getProperty("rsgb.user"));
        dsRsgb.setPassword(params.getProperty("rsgb.passwd"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);

        staging = new DatabaseDataSourceConnection(dsStaging);
        rsgb = new DatabaseDataSourceConnection(dsRsgb);

        if (this.isMsSQL) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), params.getProperty("rsgb.user").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            // we hebben alleen nog postgres over
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }

        brmo = new BrmoFramework(dsStaging, dsRsgb);

        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(BAGXMLToStagingIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        assumeTrue("Er zijn geen STAGING_OK berichten", 0l == brmo.getCountBerichten(null, null, this.bestandType, "STAGING_OK"));
        assumeTrue("Er zijn geen STAGING_OK laadprocessen", 0l == brmo.getCountLaadProcessen(null, null, this.bestandType, "STAGING_OK"));
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        CleanUtil.cleanSTAGING(staging);
        CleanUtil.cleanRSGB_BAG(rsgb, true);
        CleanUtil.cleanRSGB_BRP(rsgb);
        staging.close();
        sequential.unlock();
    }

    @Test
    public void testGbavBerichtToStagingToRsgb() throws Exception {
        try {
            brmo.loadFromFile(bestandType, BAGXMLToStagingIntegrationTest.class.getResource(bestandNaam).getFile());
        } catch (BrmoLeegBestandException blbe) {
            LOG.debug("Er is een bestand zonder berichten geladen (kan voorkomen...).");
        }

        assertEquals("Verwacht aantal berichten", aantalBerichten, brmo.getCountBerichten(null, null, bestandType, "STAGING_OK"));
        assertEquals("Verwacht aantal laadprocessen", aantalProcessen, brmo.getCountLaadProcessen(null, null, bestandType, "STAGING_OK"));

        LOG.debug("Transformeren berichten naar rsgb DB.");
        brmo.setOrderBerichten(true);
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals("Niet alle berichten zijn OK getransformeerd", aantalBerichten, brmo.getCountBerichten(null, null, bestandType, "RSGB_OK"));

        for (Bericht b : brmo.listBerichten()) {
            assertNotNull("Bericht is 'null'", b);
            assertNotNull("'db-xml' van bericht is 'null'", b.getDbXml());
        }

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals("Het aantal klopt niet", aantalBerichten, subject.getRowCount());

        ITable prs = rsgb.createDataSet().getTable("prs");
        assertEquals("Het aantal klopt niet", aantalBerichten, prs.getRowCount());

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertEquals("Het aantal klopt niet", aantalBerichten, nat_prs.getRowCount());
        assertEquals("Aanduiding naamgebruik eerste record komt niet overeen", "E", nat_prs.getValue(0, "aand_naamgebruik"));
        assertEquals("Geslachtsaanduiding eerste record komt niet overeen", "V", nat_prs.getValue(0, "geslachtsaand"));
        assertEquals("Achternaam eerste record komt niet overeen", "Kumari", nat_prs.getValue(0, "nm_geslachtsnaam"));

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertEquals("Het aantal klopt niet", aantalBerichten, ingeschr_nat_prs.getRowCount());
        assertEquals("BSN eerste record komt niet overeen", new BigDecimal(123459916), ingeschr_nat_prs.getValue(0, "bsn"));
        assertEquals("A-nummer eerste record komt niet overeen", new BigDecimal("5054783237"), ingeschr_nat_prs.getValue(0, "a_nummer"));
    }
}
