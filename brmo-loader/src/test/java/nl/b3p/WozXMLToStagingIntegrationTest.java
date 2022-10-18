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
import org.dbunit.dataset.Column;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.dataset.xml.XmlDataSet;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
 * Draaien met:
 * {@code mvn -Dit.test=WozXMLToStagingIntegrationTest -Dtest.onlyITs=true verify -pl brmo-loader -Ppostgresql > /tmp/postgresql.log}
 * voor bijvoorbeeld PostgreSQL of
 * {@code mvn -Dit.test=WozXMLToStagingIntegrationTest -Dtest.onlyITs=true verify -pl brmo-loader -Poracle > /tmp/oracle.log}
 * voor oracle.
 *
 * @author Mark Prins
 */
@Tag("skip-windows-java11")
public class WozXMLToStagingIntegrationTest extends AbstractDatabaseIntegrationTest {

    private static final Log LOG = LogFactory.getLog(WozXMLToStagingIntegrationTest.class);
    private final Lock sequential = new ReentrantLock();
    private BrmoFramework brmo;
    // dbunit
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;
    private BasicDataSource dsRsgb;
    private BasicDataSource dsStaging;

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // {"filename", aantalBerichten, aantalLaadProcessen, objectRefs, objNummer, grondoppervlakte, gem_code, ws_code, wozBelang[rij][cols], deelObjectNums[], wozOmvatKadIdentif[]},
                arguments("/woz/800000793120/204253181.xml", 1, 1, new String[]{"WOZ.WOZ.800000793120"}, "800000793120", 4000, 8000, "8106", new String[0][0], new String[0], new String[]{"8000552570003"}),
                arguments("/woz/800000793120/204325718.xml", 2, 1, new String[]{"WOZ.NPS.295f133e37f55dd610756bbb0e6eebcf0ebbc555", "WOZ.WOZ.800000200014"}, "800000200014", 500, 8000, "8106", new String[][]{{"WOZ.NPS.295f133e37f55dd610756bbb0e6eebcf0ebbc555", "800000200014", "E"}}, new String[]{"800000793120"}, new String[]{"8000552570004"}),
                arguments("/woz/800000200021/204405262.xml", 2, 1, new String[]{"WOZ.NNP.428228574", "WOZ.WOZ.800000200021"}, "800000200021", 200, 8000, "8106", new String[][]{{"WOZ.NNP.428228574", "800000200021", "E"}}, new String[0], new String[]{"8000552570005"}),
                arguments("/woz/object_met_geom.xml", 1, 1, new String[]{"WOZ.WOZ.800000003123"}, "800000003123", 450, 8000, "0372", new String[][]{{"WOZ.NPS.e19242199d42fea43af7201c13ec4ad980f1e2cb", "800000003123", "E"}}, new String[0], new String[]{"8000010170000"}),
                arguments("/woz/BRMO-204_lege_kad_identif/086600003277.anon.xml",1,1,new String[]{"WOZ.WOZ.086600003277"},"86600003277", 218, 866, "0106", new String[][]{{"WOZ.NPS.42ee4ccf42af8c36500c43a5d105dfbf38175c31", "86600003277", "G"},{"WOZ.NPS.b9d71af53ab19882d968391b1d13497d2a5c5cf0", "86600003277", "E"},{"WOZ.NPS.ccc686355b0dd382c7bedce5f98ac6e4f846be25", "86600003277", "E"}}, new String[0], new String[0]),
                // ingewikkeld bericht met historie en actueel
                arguments("/woz/BRMO-184/45272376.anon.xml", 2, 1, new String[]{"WOZ.WOZ.077200029855"}, "077200029855", 18, 772, "0106", new String[][]{{"WOZ.NPS.1e4cfa740d8a05720aa51ac670a2561cd659a6f8", "800000003123", "E"}}, new String[0], new String[]{"8000010170000"})
        );
    }

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.jdbc.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.passwd"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);

        dsRsgb = new BasicDataSource();
        dsRsgb.setUrl(params.getProperty("rsgb.jdbc.url"));
        dsRsgb.setUsername(params.getProperty("rsgb.user"));
        dsRsgb.setPassword(params.getProperty("rsgb.passwd"));
        dsRsgb.setAccessToUnderlyingConnectionAllowed(true);

        staging = new DatabaseDataSourceConnection(dsStaging);
        rsgb = new DatabaseDataSourceConnection(dsRsgb);

        if (this.isOracle) {
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

         CleanUtil.cleanRSGB_WOZ(rsgb, true);

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);
        assumeTrue(0L == brmo.getCountBerichten(null, null, BrmoFramework.BR_WOZ, "STAGING_OK"),
                "Er zijn geen STAGING_OK berichten");
        assumeTrue(0L == brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_WOZ, "STAGING_OK"),
                "Er zijn geen STAGING_OK laadprocessen");
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
//        CleanUtil.cleanSTAGING(staging, false);
//        CleanUtil.cleanRSGB_WOZ(rsgb, true);
        staging.close();
        dsStaging.close();
        rsgb.close();
        dsRsgb.close();
        sequential.unlock();
    }

    @ParameterizedTest(name = "testWozBerichtToStagingToRsgb #{index}: bestand: {0}")
    @MethodSource("argumentsProvider")
    public void testWozBerichtToStagingToRsgb(String bestandNaam,
                                              long aantalBerichten,
                                              long aantalProcessen,
                                              String[] objectRefs,
                                              String objNummer,
                                              Number grondoppervlakte,
                                              Number gemCode,
                                              String wsCode,
                                              String[][] wozBelang,
                                              String[] deelObjectNums,
                                              String[] wozOmvatKadIdentif) throws Exception {

        if (objNummer.equalsIgnoreCase("800000003123")) {
            IDataSet rsgbDataSet = new XmlDataSet(new FileInputStream(WozXMLToStagingIntegrationTest.class.getResource("/woz/subject-setup.xml").getFile()));
            DatabaseOperation.INSERT.execute(rsgb, rsgbDataSet);
        }

        assumeTrue(WozXMLToStagingIntegrationTest.class.getResource(bestandNaam) != null, "Het bestand met test bericht zou moeten bestaan.");
        brmo.loadFromFile(BrmoFramework.BR_WOZ, WozXMLToStagingIntegrationTest.class.getResource(bestandNaam).getFile(), null);

        assertEquals(aantalBerichten, brmo.getCountBerichten(null, null, BrmoFramework.BR_WOZ, "STAGING_OK"),
                "Verwacht aantal STAGING_OK berichten");
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

        for (Bericht b : brmo.getBerichten(0, 0, 10, null, null, BrmoFramework.BR_WOZ, "RSGB_OK")) {
            assertNotNull(b, "Bericht is 'null'");
            assertNotNull(b.getDbXml(), "'db-xml' van bericht is 'null'");
        }

        ITable woz_obj = rsgb.createDataSet().getTable("woz_obj");
        assertEquals(1, woz_obj.getRowCount(), "Het aantal 'woz_obj' klopt niet");
        assertEquals(objNummer, woz_obj.getValue(0, "nummer").toString(), "WOZ object nummer is niet correct");
        assertEquals(grondoppervlakte, ((Number) woz_obj.getValue(0, "grondoppervlakte")).intValue(), "Oppervlakte object nummer is niet correct");
        assertEquals(wsCode, woz_obj.getValue(0, "waterschap"), "Waterschap object is niet correct");
        assertEquals(gemCode, ((Number) woz_obj.getValue(0, "fk_verantw_gem_code")).intValue(), "Gemeentecode object is niet correct");

        if (objNummer.equalsIgnoreCase("800000003123")) {
            assertNotNull(woz_obj.getValue(0, "geom"), "geometrie verwacht voor dit object");
        }

        ITable woz_deelobj = rsgb.createDataSet().getTable("woz_deelobj");
        assertEquals(deelObjectNums.length, woz_deelobj.getRowCount(), "Het aantal 'woz_deelobj' klopt niet");
        for (int i = 0; i < deelObjectNums.length; i++) {
            assertEquals(deelObjectNums[i], woz_deelobj.getValue(i, "nummer").toString(), "WOZ deelobject nummer is niet correct");
            assertEquals(objNummer, woz_deelobj.getValue(i, "fk_6woz_nummer").toString(), "WOZ object nummer is niet correct");
        }

        ITable woz_belang = rsgb.createDataSet().getTable("woz_belang");
        assertEquals(wozBelang.length, woz_belang.getRowCount(), "Het aantal 'woz_belang' klopt niet");
        if (wozBelang.length > 0) {
            final Column[] woz_belang_cols = woz_belang.getTableMetaData().getColumns();
            for (int row = 0; row < wozBelang.length; row++) {
                for (int i = 0; i < woz_belang_cols.length; i++) {
                    assertEquals(
                            wozBelang[row][i],
                            woz_belang.getValue(row, woz_belang_cols[i].getColumnName()).toString(),
                            "woz belang " + woz_belang_cols[i].getColumnName() + " is niet correct"
                    );
                }
            }
        }

        ITable woz_omvat = rsgb.createDataSet().getTable("woz_omvat");
        assertEquals(wozOmvatKadIdentif.length, woz_omvat.getRowCount(), "Het aantal 'woz_omvat' klopt niet");
        for (int i = 0; i < wozOmvatKadIdentif.length; i++) {
            assertEquals(wozOmvatKadIdentif[i], woz_omvat.getValue(i, "fk_sc_lh_kad_identif").toString(), "kad-identif nummer is niet correct");
        }

        ITable woz_waarde = rsgb.createDataSet().getTable("woz_waarde");

        ITable brondocument = rsgb.createDataSet().getTable("brondocument");

    }
}
