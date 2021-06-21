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

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.jdbc.util.converter.OracleConnectionUnwrapper;
import org.apache.commons.dbcp2.BasicDataSource;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Draaien met:
 * {@code mvn -Dit.test=WozXMLToStagingIntegrationTest -Dtest.onlyITs=true verify -pl brmo-loader -Ppostgresql > /tmp/postgresql.log}
 * voor bijvoorbeeld PostgreSQL of
 * {@code mvn -Dit.test=WozXMLToStagingIntegrationTest -Dtest.onlyITs=true verify -pl brmo-loader -Pmssql > /tmp/mssql.log}
 * voor bijvoorbeeld MS SQL.
 *
 * @author Mark Prins
 */
public class WozXMLToStagingIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(WozXMLToStagingIntegrationTest.class);
    private final Lock sequential = new ReentrantLock();
    private BrmoFramework brmo;
    // dbunit
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // {"filename", aantalBerichten, aantalLaadProcessen, objectRefs, objNummer, grondoppervlakte},
//                arguments("/woz/800000793120/204253181.xml", 1, 1, {"WOZ.WOZ.800000793120"}, "800000793120", 4000),
//                arguments("/woz/800000793120/204325718.xml", 2, 1, new String[]{"WOZ.NPS.295f133e37f55dd610756bbb0e6eebcf0ebbc555", "WOZ.WOZ.800000200014"}, "800000200014", 500),
                arguments("/woz/800000200021/204405262.xml", 2, 1, new String[]{"WOZ.NNP.428228574", "WOZ.WOZ.800000200021"}, "800000200021", 200)

        );
    }

    @BeforeEach
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
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(WozXMLToStagingIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

//        CleanUtil.cleanRSGB_WOZ(rsgb, true);

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        assumeTrue(0L == brmo.getCountBerichten(null, null, BrmoFramework.BR_WOZ, "STAGING_OK"),
                "Er zijn geen STAGING_OK berichten");
        assumeTrue(0L == brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_WOZ, "STAGING_OK"),
                "Er zijn geen STAGING_OK laadprocessen");
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        CleanUtil.cleanSTAGING(staging, false);
        CleanUtil.cleanRSGB_WOZ(rsgb, true);
        staging.close();
        sequential.unlock();
    }

    @ParameterizedTest(name = "testWozBerichtToStagingToRsgb #{index}: type: {0}, bestand: {1}")
    @MethodSource("argumentsProvider")
    public void testWozBerichtToStagingToRsgb(String bestandNaam, long aantalBerichten, long aantalProcessen, String[] objectRefs, String objNummer, Number grondoppervlakte) throws Exception {

        brmo.loadFromFile(BrmoFramework.BR_WOZ, WozXMLToStagingIntegrationTest.class.getResource(bestandNaam).getFile(), null);

        assertEquals(aantalBerichten, brmo.getCountBerichten(null, null, BrmoFramework.BR_WOZ, "STAGING_OK"),
                "Verwacht aantal berichten");
        assertEquals(aantalProcessen, brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_WOZ, "STAGING_OK"),
                "Verwacht aantal laadprocessen");

        ITable bericht = staging.createDataSet().getTable("bericht");
        int rowNum = 0;
        for (String objectRef : objectRefs) {
            assertEquals(objectRef, bericht.getValue(rowNum, "object_ref"), "'object_ref' klopt niet");
            rowNum++;
        }


        LOG.debug("Transformeren berichten naar rsgb DB.");
        brmo.setOrderBerichten(true);
        Thread t = brmo.toRsgb();
        t.join();

        assertEquals(aantalBerichten, brmo.getCountBerichten(null, null, BrmoFramework.BR_WOZ, "RSGB_OK"),
                "Niet alle berichten zijn OK getransformeerd");

        for (Bericht b : brmo.listBerichten()) {
            Assertions.assertNotNull(b, "Bericht is 'null'");
            Assertions.assertNotNull(b.getDbXml(), "'db-xml' van bericht is 'null'");
        }

        ITable woz_obj = rsgb.createDataSet().getTable("woz_obj");
        assertEquals(1, woz_obj.getRowCount(), "Het aantal 'woz_obj' klopt niet");
        assertEquals(objNummer, woz_obj.getValue(0, "nummer").toString(), "WOZ object nummer is niet correct");
        assertEquals(grondoppervlakte, ((Number)woz_obj.getValue(0, "grondoppervlakte")).intValue(), "Oppervlakte object nummer is niet correct");

        ITable woz_deelobj = rsgb.createDataSet().getTable("woz_deelobj");

        ITable woz_belang = rsgb.createDataSet().getTable("woz_belang");

        ITable woz_waarde = rsgb.createDataSet().getTable("woz_waarde");
        // brondocument

    }
}
