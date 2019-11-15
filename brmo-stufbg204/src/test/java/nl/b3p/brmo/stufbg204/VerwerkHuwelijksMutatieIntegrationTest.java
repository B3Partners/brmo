/*
 * Copyright (C) 2018 B3Partners B.V.
 */
package nl.b3p.brmo.stufbg204;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.loader.jdbc.OracleConnectionUnwrapper;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * Deze testcase verwerkt een soap bericht uit een bestand als kennisgeving. Het
 * bestand bevat een HuwelijksMutatie welke naar de stufbg204 async service
 * wordt gepost, hierna wordt in de staging gechecked voor laadproces en bericht
 * en wordt na trasnformatie in de rsgb gechecked.
 *
 * @author Mark Prins
 * @code mvn -Dit.test=VerwerkHuwelijksMutatieIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql > target/postgresql.log}
 */
public class VerwerkHuwelijksMutatieIntegrationTest extends WebTestStub {

    private static final Log LOG = LogFactory.getLog(VerwerkHuwelijksMutatieIntegrationTest.class);
    private IDatabaseConnection staging;
    private IDatabaseConnection rsgb;
    private final Lock sequential = new ReentrantLock();
    private BrmoFramework brmo;

    @Before
    @Override
    public void setUp() throws SQLException, BrmoException, DatabaseUnitException {
        brmo = new BrmoFramework(dsStaging, dsRsgb, null);
        staging = new DatabaseDataSourceConnection(dsStaging);
        rsgb = new DatabaseDataSourceConnection(dsRsgb);

        if (this.isMsSQL) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            dsStaging.getConnection().setAutoCommit(true);
            dsRsgb.getConnection().setAutoCommit(true);
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), DBPROPS.getProperty("staging.username").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
            rsgb = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsRsgb.getConnection()), DBPROPS.getProperty("rsgb.username").toUpperCase());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            rsgb.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        } else {
            fail("Geen ondersteunde database aangegegeven");
        }

        sequential.lock();
    }

    @After
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();
        CleanUtil.cleanSTAGING(staging, false);
        CleanUtil.cleanRSGB_BRP(rsgb);
        staging.close();
        rsgb.close();
        sequential.unlock();
    }

    @Test
    @Ignore("Deze test mislukt omdat in het bericht de gegevens van de partner ontbreken, het bericht kan daardoor niet verwerkt worden")
    public void testBericht() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        InputStreamEntity reqEntity = new InputStreamEntity(
                VerwerkHuwelijksMutatieIntegrationTest.class.getResourceAsStream("/OpnemenHuwelijkScenario/StUF_request.xml"),
                -1,
                ContentType.create("text/xml", "utf-8")
        );
        reqEntity.setChunked(true);
        HttpPost httppost = new HttpPost(BASE_TEST_URL + "StUFBGAsynchroon");
        httppost.setEntity(reqEntity);
        // httppost.addHeader("SOAPAction", "ontvangKennisgeving");

        LOG.debug("SOAP request uitvoeren: " + httppost.getRequestLine());

        // set-up preemptive auth voor request en stuur bericht
        CloseableHttpResponse response = client.execute(target, httppost, localContext);

        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));

        // check staging database inhoud
        assertTrue("Er zijn anders dan 1 STAGING_OK laadprocessen", 1l <= brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        assertEquals("datum klopt niet", /* StUFbg204Util.sdf.parse("201812211418450000") */ "2018-12-21 14:18:45.0", laadproces.getValue(0, "bestand_datum").toString());

        assertTrue("Er zijn anders dan 1 STAGING_OK berichten", 1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("object ref klopt niet", "NL.BRP.Persoon.df2e41b72f8a3421ef575617fc247a77018a573f", bericht.getValue(0, "object_ref"));
        assertEquals("datum klopt niet", "2018-12-21 14:18:45.0", bericht.getValue(0, "datum").toString());

        // transformeren van bericht en check rsgb database inhoud
        Thread t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertTrue("Aantal rijen klopt niet", subject.getRowCount() == 1);
        assertEquals("naam niet als verwacht", "J de Cuykelaer", subject.getValue(0, "naam"));

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertTrue("Aantal rijen klopt niet", nat_prs.getRowCount() == 1);
        assertEquals("geslacht niet als verwacht", "M", nat_prs.getValue(1, "geslachtsaand"));
        assertEquals("geslachtsnaam niet als verwacht", "Cuykelaer", nat_prs.getValue(0, "nm_geslachtsnaam"));

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertTrue("Aantal rijen klopt niet", ingeschr_nat_prs.getRowCount() == 1);
        assertEquals("a nummer niet als verwacht", "9173658014", ingeschr_nat_prs.getValue(0, "a_nummer"));
        assertEquals("bsn nummer niet als verwacht", "301571818", ingeschr_nat_prs.getValue(0, "bsn"));
    }

    @Test
    @Ignore("Deze test mislukt omdat in het bericht de gegevens van de partner ontbreken, het bericht kan daardoor niet verwerkt worden")
    public void testToevoegingMetHuwelijkAlsComfort() throws IOException, DataSetException, SQLException, InterruptedException, ParseException, BrmoException {
        InputStreamEntity reqEntity = new InputStreamEntity(
                VerwerkToevoegingMutatieIntegrationTest.class.getResourceAsStream("/testdata-tnt/SoapBerichten/toevoeging_huwelijk_als_comfort.xml"),
                -1,
                ContentType.create("text/xml", "utf-8")
        );
        reqEntity.setChunked(true);
        HttpPost httppost = new HttpPost(BASE_TEST_URL + "StUFBGAsynchroon");
        httppost.setEntity(reqEntity);
        // httppost.addHeader("SOAPAction", "ontvangKennisgeving");

        LOG.debug("SOAP request uitvoeren: " + httppost.getRequestLine());

        // set-up preemptive auth voor request en stuur bericht
        CloseableHttpResponse response = client.execute(target, httppost, localContext);

        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));

        // check staging database inhoud
        assertTrue("Er zijn anders dan 1 STAGING_OK laadprocessen", 1l == brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SS");
        assertEquals("datum klopt niet", "2019-01-14 16:04:37.0", laadproces.getValue(0, "bestand_datum").toString());

        assertTrue("Er zijn anders dan 1 STAGING_OK berichten", 1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("object ref klopt niet", "NL.BRP.Persoon.fcd17242d8211342df48efd67341ca9765de8347", bericht.getValue(0, "object_ref"));
        assertEquals("datum klopt niet", "2019-01-14 16:04:37.0", bericht.getValue(0, "datum").toString());

        // transformeren van bericht en check rsgb database inhoud
        Thread t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertEquals("Aantal rijen klopt niet", 1, subject.getRowCount());
        assertEquals("naam niet als verwacht", "727b5940 900ed1be 8b701201 935a9e ba5b4d6e d9b9226a", subject.getValue(0, "naam"));

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertTrue("Aantal rijen klopt niet", nat_prs.getRowCount() == 1);
        assertEquals("geslacht niet als verwacht", "M", nat_prs.getValue(1, "geslachtsaand"));
        // assertEquals("geslachtsnaam niet als verwacht", "Cuykelaer", nat_prs.getValue(0, "nm_geslachtsnaam"));

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertTrue("Aantal rijen klopt niet", ingeschr_nat_prs.getRowCount() == 1);
        assertEquals("a nummer niet als verwacht", "6171717520", ingeschr_nat_prs.getValue(0, "a_nummer"));
        assertEquals("bsn nummer niet als verwacht", "749069273", ingeschr_nat_prs.getValue(0, "bsn"));
    }


    @Test
    @Ignore("TODO de 2e persoon wordt niet uitgeparsed")
    public void testBericht2() throws IOException, DataSetException, SQLException, BrmoException, InterruptedException, ParseException {
        InputStreamEntity reqEntity = new InputStreamEntity(
                VerwerkHuwelijksMutatieIntegrationTest.class.getResourceAsStream("/OpnemenHuwelijkScenario/StUF_request2.xml"),
                -1,
                ContentType.create("text/xml", "utf-8")
        );
        reqEntity.setChunked(true);
        HttpPost httppost = new HttpPost(BASE_TEST_URL + "StUFBGAsynchroon");
        httppost.setEntity(reqEntity);
        // httppost.addHeader("SOAPAction", "ontvangKennisgeving");

        LOG.debug("SOAP request uitvoeren: " + httppost.getRequestLine());

        // set-up preemptive auth voor request en stuur bericht
        CloseableHttpResponse response = client.execute(target, httppost, localContext);

        assertThat("Response status is OK.", response.getStatusLine().getStatusCode(), equalTo(HttpStatus.SC_OK));

        // check staging database inhoud
        assertTrue("Er zijn anders dan 1 STAGING_OK laadprocessen", 1l <= brmo.getCountLaadProcessen(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));

        ITable laadproces = staging.createDataSet().getTable("laadproces");
        assertEquals("datum klopt niet", /* StUFbg204Util.sdf.parse("201812211418450000") */ "2018-12-21 14:18:45.0", laadproces.getValue(0, "bestand_datum").toString());

        assertTrue("Er zijn anders dan 1 STAGING_OK berichten", 1l <= brmo.getCountBerichten(null, null, BrmoFramework.BR_BRP, "STAGING_OK"));
        ITable bericht = staging.createDataSet().getTable("bericht");
        assertEquals("object ref klopt niet", "NL.BRP.Persoon.df2e41b72f8a3421ef575617fc247a77018a573f", bericht.getValue(0, "object_ref"));
        assertEquals("datum klopt niet", "2018-12-21 14:18:45.0", bericht.getValue(0, "datum").toString());

        // transformeren van bericht en check rsgb database inhoud
        Thread t = brmo.toRsgb();
        t.join();

        ITable subject = rsgb.createDataSet().getTable("subject");
        assertTrue("Aantal rijen klopt niet", subject.getRowCount() == 1);
        assertEquals("naam niet als verwacht", "J de Cuykelaer", subject.getValue(0, "naam"));

        ITable nat_prs = rsgb.createDataSet().getTable("nat_prs");
        assertTrue("Aantal rijen klopt niet", nat_prs.getRowCount() == 1);
        assertEquals("geslacht niet als verwacht", "M", nat_prs.getValue(1, "geslachtsaand"));
        assertEquals("geslachtsnaam niet als verwacht", "Cuykelaer", nat_prs.getValue(0, "nm_geslachtsnaam"));

        ITable ingeschr_nat_prs = rsgb.createDataSet().getTable("ingeschr_nat_prs");
        assertTrue("Aantal rijen klopt niet", ingeschr_nat_prs.getRowCount() == 1);
        assertEquals("a nummer niet als verwacht", "9173658014", ingeschr_nat_prs.getValue(0, "a_nummer"));
        assertEquals("bsn nummer niet als verwacht", "301571818", ingeschr_nat_prs.getValue(0, "bsn"));
    }
}
